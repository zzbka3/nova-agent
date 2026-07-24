package com.nova.agent.entity.graph;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nova.agent.constant.AgentFlowContextVar;
import com.nova.agent.entity.AgentFlow;
import com.nova.agent.entity.AgentFlowOutput;
import com.nova.agent.entity.InputVar;
import com.nova.agent.entity.OutPutVar;
import com.nova.agent.entity.UserInvokeInput;
import com.nova.agent.enums.NodeType;
import com.nova.agent.service.AgentFlowService;
import com.nova.agent.utils.SpringContextUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class WorkflowAgentNode extends Node {

    public WorkflowAgentNode(String nodeId, String name, NodeType nodeType,
                             List<InputVar> inputVars, List<OutPutVar> outPutVars, String config) {
        super(nodeId, name, nodeType, inputVars, outPutVars, config);
    }

    @Override
    public void run(AgentFlow agentFlow) {
        log.debug("workflow agent node run, nodeId: {}", this.nodeId);
        try {
            WorkflowAgentNodeConfig waConfig = JSON.parseObject(config, WorkflowAgentNodeConfig.class);
            UserInvokeInput parentInput = (UserInvokeInput) agentFlow.getContextVar(
                    AgentFlowContextVar.USER_INVOKE_INPUT);

            AgentFlowService agentFlowService = SpringContextUtils.getBean(
                    "agentFlowService", AgentFlowService.class);

            // Build sub-workflow input
            UserInvokeInput subInput = new UserInvokeInput();
            subInput.setConversation_id(parentInput.getConversation_id());
            subInput.setApp_id(waConfig.getWorkflowAgentId());
            subInput.setDebug(parentInput.getDebug());
            subInput.setRequestId(parentInput.getRequestId());
            subInput.setExecuteId(parentInput.getExecuteId());

            // Execute sub-workflow
            AgentFlowOutput output = agentFlowService.executeAgentFlow(
                    agentFlowService.constructGraphFromConfig(
                            agentFlowService.queryAgentById(waConfig.getWorkflowAgentId()).getConfig()),
                    subInput);

            agentFlow.setContextVar(
                    AgentFlowContextVar.WORKFLOW_AGENT_OUTPUT + this.nodeId, output);
        } catch (Exception e) {
            throw new RuntimeException(this.getNodeName() + ": Workflow agent execution error! " + e.getMessage(), e);
        }
    }

    @Override
    public void fillOutputVar(AgentFlow agentFlow) {
        AgentFlowOutput output = (AgentFlowOutput) agentFlow.getContextVar(
                AgentFlowContextVar.WORKFLOW_AGENT_OUTPUT + this.nodeId);
        if (output != null && outputVars != null) {
            for (OutPutVar outVar : outputVars) {
                if ("answer".equals(outVar.getVarName())) {
                    outVar.setVarValue(output.getAnswer());
                }
            }
        }
    }

    @Data
    public static class WorkflowAgentNodeConfig {
        private String workflowAgentId;
        private String workflowAgentName;
    }
}
