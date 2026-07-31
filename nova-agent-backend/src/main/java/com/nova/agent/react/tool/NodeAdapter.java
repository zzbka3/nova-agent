package com.nova.agent.react.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nova.agent.constant.AgentFlowContextVar;
import com.nova.agent.entity.AgentFlow;
import com.nova.agent.entity.InputVar;
import com.nova.agent.entity.OutPutVar;
import com.nova.agent.entity.UserInvokeInput;
import com.nova.agent.entity.graph.*;
import com.nova.agent.enums.NodeType;
import com.nova.agent.enums.VarType;
import com.nova.agent.model.dto.CurrentUser;
import com.nova.agent.react.entity.ExecutionResult;
import com.nova.agent.react.entity.InputMapping;
import com.nova.agent.react.entity.OutputMapping;
import com.nova.agent.react.enums.ReActSignal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Adapts Tool invocations to existing Node executions.
 * Creates a minimal standalone context for each Node, maps Tool parameters
 * to Node input variables, and extracts outputs via outputMapping rules.
 */
@Slf4j
@Component
public class NodeAdapter {

    private static final String DUMMY_DSL = "{\"nodes\":[],\"edges\":[]}";

    /**
     * Execute a Tool backed by a Node in standalone mode.
     *
     * @param toolDefinition   the tool definition with mapping rules
     * @param toolArguments    the arguments from Planner (keyed by input schema name)
     * @param userInvokeInput  the user invocation context (for logging)
     * @return standardized ExecutionResult
     */
    public ExecutionResult execute(ToolDefinition toolDefinition,
                                    Map<String, Object> toolArguments,
                                    UserInvokeInput userInvokeInput) {
        long start = System.currentTimeMillis();
        String toolId = toolDefinition.getToolId();
        String toolName = toolDefinition.getName();
        log.info("NodeAdapter execute start: toolId={}, toolName={}, nodeConfig={}",
                toolId, toolName, toolDefinition.getExecutorResourceId());

        try {
            // 1. Parse node config JSON to determine node type
            String nodeConfigJson = toolDefinition.getExecutorResourceId();
            JSONObject nodeConfig = JSON.parseObject(nodeConfigJson);
            String nodeId = nodeConfig.getString("id");
            String nodeTypeStr = nodeConfig.getString("type");
            String propertiesStr = nodeConfig.getString("properties");
            JSONObject properties = JSON.parseObject(propertiesStr);
            String nodeName = properties.getString("nodeName");
            NodeType nodeType = NodeType.valueOf(nodeTypeStr);

            // 2. Parse input/output variant lists from properties
            List<InputVar> inputVars = parseInputVars(properties);
            List<OutPutVar> outputVars = parseOutputVars(properties);

            // 3. Apply inputMapping: toolArguments -> Node inputVar values
            List<InputMapping> inputMappings = parseMappings(
                    toolDefinition.getInputMapping(), InputMapping.class);
            applyInputMapping(inputVars, inputMappings, toolArguments);

            // 4. Create minimal AgentFlow context
            AgentFlow agentFlow = createStandaloneContext(userInvokeInput);

            // 5. Instantiate the matching Node subclass and wire inputs
            Node node = createNode(nodeId, nodeName, nodeType, inputVars, outputVars, propertiesStr);
            if (node == null) {
                return buildErrorResult(toolId, nodeTypeStr,
                        "Unsupported node type for Tool adapter: " + nodeTypeStr, start);
            }

            // 6. Execute the node
            node.execute(agentFlow);
            if (node.getStatus() != null
                    && node.getStatus() == com.nova.agent.enums.NodeStatus.EXCEPTION) {
                return buildErrorResult(toolId, nodeName,
                        node.getExceptionMsg() != null ? node.getExceptionMsg() : "Node execution failed", start);
            }

            // 7. Apply outputMapping: node outputVars -> structured outputs
            List<OutputMapping> outputMappings = parseMappings(
                    toolDefinition.getOutputMapping(), OutputMapping.class);
            Map<String, Object> outputs = applyOutputMapping(outputVars, outputMappings);

            // 8. Build success result
            long cost = System.currentTimeMillis() - start;
            log.info("NodeAdapter execute success: toolId={}, cost={}ms, outputs={}",
                    toolId, cost, outputs.keySet());

            return ExecutionResult.builder()
                    .success(true)
                    .sourceType("TOOL")
                    .sourceId(toolId)
                    .summary(toolName + " executed successfully")
                    .outputs(outputs)
                    .signal(ReActSignal.CONTINUE)
                    .usedTokens(node.getUseTokens())
                    .costTime(cost)
                    .build();

        } catch (Exception e) {
            log.error("NodeAdapter execute error: toolId={}", toolId, e);
            return buildErrorResult(toolId, toolDefinition.getName(),
                    e.getMessage() != null ? e.getMessage() : "Unknown error", start);
        }
    }

    /**
     * Apply memory mapping to write outputs into agent state memory.
     */
    public void applyMemoryMapping(Map<String, Object> outputs,
                                    String memoryMappingJson,
                                    Map<String, Object> memory) {
        if (memoryMappingJson == null || memoryMappingJson.isEmpty()) return;
        List<com.nova.agent.react.entity.MemoryMapping> mappings =
                parseMappings(memoryMappingJson, com.nova.agent.react.entity.MemoryMapping.class);
        for (com.nova.agent.react.entity.MemoryMapping mapping : mappings) {
            String source = mapping.getSource();
            String target = mapping.getTarget();
            // source format: "outputs.logisticsStatus" -> extract from outputs
            // target format: "memory.logisticsStatus" -> write to memory
            if (source.startsWith("outputs.")) {
                String key = source.substring("outputs.".length());
                if (outputs.containsKey(key)) {
                    String memKey = target.startsWith("memory.") ? target.substring("memory.".length()) : target;
                    memory.put(memKey, outputs.get(key));
                }
            }
        }
    }

