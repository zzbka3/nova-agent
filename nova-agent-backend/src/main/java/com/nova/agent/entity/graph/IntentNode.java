package com.nova.agent.entity.graph;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nova.agent.constant.AgentFlowContextVar;
import com.nova.agent.entity.AgentFlow;
import com.nova.agent.entity.InputVar;
import com.nova.agent.entity.OutPutVar;
import com.nova.agent.enums.IntentNodeMode;
import com.nova.agent.enums.NodeType;
import com.nova.agent.enums.VarType;
import com.nova.agent.llm.LLMClient;
import com.nova.agent.utils.SpringContextUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class IntentNode extends Node {
    private IntentNodeMode mode;

    public IntentNode(String nodeId, String name, NodeType nodeType,
                      List<InputVar> inputVars, List<OutPutVar> outPutVars, String config) {
        super(nodeId, name, nodeType, inputVars, outPutVars, config);
        IntentConfig intentConfig = JSON.parseObject(config, IntentConfig.class);
        this.mode = intentConfig.getMode();
    }

    @Override
    public void run(AgentFlow agentFlow) {
        log.debug("intent node run, nodeId: {}", this.nodeId);
        IntentConfig intentConfig = JSON.parseObject(config, IntentConfig.class);
        String query = null;
        if (inputVars != null) {
            for (InputVar var : inputVars) {
                if ("query".equals(var.getVarName())) {
                    query = (String) var.getVarValue();
                    break;
                }
            }
        }

        String intentJson = JSON.toJSONString(intentConfig.getIntentList());
        String template = (mode == IntentNodeMode.SPEED)
                ? AgentFlowContextVar.INTENT_PROMPT_SPEED_TEMPLATE
                : AgentFlowContextVar.INTENT_PROMPT_TEMPLATE;

        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String prompt = template.replace("{{intent}}", intentJson)
                .replace("{{nowDate}}", today);

        try {
            LLMClient llmClient = SpringContextUtils.getBean("resilientLlmProxy", LLMClient.class);
            String result = llmClient.chat(prompt, intentConfig.getModel());
            agentFlow.setContextVar(AgentFlowContextVar.NODE_RESULT_PREFIX + this.nodeId, result);
        } catch (Exception e) {
            throw new RuntimeException(this.getNodeName() + ": Intent recognition error! " + e.getMessage(), e);
        }
    }

    @Override
    public void fillOutputVar(AgentFlow agentFlow) {
        if (outputVars == null || outputVars.isEmpty()) return;
        String response = (String) agentFlow.getContextVar(
                AgentFlowContextVar.NODE_RESULT_PREFIX + this.nodeId);
        try {
            JSONObject json = JSON.parseObject(response);
            for (OutPutVar outVar : outputVars) {
                String name = outVar.getVarName();
                if (AgentFlowContextVar.INTENT_ID_FIELD_NAME.equals(name)) {
                    outVar.setVarValue(json.getString("intentId"));
                } else if (AgentFlowContextVar.INTENT_NAME_FIELD_NAME.equals(name)) {
                    outVar.setVarValue(json.getString("intentName"));
                } else if (AgentFlowContextVar.INTENT_THOUGHT_FIELD_NAME.equals(name)) {
                    outVar.setVarValue(json.getString("thought"));
                }
            }
        } catch (Exception e) {
            log.error("IntentNode fill output error!", e);
        }
    }

    @Data
    public static class IntentConfig {
        private IntentNodeMode mode;
        private List<IntentItem> intentList;
        private String model;
    }

    @Data
    public static class IntentItem {
        private String intentId;
        private String intentName;
        private List<ExtractVar> extractVars;
    }

    @Data
    public static class ExtractVar {
        private String varName;
        private String varType;
    }
}
