package com.nova.agent.react.entity;

import lombok.Data;
import com.nova.agent.react.entity.AutoAgentConfig.PlannerConfig;
import java.util.List;
import java.util.Map;

@Data
public class PlannerRequest {
    private String query;
    private String conversationId;
    private String agentId;
    private String agentName;
    private String description;
    private String systemPrompt;
    private Map<String, Object> memory;
    private List<ToolSummary> tools;
    private List<WorkflowSummary> workflows;
    private List<Observation> observations;
    private int loop;
    private int maxLoop;
    private PlannerConfig plannerConfig;

    @Data
    public static class ToolSummary {
        private String id;
        private String name;
        private String description;
        private List<InputDef> inputs;

        @Data
        public static class InputDef {
            private String name;
            private String type;
            private boolean required;
            private String description;
        }
    }

    @Data
    public static class WorkflowSummary {
        private String id;
        private String name;
        private String description;
        private List<InputDef> inputs;

        @Data
        public static class InputDef {
            private String name;
            private String type;
            private boolean required;
            private String description;
        }
    }
}
