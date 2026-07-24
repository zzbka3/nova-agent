package com.nova.agent.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nova.agent.constant.AgentFlowContextVar;
import com.nova.agent.entity.graph.*;
import com.nova.agent.enums.AgentRunningStatus;
import com.nova.agent.enums.IfNodeOpType;
import com.nova.agent.enums.NodeStatus;
import com.nova.agent.enums.NodeType;
import com.nova.agent.enums.VarType;
import com.nova.agent.exception.AgentFlowConstructException;
import com.nova.agent.model.po.AgentEdgeExecuteLog;
import com.nova.agent.service.AgentFlowService;
import com.nova.agent.utils.ConditionUtils;
import com.nova.agent.utils.SpringContextUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DirectedMultigraph;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Data
@Slf4j
public class AgentFlow {

    private String flowId;
    private DirectedMultigraph<Node, Edge> graph;
    private Map<String, Object> context;
    private Map<String, AtomicInteger> dependenciesCount;
    private Map<String, Node> nodeMap;
    private CountDownLatch latch;
    private volatile AgentRunningStatus status = AgentRunningStatus.INIT;
    private String exceptionMsg;
    private Map<String, Integer> nodeRunStatus = new ConcurrentHashMap<>();

    public AgentFlow(String dsl) {
        this.graph = new DirectedMultigraph<>(Edge.class);
        this.dependenciesCount = new ConcurrentHashMap<>();
        this.nodeMap = new ConcurrentHashMap<>();
        this.context = new ConcurrentHashMap<>();
        this.latch = new CountDownLatch(1);

        try {
            JSONObject dslJson = JSON.parseObject(dsl);

            // Parse nodes
            JSONArray vertexArray = dslJson.getJSONArray("nodes");
            for (int i = 0; i < vertexArray.size(); i++) {
                JSONObject vertex = vertexArray.getJSONObject(i);
                String id = vertex.getString("id");
                String nodeType = vertex.getString("type");
                String config = vertex.getString("properties");
                JSONObject properties = JSON.parseObject(config);
                String name = properties.getString("nodeName");

                List<InputVar> inputVars = new CopyOnWriteArrayList<>();
                List<OutPutVar> outputVars = new CopyOnWriteArrayList<>();
                if (properties.containsKey("inputVars")) {
                    inputVars = new CopyOnWriteArrayList<>(
                            properties.getJSONArray("inputVars").toJavaList(InputVar.class));
                }
                if (properties.containsKey("outputVars")) {
                    outputVars = new CopyOnWriteArrayList<>(
                            properties.getJSONArray("outputVars").toJavaList(OutPutVar.class));
                }

                NodeType nodeTypeEnum = NodeType.valueOf(nodeType);
                switch (nodeTypeEnum) {
                    case LLM -> addNode(id, new LLMNode(id, name, nodeTypeEnum, inputVars, outputVars, config));
                    case START -> addNode(id, new StartNode(id, name, nodeTypeEnum, inputVars, outputVars, config));
                    case END -> addNode(id, new EndNode(id, name, nodeTypeEnum, inputVars, outputVars, config));
                    case IF -> addNode(id, new IfNode(id, name, nodeTypeEnum, inputVars, outputVars, config));
                    case MESSAGE -> addNode(id, new MessageNode(id, name, nodeTypeEnum, inputVars, outputVars, config));
                    case API -> addNode(id, new ApiNode(id, name, nodeTypeEnum, inputVars, outputVars, config));
                    case INTENT -> addNode(id, new IntentNode(id, name, nodeTypeEnum, inputVars, outputVars, config));
                    case KNOWLEDGE -> addNode(id, new KnowledgeNode(id, name, nodeTypeEnum, inputVars, outputVars, config));
                    case WORKFLOW -> addNode(id, new WorkflowNode(id, name, nodeTypeEnum, inputVars, outputVars, config));
                    case MCP -> addNode(id, new McpNode(id, name, nodeTypeEnum, inputVars, outputVars, config));
                    case CODE -> addNode(id, new CodeNode(id, name, nodeTypeEnum, inputVars, outputVars, config));
                    case REWRITE -> addNode(id, new QueryRewriteNode(id, name, nodeTypeEnum, inputVars, outputVars, config));
                    case TEXT_PROCESSOR -> addNode(id, new TextProcessorNode(id, name, nodeTypeEnum, inputVars, outputVars, config));
                    case MEMORY -> addNode(id, new MemoryNode(id, name, nodeTypeEnum, inputVars, outputVars, config));
                    case WORKFLOW_AGENT -> addNode(id, new WorkflowAgentNode(id, name, nodeTypeEnum, inputVars, outputVars, config));
                }
            }

            // Parse edges
            JSONArray edgeArray = dslJson.getJSONArray("edges");
            for (int i = 0; i < edgeArray.size(); i++) {
                JSONObject edge = edgeArray.getJSONObject(i);
                String id = edge.getString("id");
                String sourceNodeId = edge.getString("sourceNodeId");
                String targetNodeId = edge.getString("targetNodeId");
                addEdge(sourceNodeId, targetNodeId, new Edge(id, sourceNodeId, targetNodeId));
            }
        } catch (Exception e) {
            log.error("agent flow construct error!", e);
            throw new AgentFlowConstructException("Agent flow construction error!", e);
        }
    }

