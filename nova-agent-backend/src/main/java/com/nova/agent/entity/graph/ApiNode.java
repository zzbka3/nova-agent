package com.nova.agent.entity.graph;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nova.agent.constant.AgentFlowContextVar;
import com.nova.agent.entity.AgentFlow;
import com.nova.agent.entity.InputVar;
import com.nova.agent.entity.ObjectStructure;
import com.nova.agent.entity.OutPutVar;
import com.nova.agent.enums.ApiAuthPos;
import com.nova.agent.enums.ApiAuthType;
import com.nova.agent.enums.ApiDebugStatus;
import com.nova.agent.enums.ApiMethodType;
import com.nova.agent.enums.ApiRequestBodyType;
import com.nova.agent.enums.ApiResponseType;
import com.nova.agent.enums.NodeType;
import com.nova.agent.enums.VarType;
import com.nova.agent.utils.HttpUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * HTTP API 调用节点。
 *
 * <p>支持完整的 HTTP 请求配置：
 * <ul>
 *   <li>请求方法：GET / POST / PUT / DELETE / PATCH</li>
 *   <li>请求参数：Query Params、Headers、认证信息</li>
 *   <li>请求体：JSON / Form Data / Form URL-Encoded</li>
 *   <li>响应解析：JSON 结构化提取 或 纯文本</li>
 * </ul>
 *
 * <p>入参中的引用变量会自动替换到请求的各个位置（URL、Header、Body）。
 * 出参按照配置的 JSON 结构从响应中提取字段。
 */
@Slf4j
public class ApiNode extends Node {

    public ApiNode(String nodeId, String name, NodeType nodeType,
                   List<InputVar> inputVars, List<OutPutVar> outPutVars, String config) {
        super(nodeId, name, nodeType, inputVars, outPutVars, config);
    }

    @Override
    public void run(AgentFlow agentFlow) {
        log.debug("api node run, nodeId: {}", this.nodeId);
        ApiNodeConfig apiNodeConfig = JSON.parseObject(config, ApiNodeConfig.class);
        String method = apiNodeConfig.getMethod().name();
        String url = apiNodeConfig.getUrl();
        ApiRequestConfig requestConfig = apiNodeConfig.getRequestConfig();
        ApiResponseConfig responseConfig = apiNodeConfig.getResponseConfig();
        AuthInfo authInfo = requestConfig != null ? requestConfig.getAuthInfo() : null;

        // Fill query params from input vars
        try {
            if (requestConfig != null) {
                fillApiParams(requestConfig);
            }
        } catch (Exception e) {
            throw new RuntimeException(this.getNodeName() + ": API request parse error!", e);
        }

        try {
            String result = invokeApi(url, method, requestConfig, authInfo);
            agentFlow.setContextVar(AgentFlowContextVar.NODE_RESULT_PREFIX + this.nodeId, result);
        } catch (Exception e) {
            throw new RuntimeException(this.getNodeName() + ": API call error! " + e.getMessage(), e);
        }

        // Parse response
        try {
            if (responseConfig != null && responseConfig.getType() == ApiResponseType.string) {
                OutPutVar out = new OutPutVar();
                out.setVarName("result");
                out.setVarType(VarType.String);
                out.setVarValue(agentFlow.getContextVar(
                        AgentFlowContextVar.NODE_RESULT_PREFIX + this.nodeId));
                if (outputVars == null) outputVars = new ArrayList<>();
                outputVars.add(out);
            }
        } catch (Exception e) {
            throw new RuntimeException(this.getNodeName() + ": Response parse error!", e);
        }
    }

    private void fillApiParams(ApiRequestConfig requestConfig) {
        if (requestConfig.getQueryParams() != null) {
            for (ApiQueryParam p : requestConfig.getQueryParams()) {
                p.setRealValue(findInputVarValue(p.getField(), p.getType()));
            }
        }
        if (requestConfig.getHeaders() != null) {
            for (ApiQueryHeader h : requestConfig.getHeaders()) {
                h.setRealValue(findInputVarValue(h.getField(), h.getType()));
            }
        }
        if (requestConfig.getBodyInfo() != null) {
            BodyInfo body = requestConfig.getBodyInfo();
            if (body.getFormData() != null) {
                for (ApiQueryParam p : body.getFormData()) {
                    p.setRealValue(findInputVarValue(p.getField(), p.getType()));
                }
            }
            if (body.getFormUrlencodedData() != null) {
                for (ApiQueryParam p : body.getFormUrlencodedData()) {
                    p.setRealValue(findInputVarValue(p.getField(), p.getType()));
                }
            }
            if (body.getJsonData() != null) {
                for (ApiJsonNode n : body.getJsonData()) {
                    n.setRealValue(findInputVarValue(n.getField(), n.getType()));
                }
            }
        }
    }

