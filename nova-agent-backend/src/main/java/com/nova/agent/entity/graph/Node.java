package com.nova.agent.entity.graph;

import com.alibaba.fastjson.JSON;
import com.nova.agent.constant.AgentFlowContextVar;
import com.nova.agent.entity.AgentFlow;
import com.nova.agent.entity.InputVar;
import com.nova.agent.entity.OutPutVar;
import com.nova.agent.entity.UserInvokeInput;
import com.nova.agent.enums.AgentRunningStatus;
import com.nova.agent.enums.NodeStatus;
import com.nova.agent.enums.NodeType;
import com.nova.agent.enums.VarType;
import com.nova.agent.model.po.AgentNodeExecuteLog;
import com.nova.agent.service.AgentFlowService;
import com.nova.agent.utils.SpringContextUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Data
@Slf4j
public abstract class Node {
    /** Node ID */
    public String nodeId;
    /** Node name */
    public String nodeName;
    /** Node type */
    public NodeType nodeType;
    /** Node input variables */
    public List<InputVar> inputVars;
    /** Node output variables */
    public List<OutPutVar> outputVars;
    /** Node configuration JSON string */
    public String config;
    /** Node execution status */
    public NodeStatus status = NodeStatus.INIT;
    /** Exception message */
    private String exceptionMsg;
    /** Used tokens count */
    public Integer useTokens = 0;

    public Node() {}

    public Node(String nodeId, String name, NodeType nodeType, List<InputVar> inputVars,
                List<OutPutVar> outPutVars, String config) {
        this.nodeId = nodeId;
        this.nodeName = name;
        this.nodeType = nodeType;
        this.config = config;
        this.inputVars = inputVars;
        this.outputVars = outPutVars;
    }

    /**
     * Execute this node within a workflow context
     */
    public void execute(AgentFlow agentFlow) {
        long start = System.currentTimeMillis();
        UserInvokeInput invokeInput = (UserInvokeInput) agentFlow.getContextVar(
                AgentFlowContextVar.USER_INVOKE_INPUT);
        log.info("node execute start! conversationId: {}, requestId: {}, node: {}, type: {}",
                invokeInput.getConversation_id(), invokeInput.getRequestId(),
                this.nodeId, this.nodeType);

        AgentFlowService agentFlowService = SpringContextUtils.getBean(
                "agentFlowService", AgentFlowService.class);

        AgentNodeExecuteLog nodeExecuteLog = new AgentNodeExecuteLog();
        nodeExecuteLog.setAppId(invokeInput.getApp_id());
        nodeExecuteLog.setConversationId(invokeInput.getConversation_id());
        nodeExecuteLog.setExecuteId(invokeInput.getExecuteId());
        nodeExecuteLog.setNodeId(this.nodeId);
        nodeExecuteLog.setNodeName(this.nodeName);
        nodeExecuteLog.setNodeType(this.nodeType.name());
        nodeExecuteLog.setStatus(NodeStatus.INIT.getStatus());

        try {
            this.setStatus(NodeStatus.RUNNING);
            // Fill input variables from context
            fillInputVar(agentFlow);
            // Record input for logging
            nodeExecuteLog.setInputVars(inputVars == null ? "[]" : JSON.toJSONString(inputVars));
            nodeExecuteLog.setOutputVars("[]");
            nodeExecuteLog.setStatus(NodeStatus.RUNNING.getStatus());
            agentFlowService.insertAgentNodeExecuteLog(nodeExecuteLog);

            // Execute node logic
            run(agentFlow);
            // Fill output variables to context
            fillOutputVar(agentFlow);
            this.setStatus(NodeStatus.FINISH);
        } catch (Exception e) {
            log.error("node execute exception! conversationId: {}, requestId: {}, node: {}, type: {}",
                    invokeInput.getConversation_id(), invokeInput.getRequestId(),
                    this.nodeId, this.nodeType, e);
            setStatus(NodeStatus.EXCEPTION);
            setExceptionMsg(e.getMessage());
            agentFlow.setStatus(AgentRunningStatus.EXCEPTION);
            agentFlow.setExceptionMsg(e.getMessage());
        } finally {
            try {
                if (nodeExecuteLog.getId() != null) {
                    nodeExecuteLog.setCostTime((int) (System.currentTimeMillis() - start));
                    nodeExecuteLog.setUsedTokens(useTokens);
                    nodeExecuteLog.setInputVars(inputVars == null ? "[]" : JSON.toJSONString(inputVars));
                    nodeExecuteLog.setStatus(status.getStatus());
                    nodeExecuteLog.setException(exceptionMsg);
                    nodeExecuteLog.setOutputVars(outputVars == null ? "[]" : JSON.toJSONString(outputVars));
                    nodeExecuteLog.setUpdater("");
                    nodeExecuteLog.setUpdateTime(new Date());
                    agentFlowService.updateAgentNodeExecuteLog(nodeExecuteLog);
                }
            } catch (Exception e) {
                setStatus(NodeStatus.EXCEPTION);
                setExceptionMsg(e.getMessage());
                agentFlow.setStatus(AgentRunningStatus.EXCEPTION);
                agentFlow.setExceptionMsg(e.getMessage());
                log.error("save node log exception!", e);
            }
        }
        log.info("node execute end! node: {}, type: {}, cost: {}ms",
                this.nodeId, this.nodeType, System.currentTimeMillis() - start);
    }

    /**
     * Fill input variables from flow context
     */
    protected void fillInputVar(AgentFlow agentFlow) {
        if (inputVars != null && !inputVars.isEmpty()) {
            for (InputVar var : inputVars) {
                agentFlow.fillInputVar(var);
            }
        }
    }

    /**
     * Core node execution logic - subclasses must implement
     */
    public abstract void run(AgentFlow agentFlow);

    /**
     * Fill output variables after execution - subclasses must implement
     */
    public abstract void fillOutputVar(AgentFlow agentFlow);

    /**
     * Is this an end node?
     */
    public boolean isEnd() {
        return false;
    }

    /**
     * Is this a start node?
     */
    public boolean isStart() {
        return false;
    }

    /**
     * Replace template variables in a string (e.g., {{varName}})
     */
    public String replaceVar(String input) {
        Map<String, Object> params = new HashMap<>();
        if (inputVars != null) {
            for (InputVar var : inputVars) {
                Object rawValue = var.getVarValue();
                String value = rawValue == null ? "" : String.valueOf(rawValue);
                if (value.contains("{{")) {
                    value = value.replaceAll("\\{\\{.*?}}", "");
                }
                params.put(var.getVarName(), value);
            }
        }
        StringSubstitutor sub = new StringSubstitutor(params, "{{", "}}");
        sub.setEnableUndefinedVariableException(false);
        String result = sub.replace(input);
        return result.replaceAll("\\{\\{.*?}}", "");
    }

    /**
     * Find an input variable value by name and type
     */
    public Object findInputVarValue(String paramName, VarType paramType) {
        if (inputVars == null) return null;
        for (InputVar inputVar : inputVars) {
            VarType varType = inputVar.getVarType().equals(VarType.reference)
                    ? inputVar.getReferenceVarType() : inputVar.getVarType();
            if (inputVar.getVarName().equals(paramName)
                    && (varType.equals(paramType) || paramType.equals(VarType.Any))) {
                return inputVar.getVarValue();
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Objects.equals(nodeId, node.nodeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId);
    }
}