    /**
     * Check if the graph is valid (no cycles)
     */
    public boolean checkGraph() {
        CycleDetector<Node, Edge> cycleDetector = new CycleDetector<>(graph);
        boolean hasCycle = cycleDetector.detectCycles();
        if (hasCycle) {
            log.warn("agent flow {} has cycle!", flowId);
        }
        return !hasCycle;
    }

    /**
     * Evaluate a single condition between two variables
     */
    public boolean evalCondition(InputVar left, InputVar right, IfNodeOpType op) {
        return switch (op) {
            case CONTAINS -> ConditionUtils.contains(left, right);
            case NOT_CONTAINS -> !ConditionUtils.contains(left, right);
            case EQUAL -> ConditionUtils.equal(left, right);
            case NOT_EQUAL -> !ConditionUtils.equal(left, right);
            case GT_EQUAL -> ConditionUtils.gte(left, right);
            case GT -> ConditionUtils.gt(left, right);
            case LT_EQUAL -> !ConditionUtils.gt(left, right);
            case LT -> !ConditionUtils.gte(left, right);
            case LENGTH_GT_EQUAL -> ConditionUtils.lengthGte(left, right);
            case LENGTH_GT -> ConditionUtils.lengthGt(left, right);
            case LENGTH_LT_EQUAL -> !ConditionUtils.lengthGt(left, right);
            case LENGTH_LT -> !ConditionUtils.lengthGte(left, right);
            case EMPTY -> ConditionUtils.empty(left);
            case NOT_EMPTY -> !ConditionUtils.empty(left);
            case IS_TRUE -> ConditionUtils.isTrue(left);
            case IS_FALSE -> !ConditionUtils.isTrue(left);
        };
    }

    /**
     * Fire the workflow execution from START node
     */
    public AgentFlowOutput fire(UserInvokeInput userInvokeInput) {
        String appId = userInvokeInput.getApp_id();
        long startTime = System.currentTimeMillis();

        try {
            AgentFlowService agentFlowService = SpringContextUtils.getBean("agentFlowService", AgentFlowService.class);
            agentFlowService.updateAgentFlowStatus(userInvokeInput.getExecuteId(), AgentRunningStatus.RUNNING, "");

            setContextVar(AgentFlowContextVar.USER_INVOKE_INPUT, userInvokeInput);
            Node start = getGraph().vertexSet().stream()
                    .filter(item -> item.getNodeType().equals(NodeType.START))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No START node found"));

            SpringContextUtils.getBean("agentExecutor", org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor.class);
            var executor = SpringContextUtils.getBean("agentExecutor",
                    org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor.class);

            if (userInvokeInput.getDebug() != null && userInvokeInput.getDebug() == 1) {
                executor = SpringContextUtils.getBean("debugExecutor",
                        org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor.class);
            }

            triggerNode(start, executor);

            try {
                boolean completed = getLatch().await(3, TimeUnit.MINUTES);
                if (!completed) {
                    log.warn("Timeout waiting for latch, flowId={}", flowId);
                    this.status = AgentRunningStatus.EXCEPTION;
                    this.exceptionMsg = "Execution timeout";
                }
            } catch (Exception e) {
                log.error("latch wait error!", e);
            }

            if (status.equals(AgentRunningStatus.EXCEPTION)) {
                agentFlowService.updateAgentFlowStatus(userInvokeInput.getExecuteId(),
                        AgentRunningStatus.EXCEPTION, exceptionMsg);
                throw new RuntimeException(exceptionMsg);
            } else {
                agentFlowService.updateAgentFlowStatus(userInvokeInput.getExecuteId(),
                        AgentRunningStatus.FINISH, "");
                return (AgentFlowOutput) getContextVar(AgentFlowContextVar.AGENT_FLOW_OUTPUT);
            }
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("AgentFlow fire finished, flowId={}, duration={}ms", flowId, duration);
        }
    }

