package com.nova.agent.entity.graph;

import com.nova.agent.entity.AgentFlow;
import com.nova.agent.entity.InputVar;
import com.nova.agent.entity.OutPutVar;
import com.nova.agent.enums.NodeType;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 工作流起始节点。
 *
 * <p>在工作流中始终是第一个被执行的节点。负责：
 * <ul>
 *   <li>接收用户原始输入</li>
 *   <li>将输入变量直接透传为输出变量，供后续节点引用</li>
 * </ul>
 *
 * <p>{@link #isStart()} 返回 {@code true}，工作流引擎从它开始点火。
 */
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
