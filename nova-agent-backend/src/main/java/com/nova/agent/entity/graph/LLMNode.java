package com.nova.agent.entity.graph;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nova.agent.constant.AgentFlowContextVar;
import com.nova.agent.entity.AgentFlow;
import com.nova.agent.entity.InputVar;
import com.nova.agent.entity.OutPutVar;
import com.nova.agent.entity.UserInvokeInput;
import com.nova.agent.enums.ModelType;
import com.nova.agent.enums.NodeType;
import com.nova.agent.enums.VarType;
import com.nova.agent.llm.CallArgs;
import com.nova.agent.llm.LLMClient;
import com.nova.agent.llm.ModelFallback;
import com.nova.agent.model.dto.CurrentUser;
import com.nova.agent.model.po.AgentExecuteLog;
import com.nova.agent.utils.SpringContextUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
public class LLMNode extends Node {
    private String model;
    private String modelType;
    private String modelServer;
    private Double temperature;
    private Integer maxOutputTokens;
    private String systemPrompt;
    private String userPrompt;
    private Integer talkHistory;
    private String fallbackModelName;
    private String fallbackModelServer;

    public LLMNode(String id, String name, NodeType nodeType,
                   List<InputVar> inputVars, List<OutPutVar> outPutVars, String config) {
        super(id, name, nodeType, inputVars, outPutVars, config);
        JSONObject object = JSON.parseObject(config);
        this.model = object.getString("model");
        this.modelType = object.getString("modelType");
        this.modelServer = object.getString("modelServer");
        this.talkHistory = object.getInteger("talkHistory");
        this.maxOutputTokens = object.getInteger("maxOutputTokens");
        this.systemPrompt = object.getString("systemPrompt");
        this.userPrompt = object.getString("userPrompt");
        this.temperature = object.getDouble("temperature");
        this.fallbackModelName = object.getString("fallbackModelName");
        this.fallbackModelServer = object.getString("fallbackModelServer");
    }

    @Override
    public void run(AgentFlow agentFlow) {
        log.debug("llm node run, nodeId: {}", this.nodeId);

        CurrentUser currentUser = (CurrentUser) agentFlow.getContextVar(
                AgentFlowContextVar.CURRENT_ACCOUNT);
        String finalUserPrompt = replaceVar(userPrompt);
        String finalSysPrompt = replaceVar(systemPrompt);

        List<String> fileUrls = new ArrayList<>();
        if (inputVars != null) {
            for (InputVar var : inputVars) {
                if (var.getVarType().equals(VarType.reference)
                        && var.getReferenceNodeId().equals(AgentFlowContextVar.DEFAULT_START_NODE_ID)
                        && var.getReferenceVarName().equals(AgentFlowContextVar.DEFAULT_START_NODE_ID_FILE_FIELD)) {
                    fileUrls = (List<String>) var.getVarValue();
                }
            }
        }

        try {
            LLMClient llmClient = SpringContextUtils.getBean("resilientLlmProxy", LLMClient.class);

            List<AgentExecuteLog> histories = new ArrayList<>();
            if (talkHistory != null && talkHistory == 1) {
                UserInvokeInput userInvokeInput = (UserInvokeInput) agentFlow.getContextVar(
                        AgentFlowContextVar.USER_INVOKE_INPUT);
                histories = userInvokeInput.getRealHistory();
            }

            String imageUrl = (fileUrls != null && !fileUrls.isEmpty()
                    && ModelType.VL.name().equals(modelType)) ? fileUrls.get(0) : null;

            CallArgs args = CallArgs.builder()
                    .userPrompt(finalUserPrompt)
                    .systemPrompt(finalSysPrompt)
                    .model(model)
                    .modelType(modelType)
                    .modelServer(modelServer)
                    .maxOutputTokens(maxOutputTokens)
                    .temperature(temperature)
                    .imageUrl(imageUrl)
                    .histories(histories)
                    .accountId(currentUser != null ? currentUser.getAccountId() : 0L)
                    .build();

            ModelFallback fb = (StringUtils.isNotBlank(fallbackModelName)
                    && StringUtils.isNotBlank(fallbackModelServer))
                    ? new ModelFallback(fallbackModelName, fallbackModelServer) : null;

            String result = llmClient.chatWithFallback(args, fb);
            agentFlow.setContextVar(AgentFlowContextVar.NODE_RESULT_PREFIX + this.nodeId, result);
        } catch (Exception e) {
            throw new RuntimeException(this.getNodeName() + ": LLM node call error! " + e.getMessage());
        }
    }

    @Override
    public void fillOutputVar(AgentFlow agentFlow) {
        if (outputVars != null && !outputVars.isEmpty()) {
            String response = (String) agentFlow.getContextVar(
                    AgentFlowContextVar.NODE_RESULT_PREFIX + this.nodeId);
            try {
                JSONArray jsonArray = JSON.parseObject(response).getJSONArray("choices");
                if (jsonArray == null) {
                    throw new RuntimeException(response);
                }
                JSONObject choices = jsonArray.getJSONObject(0);
                String result = choices.getJSONObject("message").getString("content");
                if (result.contains("```json")) {
                    result = result.replace("```json", "").replace("```", "");
                }
                JSONObject usage = JSON.parseObject(response).getJSONObject("usage");
                if (usage != null) {
                    Integer tokens = usage.getInteger("total_tokens");
                    useTokens += tokens == null ? 0 : tokens;
                }
                String reasoningContent = choices.getJSONObject("message")
                        .getString(AgentFlowContextVar.LLM_REASONING_CONTENT_FIELD_NAME);

                for (OutPutVar outPutVar : outputVars) {
                    if (outPutVar.getVarName().equals(AgentFlowContextVar.LLM_REASONING_CONTENT_FIELD_NAME)) {
                        outPutVar.setVarValue(reasoningContent);
                    } else {
                        outPutVar.setVarValue(agentFlow.covert(result, outPutVar.getVarType()));
                    }
                }
            } catch (Exception e) {
                log.error("llmNode fill output var exception!", e);
                throw new RuntimeException(this.getNodeName() + ": LLM node parse result error! " + e.getMessage());
            }
        }
    }
}
