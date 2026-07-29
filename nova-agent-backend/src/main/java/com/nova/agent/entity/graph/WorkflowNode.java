package com.nova.agent.entity.graph;

import com.alibaba.fastjson.JSON;
import com.nova.agent.constant.AgentFlowContextVar;
import com.nova.agent.entity.AgentFlow;
import com.nova.agent.entity.InputVar;
import com.nova.agent.entity.OutPutVar;
import com.nova.agent.enums.NodeType;
import com.nova.agent.enums.VarType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 子工作流/流程节点。
 *
 * <p>用于在工作流中插入一个处理步骤（通常是非 AI 的纯文本/数据变换）。
 * 与 {@link WorkflowAgentNode} 不同，此节点不会嵌套执行另一个完整的 Agent，
 * 而是执行一段简单的模板替换逻辑。
 */
@Slf4j
public class WorkflowNode extends Node {

    public WorkflowNode(String nodeId, String name, NodeType nodeType,
                        List<InputVar> inputVars, List<OutPutVar> outPutVars, String config) {
        super(nodeId, name, nodeType, inputVars, outPutVars, config);
    }

    @Override
    public void run(AgentFlow agentFlow) {
        log.debug("workflow node run, nodeId: {}", this.nodeId);
        ProcessNodeConfig processConfig = JSON.parseObject(config, ProcessNodeConfig.class);
        String msgResult = replaceVar(processConfig.msg);
        agentFlow.setContextVar(AgentFlowContextVar.NODE_RESULT_PREFIX + this.nodeId, msgResult);
    }

    @Override
    public void fillOutputVar(AgentFlow agentFlow) {
        String msg = (String) agentFlow.getContextVar(
                AgentFlowContextVar.NODE_RESULT_PREFIX + this.nodeId);
        // Propagate all input vars as outputs
        if (inputVars != null) {
            for (InputVar inputVar : inputVars) {
                OutPutVar out = new OutPutVar();
                out.setVarName(inputVar.getVarName());
                out.setVarType(inputVar.getVarType());
                out.setVarValue(inputVar.getVarValue());
                if (outputVars != null) {
                    outputVars.add(out);
                }
            }
        }
        OutPutVar msgOut = new OutPutVar();
        msgOut.setVarName("msg");
        msgOut.setVarType(VarType.String);
        msgOut.setVarValue(msg);
        if (outputVars != null) {
            outputVars.add(msgOut);
        }
    }

    @Data
    public static class ProcessNodeConfig {
        private Boolean isStream;
        private String msg;
    }
}
