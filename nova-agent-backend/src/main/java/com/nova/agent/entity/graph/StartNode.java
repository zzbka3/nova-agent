package com.nova.agent.entity.graph;

import com.nova.agent.entity.AgentFlow;
import com.nova.agent.entity.InputVar;
import com.nova.agent.entity.OutPutVar;
import com.nova.agent.enums.NodeType;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class StartNode extends Node {

    public StartNode(String nodeId, String name, NodeType nodeType,
                     List<InputVar> inputVars, List<OutPutVar> outPutVars, String config) {
        super(nodeId, name, nodeType, inputVars, outPutVars, config);
    }

    @Override
    public void run(AgentFlow agentFlow) {
        log.debug("start node run, nodeId: {}", this.nodeId);
        // Start node propagates user input to output vars directly
    }

    @Override
    public void fillOutputVar(AgentFlow agentFlow) {
        // Start node populates user inputs as outputs for downstream nodes
        if (outputVars != null) {
            for (OutPutVar var : outputVars) {
                if (var.getVarValue() == null && inputVars != null) {
                    for (InputVar inputVar : inputVars) {
                        if (inputVar.getVarName().equals(var.getVarName())) {
                            var.setVarValue(inputVar.getVarValue());
                            var.setVarType(inputVar.getVarType() == null ? var.getVarType() : inputVar.getVarType());
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean isStart() {
        return true;
    }
}
