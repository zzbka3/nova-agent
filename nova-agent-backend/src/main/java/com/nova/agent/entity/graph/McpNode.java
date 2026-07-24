package com.nova.agent.entity.graph;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nova.agent.constant.AgentFlowContextVar;
import com.nova.agent.entity.AgentFlow;
import com.nova.agent.entity.InputVar;
import com.nova.agent.entity.OutPutVar;
import com.nova.agent.enums.NodeType;
import com.nova.agent.enums.VarType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MCP (Model Context Protocol) Node
 * Connects to external MCP servers via SSE + JSON-RPC for tool execution.
 */
@Slf4j
public class McpNode extends Node {

    private String mcpServerUrl;
    private String selectedToolName;
    private String outputFormat;
    private List<JSONObject> availableTools;

    public McpNode(String nodeId, String name, NodeType nodeType,
                   List<InputVar> inputVars, List<OutPutVar> outputVars, String config) {
        super(nodeId, name, nodeType, inputVars, outputVars, config);
        try {
            McpConfig conf = JSON.parseObject(config, McpConfig.class);
            if (conf.getMcpServers() == null || conf.getMcpServers().isEmpty()) {
                throw new IllegalArgumentException("MCP configuration missing mcpServers");
            }
            McpServer server = conf.getMcpServers().values().iterator().next();
            if (server == null || server.getUrl() == null) {
                throw new IllegalArgumentException("MCP server missing url");
            }
            this.mcpServerUrl = server.getUrl();
            this.selectedToolName = conf.getSelectedToolName();
            this.outputFormat = conf.getOutputFormat();
        } catch (Exception e) {
            throw new IllegalArgumentException("MCP node config parse error: " + e.getMessage());
        }
    }

