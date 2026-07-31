package com.nova.agent.react.planner;

import com.nova.agent.react.entity.PlannerRequest;
import com.nova.agent.react.entity.PlannerResponse;

/**
 * Planner interface - makes structured action decisions based on agent state.
 */
@FunctionalInterface
public interface Planner {
    /**
     * Given the current planning context, return the next structured action.
     *
     * @param request planned request containing query, memory, tools, observations and loop info
     * @return structured action decision
     */
    PlannerResponse plan(PlannerRequest request);
}
