package com.nova.agent.react.trace;

import com.alibaba.fastjson.JSON;
import com.nova.agent.react.entity.ExecutionResult;
import com.nova.agent.react.entity.PlannerResponse;
import com.nova.agent.react.entity.ReActAction;
import com.nova.agent.react.enums.ReActionType;
import com.nova.agent.react.repository.PlannerTraceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Records Planner decisions and Tool executions to the planner_trace table.
 */
@Slf4j
@Component
public class TraceService {

    @Autowired
    private PlannerTraceMapper plannerTraceMapper;

    /**
     * Record a Planner decision.
     */
    public void recordPlanner(String appId, String conversationId, Long executeId,
                               int loopIndex, PlannerResponse response, int costTime) {
        PlannerTrace trace = new PlannerTrace();
        trace.setAppId(appId);
        trace.setConversationId(conversationId);
        trace.setExecuteId(executeId);
        trace.setLoopIndex(loopIndex);
        trace.setTraceType("PLANNER");
        trace.setThought(response.getThought());

        if (response.getAction() != null) {
            ReActAction action = response.getAction();
            trace.setActionType(action.getType() != null ? action.getType().name() : null);
            trace.setActionTarget(action.getTargetId());
            trace.setActionArguments(action.getArguments() != null
                    ? JSON.toJSONString(action.getArguments()) : null);
        }

        trace.setCostTime(costTime);
        trace.setUsedTokens(response.getUsedTokens() != null ? response.getUsedTokens() : 0);

        try {
            plannerTraceMapper.insert(trace);
        } catch (Exception e) {
            log.error("Failed to record planner trace", e);
        }
    }

    /**
     * Record a Tool execution result.
     */
    public void recordTool(String appId, String conversationId, Long executeId,
                            int loopIndex, ReActAction action, ExecutionResult result) {
        PlannerTrace trace = new PlannerTrace();
        trace.setAppId(appId);
        trace.setConversationId(conversationId);
        trace.setExecuteId(executeId);
        trace.setLoopIndex(loopIndex);
        trace.setTraceType("TOOL");
        trace.setActionType(action.getType() != null ? action.getType().name() : null);
        trace.setActionTarget(action.getTargetId());
        trace.setActionArguments(action.getArguments() != null
                ? JSON.toJSONString(action.getArguments()) : null);
        trace.setResultSummary(result.getSummary());
        trace.setResultSuccess(result.isSuccess() ? 1 : 0);
        trace.setResultOutputs(result.getOutputs() != null
                ? JSON.toJSONString(result.getOutputs()) : null);
        trace.setSignal(result.getSignal() != null ? result.getSignal().name() : null);
        trace.setCostTime(result.getCostTime() != null ? result.getCostTime().intValue() : 0);
        trace.setUsedTokens(result.getUsedTokens() != null ? result.getUsedTokens() : 0);

        try {
            plannerTraceMapper.insert(trace);
        } catch (Exception e) {
            log.error("Failed to record tool trace", e);
        }
    }

    /**
     * Record the final FINAL action.
     */
    public void recordFinal(String appId, String conversationId, Long executeId,
                             int loopIndex, PlannerResponse response, int costTime) {
        PlannerTrace trace = new PlannerTrace();
        trace.setAppId(appId);
        trace.setConversationId(conversationId);
        trace.setExecuteId(executeId);
        trace.setLoopIndex(loopIndex);
        trace.setTraceType("FINAL");
        trace.setThought(response.getThought());

        if (response.getAction() != null) {
            ReActAction action = response.getAction();
            trace.setActionType(action.getType() != null ? action.getType().name() : null);
            trace.setActionTarget(action.getTargetId());
            trace.setActionArguments(action.getArguments() != null
                    ? JSON.toJSONString(action.getArguments()) : null);
        }

        trace.setCostTime(costTime);
        trace.setUsedTokens(response.getUsedTokens() != null ? response.getUsedTokens() : 0);

        try {
            plannerTraceMapper.insert(trace);
        } catch (Exception e) {
            log.error("Failed to record final trace", e);
        }
    }
}
