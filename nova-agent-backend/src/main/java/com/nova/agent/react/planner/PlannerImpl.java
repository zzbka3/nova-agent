package com.nova.agent.react.planner;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nova.agent.llm.CallArgs;
import com.nova.agent.llm.LLMClient;
import com.nova.agent.llm.ModelFallback;
import com.nova.agent.react.entity.AutoAgentConfig;
import com.nova.agent.react.entity.PlannerRequest;
import com.nova.agent.react.entity.PlannerResponse;
import com.nova.agent.react.entity.ReActAction;
import com.nova.agent.react.enums.ReActionType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Uses LLM to make ReAct planning decisions.
 * Builds a structured prompt from PlannerRequest, calls the LLM,
 * and parses the JSON response into a PlannerResponse.
 */
@Slf4j
@Component
public class PlannerImpl implements Planner {

    @Autowired
    private LLMClient llmClient;



    private static final int MAX_PARSE_RETRIES = 2;

    @Override
    public PlannerResponse plan(PlannerRequest request) {
        log.info("Planner plan start: agentId={}, conversationId={}, loop={}/{}",
                request.getAgentId(), request.getConversationId(),
                request.getLoop(), request.getMaxLoop());

        String systemPrompt = buildSystemPrompt(request);
        String userPrompt = buildUserPrompt(request);

        for (int attempt = 1; attempt <= MAX_PARSE_RETRIES + 1; attempt++) {
            try {
                AutoAgentConfig.PlannerConfig plannerCfg = request.getPlannerConfig();
                String modelName = (plannerCfg != null && plannerCfg.getModel() != null) ? plannerCfg.getModel() : "deepseek-v4-pro";
                String modelServerName = (plannerCfg != null) ? plannerCfg.getModelServer() : null;
                String modelTypeStr = (plannerCfg != null && plannerCfg.getModelType() != null) ? plannerCfg.getModelType() : "LLM";
                Double temp = (plannerCfg != null && plannerCfg.getTemperature() != null) ? plannerCfg.getTemperature() : 0.3;
                Integer maxOutTokens = (plannerCfg != null && plannerCfg.getMaxTokens() != null) ? plannerCfg.getMaxTokens() : 4096;

                CallArgs args = CallArgs.builder()
                        .systemPrompt(systemPrompt)
                        .userPrompt(userPrompt)
                        .model(modelName)
                        .modelType(modelTypeStr)
                        .modelServer(modelServerName)
                        .temperature(temp)
                        .maxOutputTokens(maxOutTokens)
                        .build();

                String fbModel = (plannerCfg != null) ? plannerCfg.getFallbackModel() : null;
                String fbServer = (plannerCfg != null) ? plannerCfg.getFallbackModelServer() : null;
                ModelFallback fallback = (StringUtils.isNotBlank(fbModel)
                        && StringUtils.isNotBlank(fbServer))
                        ? new ModelFallback(fbModel, fbServer)
                        : null;

                String apiResponse = llmClient.chatWithFallback(args, fallback);
                log.debug("Planner raw response (first 500 chars): {}",
                        apiResponse.length() > 500 ? apiResponse.substring(0, 500) : apiResponse);
                
                // Extract message content from OpenAI response format
                String rawResponse = extractContent(apiResponse);
                PlannerResponse response = parseResponse(rawResponse);

                log.info("Planner decision: thought={}, action={}({}) tokens={}",
                        truncate(response.getThought(), 80),
                        response.getAction().getType(),
                        response.getAction().getTargetId(),
                        response.getUsedTokens());

                return response;

            } catch (Exception e) {
                log.warn("Planner attempt {}/{} failed: {}", attempt, MAX_PARSE_RETRIES + 1, e.getMessage());
                if (attempt > MAX_PARSE_RETRIES) {
                    log.error("Planner failed after {} attempts, returning FINAL", attempt);
                    return buildFallbackResponse(request, e.getMessage());
                }
            }
        }

        return buildFallbackResponse(request, "Max retries exceeded");
    }

    private String buildSystemPrompt(PlannerRequest req) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一个智能助手");
        if (StringUtils.isNotBlank(req.getAgentName())) {
            sb.append("：").append(req.getAgentName());
        }
        sb.append("。\n\n");

        if (StringUtils.isNotBlank(req.getSystemPrompt())) {
            sb.append(req.getSystemPrompt()).append("\n\n");
        }

        // Current memory context
        if (req.getMemory() != null && !req.getMemory().isEmpty()) {
            sb.append("## 当前记忆\n");
            for (Map.Entry<String, Object> entry : req.getMemory().entrySet()) {
                sb.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            sb.append("\n");
        }

        // Available tools
        if (req.getTools() != null && !req.getTools().isEmpty()) {
            sb.append("## 可用工具\n");
            for (PlannerRequest.ToolSummary tool : req.getTools()) {
                sb.append("- **").append(tool.getName()).append("**");
                sb.append(" (id: `").append(tool.getId()).append("`)");
                sb.append(": ").append(tool.getDescription()).append("\n");
                if (tool.getInputs() != null && !tool.getInputs().isEmpty()) {
                    sb.append("  参数: ");
                    sb.append(tool.getInputs().stream()
                            .map(i -> i.getName() + (i.isRequired() ? "*" : "") + "(" + i.getType() + ")")
                            .collect(Collectors.joining(", ")));
                    sb.append("\n");
                }
            }
            sb.append("\n");
        }

        // Output format
        sb.append("## 输出格式\n");
        sb.append("你必须严格按照以下 JSON 格式返回，不要包含任何其他内容：\n");
        sb.append("```json\n");
        sb.append("{\n");
        sb.append("  \"thought\": \"你的推理和思考过程\",\n");
        sb.append("  \"action\": {\n");
        sb.append("    \"type\": \"TOOL\",\n");
        sb.append("    \"targetId\": \"工具ID\",\n");
        sb.append("    \"arguments\": { \"参数名\": \"参数值\" }\n");
        sb.append("  }\n");
        sb.append("}\n");
        sb.append("```\n\n");
        sb.append("- 如果需要调用工具，action.type = \"TOOL\"，targetId 填写工具的 id\n");
        sb.append("- 如果任务已经完成，action.type = \"FINAL\"，targetId 填 \"FINAL\"，arguments 中必须包含 finalAnswer 字段\n");
        sb.append("- 你只能调用上面列出的工具，不要编造不存在的工具\n");
        sb.append("- arguments 中的必填参数（带*的）必须提供\n");

        return sb.toString();
    }

