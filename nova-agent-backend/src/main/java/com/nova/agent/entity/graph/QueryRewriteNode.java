package com.nova.agent.entity.graph;

import com.alibaba.fastjson.JSON;
import com.nova.agent.constant.AgentFlowContextVar;
import com.nova.agent.entity.AgentFlow;
import com.nova.agent.entity.InputVar;
import com.nova.agent.entity.OutPutVar;
import com.nova.agent.enums.NodeType;
import com.nova.agent.enums.VarType;
import com.nova.agent.llm.LLMClient;
import com.nova.agent.utils.SpringContextUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class QueryRewriteNode extends Node {

    public QueryRewriteNode(String nodeId, String name, NodeType nodeType,
                            List<InputVar> inputVars, List<OutPutVar> outPutVars, String config) {
        super(nodeId, name, nodeType, inputVars, outPutVars, config);
    }

    @Override
    public void run(AgentFlow agentFlow) {
        log.debug("query rewrite node run, nodeId: {}", this.nodeId);
        RewriteConfig rewriteConfig = JSON.parseObject(config, RewriteConfig.class);
        String query = null;
        if (inputVars != null) {
            for (InputVar var : inputVars) {
                if ("query".equals(var.getVarName())) {
                    query = (String) var.getVarValue();
                    break;
                }
            }
        }
        if (query == null) query = "";

        String prompt = rewriteConfig.getRewritePrompt().replace("{{query}}", query);
        try {
            LLMClient llmClient = SpringContextUtils.getBean("resilientLlmProxy", LLMClient.class);
            String result = llmClient.chat(prompt, rewriteConfig.getModel());
            agentFlow.setContextVar(AgentFlowContextVar.NODE_RESULT_PREFIX + this.nodeId, result);
        } catch (Exception e) {
            agentFlow.setContextVar(AgentFlowContextVar.NODE_RESULT_PREFIX + this.nodeId, query);
        }
    }

    @Override
    public void fillOutputVar(AgentFlow agentFlow) {
        if (outputVars == null || outputVars.isEmpty()) return;
        String result = (String) agentFlow.getContextVar(
                AgentFlowContextVar.NODE_RESULT_PREFIX + this.nodeId);
        for (OutPutVar out : outputVars) {
            if ("rewrittenQuery".equals(out.getVarName())) {
                out.setVarValue(result);
                out.setVarType(VarType.String);
            }
        }
    }

    @Data
    public static class RewriteConfig {
        private String rewritePrompt;
        private String model;
    }
}
