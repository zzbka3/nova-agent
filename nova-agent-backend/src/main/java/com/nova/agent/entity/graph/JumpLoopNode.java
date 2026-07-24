package com.nova.agent.entity.graph;

import com.nova.agent.entity.AgentFlow;
import com.nova.agent.entity.InputVar;
import com.nova.agent.entity.OutPutVar;
import com.nova.agent.enums.NodeType;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class JumpLoopNode extends Node {

    public JumpLoopNode(String nodeId, String name, NodeType nodeType,
                        List<InputVar> inputVars, List<OutPutVar> outPutVars, String config) {
        super(nodeId, name, nodeType, inputVars, outPutVars, config);
    }

    @Override
    public void run(AgentFlow agentFlow) {
        log.debug("jump loop node run, nodeId: {}", this.nodeId);
        // Jump back to the corresponding LoopNode
        // This is handled at the graph level through edges
    }

    @Override
    public void fillOutputVar(AgentFlow agentFlow) {
        // Pass through
    }
}
