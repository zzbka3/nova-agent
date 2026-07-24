package com.nova.agent.entity.graph;

import com.alibaba.fastjson.JSON;
import com.nova.agent.entity.AgentFlow;
import com.nova.agent.entity.InputVar;
import com.nova.agent.entity.OutPutVar;
import com.nova.agent.enums.NodeType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class LoopNode extends Node {

    public LoopNode(String nodeId, String name, NodeType nodeType,
                    List<InputVar> inputVars, List<OutPutVar> outPutVars, String config) {
        super(nodeId, name, nodeType, inputVars, outPutVars, config);
    }

    @Override
    public void run(AgentFlow agentFlow) {
        log.debug("loop node run, nodeId: {}", this.nodeId);
        LoopConfig loopConfig = JSON.parseObject(config, LoopConfig.class);
        // Loop logic is handled at the graph execution level
        // This node acts as a loop entry point marker
    }

    @Override
    public void fillOutputVar(AgentFlow agentFlow) {
        // Pass through input vars to output
        if (inputVars != null && outputVars != null) {
            for (InputVar in : inputVars) {
                for (OutPutVar out : outputVars) {
                    if (in.getVarName().equals(out.getVarName())) {
                        out.setVarValue(in.getVarValue());
                    }
                }
            }
        }
    }

    @Data
    public static class LoopConfig {
        private String loopType; // FOR, WHILE, FOREACH
        private Integer maxIterations;
        private String condition;
        private String arrayVar;
    }
}