    /**
     * Trigger execution from a node (recursively fires downstream)
     */
    public void triggerNode(Node node, org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor executor) {
        if (hasRun(node)) {
            log.warn("node {} already run", node.getNodeId());
            return;
        }
        try {
            executor.submit(() -> {
                try {
                    if (!status.equals(AgentRunningStatus.INIT) && !status.equals(AgentRunningStatus.RUNNING)) {
                        log.info("triggerNode {} : status {}, return", node.getNodeId(), status);
                        getLatch().countDown();
                        return;
                    }

                    node.setStatus(NodeStatus.REACH);
                    node.execute(this);

                    if (node.isEnd() || status.equals(AgentRunningStatus.EXCEPTION)) {
                        getLatch().countDown();
                        return;
                    }

                    Set<Edge> edges = graph.outgoingEdgesOf(node);
                    if (edges.isEmpty()) return;

                    for (Edge edge : edges) {
                        edge.conditionMatch();
                    }

                    Map<String, List<Edge>> edgeGroup = edges.stream()
                            .collect(Collectors.groupingBy(e -> e.getGroup() != null ? e.getGroup() : ""));
                    for (List<Edge> edgeList : edgeGroup.values()) {
                        Edge matchEdge = null;
                        for (Edge edge : edgeList) {
                            if (edge.getConditionMatch() == 1) {
                                matchEdge = edge;
                                break;
                            }
                        }
                        if (matchEdge != null) {
                            updateEdgeToDb(matchEdge, 1);
                            Node target = getGraph().getEdgeTarget(matchEdge);
                            propagate(node, target, new HashSet<>(), executor);
                        } else {
                            Edge edge = edgeList.get(0);
                            Node target = getGraph().getEdgeTarget(edge);
                            propagate(node, target, new HashSet<>(), executor);
                        }
                    }
                } catch (Exception e) {
                    getLatch().countDown();
                    this.setStatus(AgentRunningStatus.EXCEPTION);
                    this.exceptionMsg = "Flow execution error: " + e.getMessage();
                    log.error("triggerNode error! node=%s, nodeId=%s".formatted(node.getNodeName(), node.getNodeId()), e);
                }
            });
        } catch (RejectedExecutionException e) {
            log.warn("Thread pool full, node rejected, nodeId={}", node.getNodeId());
            getLatch().countDown();
            this.setStatus(AgentRunningStatus.EXCEPTION);
            this.exceptionMsg = "System busy, please try again later";
        }
    }

    private void propagate(Node source, Node node, HashSet<Node> visited,
                           org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor executor) {
        if (visited.contains(node)) return;
        visited.add(node);
        if (canNodeRun(node)) {
            log.info("propagate: {} => {}", source.getNodeId(), node.getNodeId());
            triggerNode(node, executor);
        }
        for (Edge outgoing : graph.outgoingEdgesOf(node)) {
            Node nextNode = graph.getEdgeTarget(outgoing);
            propagate(source, nextNode, visited, executor);
        }
    }

