package com.nova.agent.react.entity;

import lombok.Data;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class AgentState {

    @Data
    public static class ConversationState {
        private String conversationId;
        private String agentId;
        private String currentQuery;
    }

    @Data
    public static class PlannerState {
        private int loop;
        private int maxLoop;
        private ReActAction lastAction;
    }

    @Data
    public static class ObservationsState {
        private List<Observation> current = new ArrayList<>();
        private List<Observation> history = new ArrayList<>();
    }

    @Data
    public static class RuntimeState {
        private String status = "INIT";
        private int currentStep;
        private long startTime;
        private long lastUpdateTime;
    }

    private ConversationState conversation = new ConversationState();
    private Map<String, Object> memory = new LinkedHashMap<>();
    private ObservationsState observations = new ObservationsState();
    private PlannerState planner = new PlannerState();
    private RuntimeState runtime = new RuntimeState();
}
