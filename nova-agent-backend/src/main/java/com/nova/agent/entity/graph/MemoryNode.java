package com.nova.agent.entity.graph;

import com.alibaba.fastjson.JSON;
import com.nova.agent.constant.AgentFlowContextVar;
import com.nova.agent.entity.AgentFlow;
import com.nova.agent.entity.InputVar;
import com.nova.agent.entity.MemoryVar;
import com.nova.agent.entity.OutPutVar;
import com.nova.agent.enums.NodeType;
import com.nova.agent.enums.VarType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MemoryNode extends Node {

    public MemoryNode(String nodeId, String name, NodeType nodeType,
                      List<InputVar> inputVars, List<OutPutVar> outPutVars, String config) {
        super(nodeId, name, nodeType, inputVars, outPutVars, config);
    }

    @Override
    public void run(AgentFlow agentFlow) {
        log.debug("memory node run, nodeId: {}", this.nodeId);
        MemoryConfig memConfig = JSON.parseObject(config, MemoryConfig.class);
        String operation = memConfig.getOperation();

        ConcurrentHashMap<String, MemoryVar> memoryMap = (ConcurrentHashMap<String, MemoryVar>)
                agentFlow.getContextVar(AgentFlowContextVar.MEMORY_VAR);

        if (memoryMap == null) {
            memoryMap = new ConcurrentHashMap<>();
            agentFlow.setContextVar(AgentFlowContextVar.MEMORY_VAR, memoryMap);
        }

        if ("read".equals(operation)) {
            String varName = memConfig.getVarName();
            if (varName != null && memoryMap.containsKey(varName)) {
                MemoryVar mem = memoryMap.get(varName);
                agentFlow.setContextVar(AgentFlowContextVar.NODE_RESULT_PREFIX + this.nodeId,
                        mem.getVarValue());
            }
        } else if ("write".equals(operation)) {
            if (inputVars != null) {
                for (InputVar var : inputVars) {
                    MemoryVar mem = new MemoryVar();
                    mem.setVarName(var.getVarName());
                    mem.setVarValue(var.getVarValue());
                    mem.setVarType(var.getVarType() != null ? var.getVarType().name() : null);
                    mem.setLastUpdateTime(System.currentTimeMillis());
                    memoryMap.put(var.getVarName(), mem);
                }
            }
        } else if ("clear".equals(operation)) {
            memoryMap.clear();
        }
    }

    @Override
    public void fillOutputVar(AgentFlow agentFlow) {
        if (outputVars == null || outputVars.isEmpty()) return;
        Object value = agentFlow.getContextVar(
                AgentFlowContextVar.NODE_RESULT_PREFIX + this.nodeId);
        if (value != null) {
            for (OutPutVar out : outputVars) {
                out.setVarValue(value);
            }
        }
    }

    @Data
    public static class MemoryConfig {
        private String operation;
        private String varName;
    }
}