    private boolean canNodeRun(Node node) {
        synchronized (node) {
            Set<Edge> edges = graph.incomingEdgesOf(node);
            if (edges.stream().noneMatch(e -> e.getConditionMatch() == 1)) {
                return false;
            }

            Map<String, List<Edge>> edgeGroup = edges.stream()
                    .collect(Collectors.groupingBy(e -> e.getGroup() != null ? e.getGroup() : ""));
            List<List<Edge>> noReachEdges = new ArrayList<>();
            boolean hasSuccess = false;

            for (Map.Entry<String, List<Edge>> entry : edgeGroup.entrySet()) {
                List<Edge> edgeList = entry.getValue();
                boolean hasNoReach = false;
                for (Edge edge : edgeList) {
                    if (edge.getConditionMatch() == -1) hasNoReach = true;
                    if (edge.getConditionMatch() == 1) hasSuccess = true;
                }
                if (hasNoReach) noReachEdges.add(edgeList);
            }

            if (!hasSuccess && noReachEdges.isEmpty()) return false;
            if (!noReachEdges.isEmpty()) {
                for (List<Edge> edgeList : noReachEdges) {
                    Node sourceNode = getGraph().getEdgeSource(edgeList.get(0));
                    if (possibleAccess(sourceNode)) return false;
                }
            }
            return true;
        }
    }

    private boolean possibleAccess(Node node) {
        Set<Edge> edges = graph.incomingEdgesOf(node);
        Map<String, List<Edge>> edgeGroup = edges.stream()
                .collect(Collectors.groupingBy(e -> e.getGroup() != null ? e.getGroup() : ""));
        for (Map.Entry<String, List<Edge>> entry : edgeGroup.entrySet()) {
            List<Edge> edgeList = entry.getValue();
            boolean hasNoReach = false, hasSuccess = false;
            for (Edge edge : edgeList) {
                if (edge.getConditionMatch() == 1) hasSuccess = true;
                if (edge.getConditionMatch() == -1) hasNoReach = true;
            }
            if (!hasSuccess && !hasNoReach) return false;
            if (hasSuccess) return true;
            Edge edge = entry.getValue().get(0);
            Node prevNode = getGraph().getEdgeSource(edge);
            if (possibleAccess(prevNode)) return true;
        }
        return false;
    }

    private void updateEdgeToDb(Edge edge, int match) {
        try {
            AgentFlowService agentFlowService = SpringContextUtils.getBean("agentFlowService", AgentFlowService.class);
            UserInvokeInput userInvokeInput = (UserInvokeInput) getContextVar(AgentFlowContextVar.USER_INVOKE_INPUT);
            AgentEdgeExecuteLog executeLog = new AgentEdgeExecuteLog();
            executeLog.setAppId(userInvokeInput.getApp_id());
            executeLog.setConversationId(userInvokeInput.getConversation_id());
            executeLog.setExecuteId(userInvokeInput.getExecuteId());
            executeLog.setEdgeId(edge.getId());
            executeLog.setFromNodeId(edge.getFromNodeId());
            executeLog.setTargetNodeId(edge.getTargetNodeId());
            executeLog.setConditionMatch(match);
            executeLog.setCreator("");
            executeLog.setCreateTime(new Date());
            executeLog.setUpdater("");
            executeLog.setUpdateTime(new Date());
            agentFlowService.updateEdgeToDb(executeLog);
        } catch (Exception e) {
            log.error("updateEdgeToDb error!", e);
        }
    }

    /**
     * Fill an input variable from context (resolve references)
     */
    public void fillInputVar(InputVar var) {
        if (var == null) return;
        if (!var.getVarType().equals(VarType.reference)
                || StringUtils.isEmpty(var.getReferenceNodeId())
                || StringUtils.isEmpty(var.getReferenceVarName())
                || var.getReferenceVarType() == null) {
            return;
        }
        var.setVarValue(null);
        String refNodeId = var.getReferenceNodeId();
        String refVarName = var.getReferenceVarName();
        VarType refVarType = var.getReferenceVarType();

        Node refNode = nodeMap.get(refNodeId);
        if (refNode == null) return;
        List<OutPutVar> outPutVars = refNode.getOutputVars();
        if (outPutVars == null) return;

        boolean multiLevel = refVarName.contains(".");
        if (!multiLevel) {
            for (OutPutVar outVar : outPutVars) {
                if (outVar.getVarName().equals(refVarName) && outVar.getVarType().equals(refVarType)) {
                    var.setVarValue(outVar.getVarValue());
                    return;
                }
            }
        } else {
            List<String> parts = Arrays.asList(refVarName.split("\\."));
            OutPutVar firstLevel = null;
            for (OutPutVar outVar : outPutVars) {
                if (outVar.getVarName().equals(parts.get(0))
                        && (outVar.getVarType().equals(VarType.Object)
                        || outVar.getVarType().equals(VarType.ArrayObject))) {
                    firstLevel = outVar;
                    break;
                }
            }
            if (firstLevel == null) return;
            var.setVarValue(extractValue(firstLevel, refVarName, refVarType));
        }
    }