    @Override
    public void run(AgentFlow agentFlow) {
        // Runtime override from input vars
        if (inputVars != null) {
            for (InputVar v : inputVars) {
                if ("selectedToolName".equals(v.getVarName()) && v.getVarValue() != null) {
                    this.selectedToolName = String.valueOf(v.getVarValue());
                } else if ("outputFormat".equals(v.getVarName()) && v.getVarValue() != null) {
                    this.outputFormat = String.valueOf(v.getVarValue());
                }
            }
        }

        if (selectedToolName == null || selectedToolName.trim().isEmpty()) {
            throw new RuntimeException("No tool name specified for MCP call");
        }
        if (mcpServerUrl == null || mcpServerUrl.isEmpty()) {
            throw new IllegalArgumentException("MCP Server URL not configured");
        }

        HttpURLConnection sseConnection = null;
        BufferedReader sseReader = null;
        try {
            // 1. Establish SSE connection
            URL url = new URL(mcpServerUrl);
            sseConnection = (HttpURLConnection) url.openConnection();
            sseConnection.setRequestMethod("GET");
            sseConnection.setRequestProperty("Accept", "text/event-stream");
            sseConnection.setDoInput(true);
            sseConnection.setConnectTimeout(10000);
            sseConnection.setReadTimeout(30000);

            int responseCode = sseConnection.getResponseCode();
            if (responseCode < 200 || responseCode >= 300) {
                throw new RuntimeException("MCP SSE connection failed, HTTP " + responseCode);
            }

            sseReader = new BufferedReader(new InputStreamReader(sseConnection.getInputStream()));

            // 2. Get messages endpoint from SSE
            String messagesUrl = resolveMessagesEndpoint(url, sseReader);

            // 3. Initialize JSON-RPC handshake
            JSONObject initRequest = new JSONObject();
            initRequest.put("jsonrpc", "2.0");
            initRequest.put("id", 1);
            initRequest.put("method", "initialize");
            JSONObject initParams = new JSONObject();
            initParams.put("protocolVersion", "2025-03-26");
            JSONObject capabilities = new JSONObject();
            JSONObject rootsCap = new JSONObject();
            rootsCap.put("listChanged", true);
            capabilities.put("roots", rootsCap);
            initParams.put("capabilities", capabilities);
            JSONObject clientInfo = new JSONObject();
            clientInfo.put("name", "NovaAgent");
            clientInfo.put("version", "1.0");
            initParams.put("clientInfo", clientInfo);
            initRequest.put("params", initParams);

            sendJsonMessage(messagesUrl, initRequest.toJSONString());
            JSONObject initResponse = waitForJsonResponse(sseReader, 1);
            if (initResponse == null || initResponse.getJSONObject("error") != null) {
                throw new RuntimeException("MCP initialize failed");
            }

            // Send initialized notification
            JSONObject initializedNote = new JSONObject();
            initializedNote.put("jsonrpc", "2.0");
            initializedNote.put("method", "notifications/initialized");
            sendJsonMessage(messagesUrl, initializedNote.toJSONString());

            // 4. List tools
            JSONObject listRequest = new JSONObject();
            listRequest.put("jsonrpc", "2.0");
            listRequest.put("id", 2);
            listRequest.put("method", "tools/list");
            listRequest.put("params", new JSONObject());
            sendJsonMessage(messagesUrl, listRequest.toJSONString());

            JSONObject listResponse = waitForJsonResponse(sseReader, 2);
            if (listResponse == null || listResponse.getJSONObject("result") == null) {
                throw new RuntimeException("tools/list returned error");
            }
            JSONArray toolsArr = listResponse.getJSONObject("result").getJSONArray("tools");
            this.availableTools = new ArrayList<>();
            if (toolsArr != null) {
                for (int i = 0; i < toolsArr.size(); i++) {
                    this.availableTools.add(toolsArr.getJSONObject(i));
                }
            }

            // 5. Call the selected tool
            int callRequestId = 3;
            JSONObject callRequest = new JSONObject();
            callRequest.put("jsonrpc", "2.0");
            callRequest.put("id", callRequestId);
            callRequest.put("method", "tools/call");
            JSONObject callParams = new JSONObject();
            callParams.put("name", selectedToolName);

            // Build arguments from input vars
            JSONObject argsObj = new JSONObject();
            if (inputVars != null) {
                for (InputVar var : inputVars) {
                    if (var.getVarType() == VarType.reference) continue;
                    Object val = var.getVarValue();
                    if (val instanceof String && var.getVarType() != VarType.String && var.getVarType() != VarType.Any) {
                        Object converted = agentFlow.covert((String) val, var.getVarType());
                        if (converted != null) val = converted;
                    }
                    String varName = var.getVarName();
                    if (varName.contains(".")) {
                        putNestedJson(argsObj, varName, val);
                    } else {
                        argsObj.put(varName, val);
                    }
                }
            }
            callParams.put("arguments", argsObj);
            callRequest.put("params", callParams);

            sendJsonMessage(messagesUrl, callRequest.toJSONString());
            JSONObject callResp = waitForJsonResponse(sseReader, callRequestId);
            if (callResp == null || callResp.getJSONObject("result") == null) {
                throw new RuntimeException("tools/call returned error");
            }

            // 6. Build result and store in context
            JSONObject resultObj = callResp.getJSONObject("result");
            JSONObject standardOutput = new JSONObject();
            standardOutput.put("error_code", 0);
            standardOutput.put("content", resultObj.getJSONArray("content"));
            standardOutput.put("selected_tool", this.selectedToolName);
            standardOutput.put("output_format", this.outputFormat);
            agentFlow.setContextVar(AgentFlowContextVar.NODE_RESULT_PREFIX + this.nodeId,
                    standardOutput.toJSONString());

        } catch (Exception e) {
            throw new RuntimeException("McpNode execution error: " + e.getMessage(), e);
        } finally {
            if (sseReader != null) {
                try { sseReader.close(); } catch (Exception ignored) {}
            }
            if (sseConnection != null) {
                sseConnection.disconnect();
            }
        }
    }

