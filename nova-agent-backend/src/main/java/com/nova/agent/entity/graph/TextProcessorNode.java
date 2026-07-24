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

@Slf4j
public class TextProcessorNode extends Node {

    public TextProcessorNode(String nodeId, String name, NodeType nodeType,
                             List<InputVar> inputVars, List<OutPutVar> outPutVars, String config) {
        super(nodeId, name, nodeType, inputVars, outPutVars, config);
    }

    @Override
    public void run(AgentFlow agentFlow) {
        log.debug("text processor node run, nodeId: {}", this.nodeId);
        ProcessorConfig procConfig = JSON.parseObject(config, ProcessorConfig.class);
        String text = null;
        if (inputVars != null) {
            for (InputVar var : inputVars) {
                if ("text".equals(var.getVarName())) {
                    text = (String) var.getVarValue();
                    break;
                }
            }
        }
        if (text == null) text = "";

        String result = processText(text, procConfig);
        agentFlow.setContextVar(AgentFlowContextVar.NODE_RESULT_PREFIX + this.nodeId, result);
    }

    private String processText(String text, ProcessorConfig config) {
        String result = text;
        String operation = config.getOperation();
        if ("trim".equals(operation)) {
            result = text.trim();
        } else if ("upper".equals(operation)) {
            result = text.toUpperCase();
        } else if ("lower".equals(operation)) {
            result = text.toLowerCase();
        } else if ("replace".equals(operation) && config.getFind() != null) {
            result = text.replace(config.getFind(), config.getReplace() != null ? config.getReplace() : "");
        } else if ("extract_json".equals(operation)) {
            result = extractJson(text);
        }
        return result;
    }

    private String extractJson(String text) {
        int start = text.indexOf("{");
        int end = text.lastIndexOf("}");
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        start = text.indexOf("[");
        end = text.lastIndexOf("]");
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    @Override
    public void fillOutputVar(AgentFlow agentFlow) {
        if (outputVars == null || outputVars.isEmpty()) return;
        String result = (String) agentFlow.getContextVar(
                AgentFlowContextVar.NODE_RESULT_PREFIX + this.nodeId);
        for (OutPutVar out : outputVars) {
            if ("result".equals(out.getVarName())) {
                out.setVarValue(result);
                out.setVarType(VarType.String);
            }
        }
    }

    @Data
    public static class ProcessorConfig {
        private String operation;
        private String find;
        private String replace;
    }
}