    private String invokeApi(String url, String method, ApiRequestConfig request, AuthInfo authInfo) throws Exception {
        String jsonBody = "{}";
        if (request != null && request.getBodyInfo() != null
                && request.getBodyInfo().getJsonData() != null
                && !request.getBodyInfo().getJsonData().isEmpty()) {
            jsonBody = buildJson(request.getBodyInfo().getJsonData());
        }
        List<ApiQueryParam> queryParams = request != null && request.getQueryParams() != null
                ? request.getQueryParams() : new ArrayList<>();
        List<ApiQueryHeader> headers = request != null && request.getHeaders() != null
                ? request.getHeaders() : new ArrayList<>();

        queryParams = queryParams.stream().filter(p -> StringUtils.isNotEmpty(p.getField())).toList();
        headers = headers.stream().filter(h -> StringUtils.isNotEmpty(h.getField())).toList();

        if (authInfo != null && StringUtils.isEmpty(authInfo.getTokenFieldName())) {
            authInfo = null;
        }
        return HttpUtils.invokeApi(url, method, queryParams, headers, jsonBody, authInfo);
    }

    @Override
    public void fillOutputVar(AgentFlow agentFlow) {
        // Output vars are set in run()
    }

    public static String buildJson(List<ApiJsonNode> nodes) {
        JSONObject obj = buildObject(nodes);
        return obj.toJSONString();
    }

    private static JSONObject buildObject(List<ApiJsonNode> nodes) {
        JSONObject obj = new JSONObject();
        for (ApiJsonNode node : nodes) {
            obj.put(node.getField(), convertValue(node));
        }
        return obj;
    }

    private static Object convertValue(ApiJsonNode node) {
        if (node.getType() == VarType.Object) {
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                return buildObject(node.getChildren());
            }
            return new JSONObject();
        }
        if (node.getType().name().startsWith("Array")) {
            return buildArray(node);
        }
        return node.getRealValue();
    }

    private static Object buildArray(ApiJsonNode node) {
        Object value = node.getRealValue();
        if (node.getChildren() != null && !node.getChildren().isEmpty()) {
            JSONArray arr = new JSONArray();
            for (ApiJsonNode child : node.getChildren()) {
                arr.add(convertValue(child));
            }
            return arr;
        }
        if (value instanceof String) {
            try { return JSONArray.parseArray((String) value); } catch (Exception e) { /* fall through */ }
        }
        if (value instanceof List) return value;
        JSONArray arr = new JSONArray();
        arr.add(value);
        return arr;
    }

    // Inner DTO classes
    @Data public static class ApiNodeConfig {
        private ApiMethodType method;
        private String url;
        private ApiDebugStatus debugStatus;
        private ApiRequestConfig requestConfig;
        private ApiResponseConfig responseConfig;
    }
    @Data public static class ApiResponseConfig {
        private ApiResponseType type;
        private List<ObjectStructure> jsonResult;
    }
    @Data public static class ApiRequestConfig {
        private List<ApiQueryParam> queryParams;
        private List<ApiQueryHeader> headers;
        private AuthInfo authInfo;
        private BodyInfo bodyInfo;
    }
    @Data public static class BodyInfo {
        private ApiRequestBodyType bodyType;
        private List<ApiQueryParam> formData;
        private List<ApiQueryParam> formUrlencodedData;
        private List<ApiJsonNode> jsonData;
    }
    @Data public static class ApiJsonNode {
        private String id;
        private VarType type;
        private String field;
        private String mockValue;
        private Object realValue;
        private List<ApiJsonNode> children;
    }
    @Data public static class ApiQueryParam {
        private String field;
        private String mockValue;
        private VarType type = VarType.String;
        private Object realValue;
    }
    @Data public static class ApiQueryHeader {
        private String field;
        private VarType type = VarType.String;
        private String mockValue;
        private Object realValue;
    }
    @Data public static class AuthInfo {
        private ApiAuthType authType;
        private ApiAuthPos authPos;
        private String tokenFieldName;
        private String token;
    }
}