    @Override
    public void fillOutputVar(AgentFlow agentFlow) {
        if (outputVars == null || outputVars.isEmpty()) return;

        Object raw = agentFlow.getContextVar(AgentFlowContextVar.NODE_RESULT_PREFIX + this.nodeId);
        if (raw == null) return;

        try {
            JSONObject resultJson = JSON.parseObject(raw.toString());
            for (OutPutVar out : outputVars) {
                String name = out.getVarName();
                if ("error_code".equals(name)) {
                    out.setVarValue(resultJson.getInteger("error_code"));
                } else if ("content".equals(name)) {
                    JSONArray contentArr = resultJson.getJSONArray("content");
                    if (contentArr != null) {
                        if (out.getVarType() == VarType.String) {
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < contentArr.size(); i++) {
                                JSONObject item = contentArr.getJSONObject(i);
                                sb.append(item.containsKey("text") ? item.getString("text") : item.toJSONString());
                            }
                            out.setVarValue(sb.toString());
                        } else {
                            out.setVarValue(contentArr);
                        }
                    }
                } else if ("result".equals(name)) {
                    JSONArray contentArr = resultJson.getJSONArray("content");
                    if (contentArr != null && !contentArr.isEmpty()) {
                        String text = contentArr.getJSONObject(0).getString("text");
                        try {
                            out.setVarValue(JSON.parse(text));
                        } catch (Exception e) {
                            out.setVarValue(text);
                        }
                    }
                } else if ("selected_tool".equals(name)) {
                    out.setVarValue(resultJson.getString("selected_tool"));
                } else if ("output_format".equals(name)) {
                    out.setVarValue(resultJson.getString("output_format"));
                } else {
                    if (resultJson.containsKey(name)) {
                        out.setVarValue(resultJson.get(name));
                    }
                }
            }
        } catch (Exception e) {
            log.error("McpNode fill output error!", e);
        }
    }

    private String resolveMessagesEndpoint(URL url, BufferedReader reader) throws Exception {
        String line, eventType = null;
        StringBuilder dataBuf = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) {
                if ("endpoint".equals(eventType)) {
                    String endpoint = dataBuf.toString().trim();
                    if (endpoint.startsWith("http")) {
                        return endpoint;
                    }
                    return url.getProtocol() + "://" + url.getHost()
                            + (url.getPort() != -1 ? ":" + url.getPort() : "") + endpoint;
                }
                eventType = null;
                dataBuf.setLength(0);
                continue;
            }
            if (line.startsWith("event:")) {
                eventType = line.substring("event:".length()).trim();
            } else if (line.startsWith("data:")) {
                dataBuf.append(line.substring("data:".length()));
            }
        }
        throw new RuntimeException("Could not get messages endpoint from SSE stream");
    }

    private void sendJsonMessage(String endpointUrl, String jsonBody) throws Exception {
        URL endpoint = new URL(endpointUrl);
        HttpURLConnection conn = (HttpURLConnection) endpoint.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setRequestProperty("Content-Type", "application/json");
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes());
            os.flush();
        }
        int code = conn.getResponseCode();
        if (code < 200 || code >= 300) {
            throw new RuntimeException("MCP send JSON failed, HTTP " + code);
        }
        conn.disconnect();
    }

    private JSONObject waitForJsonResponse(BufferedReader reader, int requestId) throws Exception {
        String line, eventType = null;
        StringBuilder dataBuf = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) {
                if ("message".equals(eventType)) {
                    String data = dataBuf.toString().trim();
                    if (!data.isEmpty()) {
                        try {
                            JSONObject json = JSON.parseObject(data);
                            if (json.getInteger("id") != null && json.getInteger("id") == requestId) {
                                return json;
                            }
                        } catch (Exception ignored) {}
                    }
                }
                eventType = null;
                dataBuf.setLength(0);
                continue;
            }
            if (line.startsWith("event:")) {
                eventType = line.substring("event:".length()).trim();
            } else if (line.startsWith("data:")) {
                dataBuf.append(line.substring("data:".length()));
            }
        }
        return null;
    }

    private void putNestedJson(JSONObject parent, String path, Object value) {
        String[] parts = path.split("\\.", 2);
        if (parts.length == 1) {
            parent.put(parts[0], value);
        } else {
            JSONObject child = parent.getJSONObject(parts[0]);
            if (child == null) {
                child = new JSONObject();
                parent.put(parts[0], child);
            }
            putNestedJson(child, parts[1], value);
        }
    }

    @Data
    public static class McpConfig {
        private Map<String, McpServer> mcpServers;
        private String selectedToolName;
        private String outputFormat;
    }

    @Data
    public static class McpServer {
        private String url;
    }
}