    // ---- private helpers ----

    private List<InputVar> parseInputVars(JSONObject properties) {
        if (!properties.containsKey("inputVars")) return new ArrayList<>();
        return properties.getJSONArray("inputVars").toJavaList(InputVar.class);
    }

    private List<OutPutVar> parseOutputVars(JSONObject properties) {
        if (!properties.containsKey("outputVars")) return new ArrayList<>();
        return properties.getJSONArray("outputVars").toJavaList(OutPutVar.class);
    }

    private <T> List<T> parseMappings(String mappingJson, Class<T> clazz) {
        if (mappingJson == null || mappingJson.isEmpty()) return new ArrayList<>();
        return JSON.parseArray(mappingJson).toJavaList(clazz);
    }

    /**
     * Apply input mapping: for each mapping rule, read source from toolArguments
     * and set it as the target inputVar's value.
     *
     * Source format: "inputs.orderId" means toolArguments.get("orderId")
     * Target format: "node.params.orderId" means inputVars where varName == "orderId"
     */
    private void applyInputMapping(List<InputVar> inputVars,
                                    List<InputMapping> mappings,
                                    Map<String, Object> toolArguments) {
        for (InputMapping mapping : mappings) {
            String source = mapping.getSource();
            String target = mapping.getTarget();
            if (source.startsWith("inputs.")) {
                String inputKey = source.substring("inputs.".length());
                if (toolArguments.containsKey(inputKey)) {
                    Object value = toolArguments.get(inputKey);
                    String targetVarName = extractVarName(target);
                    for (InputVar var : inputVars) {
                        if (var.getVarName().equals(targetVarName)) {
                            // Set as fixed value (not reference), so fillInputVar() skips it
                            if (var.getVarType() == VarType.reference) {
                                var.setVarType(var.getReferenceVarType() != null
                                        ? var.getReferenceVarType() : VarType.String);
                            }
                            var.setVarValue(value);
                            log.debug("Input mapping: {} ({}) -> {}", inputKey, value, targetVarName);
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Extract variable name from mapping target like "node.params.orderId" -> "orderId".
     */
    private String extractVarName(String target) {
        if (target == null) return target;
        if (target.startsWith("node.params.")) return target.substring("node.params.".length());
        return target;
    }

    /**
     * Apply output mapping: read values from Node output variables and put them
     * into a structured outputs map.
     *
     * Source format: "result.status" -> read from outputVar named "status"
     * Target format: "outputs.logisticsStatus" -> write key "logisticsStatus"
     */
    private Map<String, Object> applyOutputMapping(List<OutPutVar> outputVars,
                                                    List<OutputMapping> mappings) {
        Map<String, Object> outputs = new LinkedHashMap<>();
        Map<String, Object> varMap = outputVars.stream()
                .filter(v -> v.getVarValue() != null)
                .collect(Collectors.toMap(OutPutVar::getVarName, OutPutVar::getVarValue, (a, b) -> b, LinkedHashMap::new));

        for (OutputMapping mapping : mappings) {
            String source = mapping.getSource();
            String target = mapping.getTarget();
            String sourceKey = source.startsWith("result.") ? source.substring("result.".length()) : source;
            String targetKey = target.startsWith("outputs.") ? target.substring("outputs.".length()) : target;

            if (varMap.containsKey(sourceKey)) {
                outputs.put(targetKey, varMap.get(sourceKey));
            } else {
                // Try direct field extraction from underlying result JSON
                // This handles cases where the full LLM response is stored
                log.debug("outputMapping: sourceKey {} not in direct outputs, using all outputs", sourceKey);
                outputs.putAll(varMap);
            }
        }

        // If no mappings defined, return all output vars
        if (mappings.isEmpty() && !varMap.isEmpty()) {
            outputs.putAll(varMap);
        }

        return outputs;
    }

    private AgentFlow createStandaloneContext(UserInvokeInput userInvokeInput) {
        AgentFlow agentFlow = new AgentFlow(DUMMY_DSL);
        agentFlow.setContextVar(AgentFlowContextVar.USER_INVOKE_INPUT, userInvokeInput);
        // Set a dummy current user for nodes that need it
        CurrentUser dummyUser = new CurrentUser();
        dummyUser.setAccountId(0L);
        agentFlow.setContextVar(AgentFlowContextVar.CURRENT_ACCOUNT, dummyUser);
        return agentFlow;
    }

    /**
     * Create a Node instance based on type. Only supports API, CODE, KNOWLEDGE for phase 1.
     */
    private Node createNode(String nodeId, String name, NodeType type,
                            List<InputVar> inputVars, List<OutPutVar> outputVars,
                            String config) {
        return switch (type) {
            case API -> new ApiNode(nodeId, name, type, inputVars, outputVars, config);
            case CODE -> new CodeNode(nodeId, name, type, inputVars, outputVars, config);
            case KNOWLEDGE -> new KnowledgeNode(nodeId, name, type, inputVars, outputVars, config);
            case LLM -> new LLMNode(nodeId, name, type, inputVars, outputVars, config);
            default -> {
                log.warn("Node type {} not yet supported as standalone Tool", type);
                yield null;
            }
        };
    }

    private ExecutionResult buildErrorResult(String sourceId, String summary, String message, long start) {
        return ExecutionResult.builder()
                .success(false)
                .sourceType("TOOL")
                .sourceId(sourceId)
                .summary(summary)
                .signal(ReActSignal.ERROR)
                .message(message)
                .costTime(System.currentTimeMillis() - start)
                .build();
    }
}