    public static Object extractValue(OutPutVar var, String path, VarType refVarType) {
        if (var == null || path == null || path.isEmpty()) return null;
        String[] parts = path.split("\\.");
        Object value = null;
        if (var.getVarType() == VarType.Object) {
            value = JSON.parseObject(JSON.toJSONString(var.getVarValue()), Object.class);
        } else if (var.getVarType() == VarType.ArrayObject) {
            value = JSON.parseArray(JSON.toJSONString(var.getVarValue()));
        } else {
            return null;
        }
        return extractRecursive(value, var.getStructure(), parts, 1, refVarType);
    }

    private static Object extractRecursive(Object value, ObjectStructure structure,
                                           String[] parts, int index, VarType refVarType) {
        if (value == null || structure == null) return null;
        if (index >= parts.length) {
            return structure.getType().equals(refVarType) ? value : null;
        }
        String part = parts[index];
        if (structure.getType() != VarType.Object && structure.getType() != VarType.ArrayObject) return null;

        List<ObjectStructure> children = structure.getChildren();
        if (children == null) return null;

        ObjectStructure nextStructure = null;
        for (ObjectStructure child : children) {
            if (part.equals(child.getField())) {
                nextStructure = child;
                break;
            }
        }

        Object nextValue;
        if (value instanceof JSONObject jo) {
            nextValue = jo.get(part);
        } else if (value instanceof JSONArray ja) {
            nextValue = ja.stream().map(o -> ((JSONObject) o).get(part))
                    .collect(Collectors.toCollection(JSONArray::new));
        } else {
            try {
                Field field = value.getClass().getDeclaredField(part);
                field.setAccessible(true);
                nextValue = field.get(value);
            } catch (Exception e) {
                return null;
            }
        }
        return extractRecursive(nextValue, nextStructure, parts, index + 1, refVarType);
    }

    private void addNode(String id, Node node) {
        graph.addVertex(node);
        nodeMap.put(id, node);
        dependenciesCount.put(id, new AtomicInteger(0));
    }

    private void addEdge(String fromId, String toId, Edge edge) {
        Node from = nodeMap.get(fromId);
        Node to = nodeMap.get(toId);
        if (from == null || to == null) {
            throw new AgentFlowConstructException("Edge references non-existent node: " + fromId + " -> " + toId);
        }
        graph.addEdge(from, to, edge);
        dependenciesCount.get(toId).incrementAndGet();
    }

    public Object getContextVar(String var) {
        return context.get(var);
    }

    public void setContextVar(String var, Object value) {
        context.put(var, value);
    }

    /**
     * Convert a string value to the target type
     */
    public Object covert(String result, VarType type) {
        try {
            return switch (type) {
                case String -> result;
                case Integer -> Integer.parseInt(result);
                case Number -> Double.parseDouble(result);
                case Boolean -> Boolean.parseBoolean(result);
                case ArrayString -> JSON.parseArray(result).toJavaList(String.class);
                case ArrayInteger -> JSON.parseArray(result).toJavaList(Integer.class);
                case ArrayNumber -> JSON.parseArray(result).toJavaList(Double.class);
                case ArrayBoolean -> JSON.parseArray(result).toJavaList(Boolean.class);
                default -> null;
            };
        } catch (Exception e) {
            log.error("covert var error!", e);
            throw new RuntimeException("Variable type conversion error!");
        }
    }

    public void updateEdge(Set<Edge> targetEdges, List<String> edgeIds, String condition, Integer conditionMatch) {
        for (Edge edge : targetEdges) {
            if (edgeIds.contains(edge.getId())) {
                edge.setCondition(condition);
                edge.setConditionMatch(conditionMatch);
            }
        }
    }

    public synchronized boolean hasRun(Node node) {
        boolean run = nodeRunStatus.containsKey(node.getNodeId());
        log.info("node {} has run: {}", node.getNodeId(), run);
        setRun(node.getNodeId());
        return run;
    }

    public synchronized void setRun(String nodeId) {
        nodeRunStatus.put(nodeId, 1);
    }
}
