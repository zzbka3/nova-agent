package com.nova.agent.react.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nova.agent.react.entity.AutoAgentConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Parses an Agent's config JSON into an AutoAgentConfig for ReAct mode.
 *
 * Expected config format:
 * {
 *   "systemPrompt": "...",
 *   "maxLoop": 10,
 *   "tools": ["tool_id_1", "tool_id_2"],
 *   "workflows": [],
 *   "memorySchema": [{"name": "orderId", "type": "string", "description": "..."}],
 *   "plannerConfig": {
 *     "model": "gpt-4",
 *     "modelServer": "https://...",
 *     "modelType": "LLM",
 *     "temperature": 0.3,
 *     "maxTokens": 4096,
 *     "fallbackModel": "gpt-4o-mini",
 *     "fallbackModelServer": "https://..."
 *   }
 * }
 */
@Slf4j
public class AgentConfigParser {

    public static AutoAgentConfig parse(String appId, String agentName,
                                         String config, String memorySchema,
                                         Integer referenceTurns) {
        AutoAgentConfig cfg = new AutoAgentConfig();
        cfg.setId(appId);
        cfg.setName(agentName);

        try {
            JSONObject root = JSON.parseObject(config);
            cfg.setSystemPrompt(root.getString("systemPrompt"));
            cfg.setMaxLoop(root.getInteger("maxLoop") != null ? root.getInteger("maxLoop") : 10);

            // Parse tools array
            List<String> tools = parseStringList(root, "tools");
            cfg.setTools(tools != null ? tools : Collections.emptyList());

            // Parse workflows array
            List<String> workflows = parseStringList(root, "workflows");
            cfg.setWorkflows(workflows != null ? workflows : Collections.emptyList());

            // Try to parse description if available
            if (root.containsKey("description")) {
                cfg.setDescription(root.getString("description"));
            }

            // Parse planner config
            if (root.containsKey("plannerConfig")) {
                JSONObject plannerJson = root.getJSONObject("plannerConfig");
                AutoAgentConfig.PlannerConfig plannerCfg = new AutoAgentConfig.PlannerConfig();
                if (plannerJson.containsKey("model")) plannerCfg.setModel(plannerJson.getString("model"));
                if (plannerJson.containsKey("modelServer")) plannerCfg.setModelServer(plannerJson.getString("modelServer"));
                if (plannerJson.containsKey("modelType")) plannerCfg.setModelType(plannerJson.getString("modelType"));
                if (plannerJson.containsKey("temperature")) plannerCfg.setTemperature(plannerJson.getDouble("temperature"));
                if (plannerJson.containsKey("maxTokens")) plannerCfg.setMaxTokens(plannerJson.getInteger("maxTokens"));
                if (plannerJson.containsKey("fallbackModel")) plannerCfg.setFallbackModel(plannerJson.getString("fallbackModel"));
                if (plannerJson.containsKey("fallbackModelServer")) plannerCfg.setFallbackModelServer(plannerJson.getString("fallbackModelServer"));
                cfg.setPlannerConfig(plannerCfg);
            }

        } catch (Exception e) {
            log.error("Failed to parse AutoAgent config for appId={}", appId, e);
            // Set defaults
            cfg.setSystemPrompt(config);  // Use raw config as system prompt
            cfg.setTools(Collections.emptyList());
            cfg.setWorkflows(Collections.emptyList());
        }

        // Parse memory schema from agent's memorySchema field (not from config)
        if (StringUtils.isNotBlank(memorySchema)) {
            try {
                List<AutoAgentConfig.MemorySchemaEntry> entries =
                        JSON.parseArray(memorySchema, AutoAgentConfig.MemorySchemaEntry.class);
                cfg.setMemorySchema(entries);
            } catch (Exception e) {
                log.warn("Failed to parse memorySchema for appId={}", appId);
                cfg.setMemorySchema(Collections.emptyList());
            }
        }

        cfg.setReferenceTurns(referenceTurns != null ? referenceTurns : 5);
        return cfg;
    }

    private static List<String> parseStringList(JSONObject root, String key) {
        JSONArray arr = root.getJSONArray(key);
        if (arr == null) return Collections.emptyList();
        List<String> result = new ArrayList<>(arr.size());
        for (int i = 0; i < arr.size(); i++) {
            result.add(arr.getString(i));
        }
        return result;
    }
}
