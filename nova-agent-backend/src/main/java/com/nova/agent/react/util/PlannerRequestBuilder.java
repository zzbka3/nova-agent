package com.nova.agent.react.util;

import com.nova.agent.react.entity.AgentState;
import com.nova.agent.react.entity.AutoAgentConfig;
import com.nova.agent.react.entity.PlannerRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a PlannerRequest from AutoAgent config and current AgentState.
 */
public class PlannerRequestBuilder {

    /**
     * Build a PlannerRequest for the next planning cycle.
     */
    public static PlannerRequest build(AutoAgentConfig config, AgentState state,
                                        List<PlannerRequest.ToolSummary> toolSummaries,
                                        List<PlannerRequest.WorkflowSummary> workflowSummaries) {
        PlannerRequest req = new PlannerRequest();
        req.setQuery(state.getConversation().getCurrentQuery());
        req.setConversationId(state.getConversation().getConversationId());
        req.setAgentId(state.getConversation().getAgentId());
        req.setAgentName(config.getName());
        req.setDescription(config.getDescription());
        req.setSystemPrompt(config.getSystemPrompt());
        req.setMemory(state.getMemory());

        req.setTools(toolSummaries != null ? toolSummaries : new ArrayList<>());
        req.setWorkflows(workflowSummaries != null ? workflowSummaries : new ArrayList<>());

        // Accumulate observations: current round + history
        List<com.nova.agent.react.entity.Observation> allObs = new ArrayList<>();
        if (state.getObservations().getHistory() != null) {
            allObs.addAll(state.getObservations().getHistory());
        }
        if (state.getObservations().getCurrent() != null) {
            allObs.addAll(state.getObservations().getCurrent());
        }
        req.setObservations(allObs);

        req.setLoop(state.getPlanner().getLoop());
        req.setMaxLoop(state.getPlanner().getMaxLoop());
        req.setPlannerConfig(config.getPlannerConfig());

        return req;
    }
}
