package com.nova.agent.react.entity;

import lombok.Data;
import java.util.List;

/**
 * AutoAgent configuration parsed from agent.config JSON.
 */
@Data
public class AutoAgentConfig {
    private String id;
    private String name;
    private String description;
    private String systemPrompt;
    private Integer maxLoop = 10;
    private List<String> tools;
    private List<String> workflows;
    private List<MemorySchemaEntry> memorySchema;
    private Integer referenceTurns = 5;
    private PlannerConfig plannerConfig = new PlannerConfig();

    @Data
    public static class MemorySchemaEntry {
        private String name;
        private String type;
        private String description;
    }

    @Data
    public static class PlannerConfig {
        private String model = "deepseek-v4-pro";
        private String modelServer;
        private String modelType = "LLM";
        private Double temperature = 0.3;
        private Integer maxTokens = 4096;
        private String fallbackModel;
        private String fallbackModelServer;
    }
}
