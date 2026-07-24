package com.nova.agent.entity.graph;

import com.alibaba.fastjson.JSON;
import com.nova.agent.constant.AgentFlowContextVar;
import com.nova.agent.entity.AgentFlow;
import com.nova.agent.entity.AgentFlowOutput;
import com.nova.agent.entity.InputVar;
import com.nova.agent.entity.OutPutVar;
import com.nova.agent.enums.NodeType;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class EndNode extends Node {

    public EndNode(String nodeId, String name, NodeType nodeType,
                   List<InputVar> inputVars, List<OutPutVar> outPutVars, String config) {
        super(nodeId, name, nodeType, inputVars, outPutVars, config);
    }

    @Override
    public void run(AgentFlow agentFlow) {
        log.debug("end node run, nodeId: {}", this.nodeId);
        AgentFlowOutput output = new AgentFlowOutput();
        // Collect all input var values as the final answer
        StringBuilder answer = new StringBuilder();
        if (inputVars != null && !inputVars.isEmpty()) {
            for (InputVar inputVar : inputVars) {
                Object value = inputVar.getVarValue();
                if (value != null) {
                    if (answer.length() > 0) answer.append("\n");
                    answer.append(String.valueOf(value));
                }
            }
        }
        output.setAnswer(answer.toString());
        agentFlow.setContextVar(AgentFlowContextVar.AGENT_FLOW_OUTPUT, output);
    }

    @Override
    public void fillOutputVar(AgentFlow agentFlow) {
        // End node doesn't have downstream nodes
    }

    @Override
    public boolean isEnd() {
        return true;
    }
}