    private String buildUserPrompt(PlannerRequest req) {
        StringBuilder sb = new StringBuilder();
        sb.append("用户输入: ").append(req.getQuery()).append("\n");

        // Observations from previous tool calls
        List<com.nova.agent.react.entity.Observation> observations =
                req.getObservations() != null ? req.getObservations() : Collections.emptyList();
        if (!observations.isEmpty()) {
            sb.append("\n## 之前的执行结果\n");
            for (int i = 0; i < observations.size(); i++) {
                com.nova.agent.react.entity.Observation obs = observations.get(i);
                sb.append(i + 1).append(". ");
                sb.append(obs.isSuccess() ? "✓" : "✗").append(" ");
                sb.append(obs.getSummary());
                if (obs.getOutputs() != null && !obs.getOutputs().isEmpty()) {
                    sb.append("\n   输出: ").append(JSON.toJSONString(obs.getOutputs()));
                }
                if (obs.getMessage() != null && !obs.getMessage().isEmpty()) {
                    sb.append("\n   消息: ").append(obs.getMessage());
                }
                sb.append("\n");
            }
        }

        sb.append("\n").append("当前是第 ").append(req.getLoop() + 1).append(" 步，");
        sb.append("最多 ").append(req.getMaxLoop()).append(" 步。");
        sb.append("请决定下一步动作。");
        return sb.toString();
    }

    /**
     * Extract the message content from the OpenAI API response JSON.
     * API returns: {"choices":[{"message":{"content":"..."}}]} and we need just the content.
     */
    private String extractContent(String apiResponse) {
        try {
            com.alibaba.fastjson.JSONObject apiJson = JSON.parseObject(apiResponse);
            if (apiJson.containsKey("choices")) {
                com.alibaba.fastjson.JSONArray choices = apiJson.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    com.alibaba.fastjson.JSONObject first = choices.getJSONObject(0);
                    com.alibaba.fastjson.JSONObject msg = first.getJSONObject("message");
                    if (msg != null) {
                        String content = msg.getString("content");
                        if (content != null && !content.isEmpty()) {
                            return content;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract content from API response, using raw text", e);
        }
        return apiResponse;
    }

    /**
     * Parse the LLM's raw response into a PlannerResponse.
     * Handles markdown code fences and direct JSON.
     */
    public PlannerResponse parseResponse(String raw) {
        String json = extractJson(raw);

        JSONObject obj = JSON.parseObject(json);

        PlannerResponse response = new PlannerResponse();
        response.setThought(obj.getString("thought"));

        JSONObject actionObj = obj.getJSONObject("action");
        if (actionObj == null) {
            throw new RuntimeException("Missing 'action' field in Planner response");
        }

        ReActAction action = new ReActAction();
        String typeStr = actionObj.getString("type");
        if (typeStr == null) {
            throw new RuntimeException("Missing 'action.type' field");
        }
        action.setType(ReActionType.valueOf(typeStr.toUpperCase()));
        action.setTargetId(actionObj.getString("targetId"));

        if (actionObj.containsKey("arguments")) {
            JSONObject argsObj = actionObj.getJSONObject("arguments");
            if (argsObj != null) {
                action.setArguments(argsObj);
            }
        }

        response.setAction(action);
        return response;
    }

    /**
     * Extract JSON string from a potentially markdown-wrapped LLM response.
     */
    private String extractJson(String raw) {
        String trimmed = raw.trim();
        // Strip markdown code fences
        if (trimmed.startsWith("```")) {
            int start = trimmed.indexOf("\n");
            if (start == -1) start = 3;
            else start = start + 1;
            int end = trimmed.lastIndexOf("```");
            if (end > start) {
                trimmed = trimmed.substring(start, end).trim();
            } else {
                trimmed = trimmed.substring(start).trim();
            }
        }
        // Find the JSON object boundaries
        int braceOpen = trimmed.indexOf('{');
        int braceClose = trimmed.lastIndexOf('}');
        if (braceOpen >= 0 && braceClose > braceOpen) {
            trimmed = trimmed.substring(braceOpen, braceClose + 1);
        }
        return trimmed;
    }

    private PlannerResponse buildFallbackResponse(PlannerRequest req, String errorMsg) {
        PlannerResponse response = new PlannerResponse();
        response.setThought("Planning failed: " + errorMsg + ". Returning final answer to user.");
        ReActAction action = new ReActAction();
        action.setType(ReActionType.FINAL);
        action.setTargetId("FINAL");
        action.setArguments(Collections.singletonMap("finalAnswer",
                "抱歉，处理您的请求时遇到了问题，请稍后重试。"));
        response.setAction(action);
        return response;
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return null;
        return s.length() <= maxLen ? s : s.substring(0, maxLen) + "...";
    }
}
