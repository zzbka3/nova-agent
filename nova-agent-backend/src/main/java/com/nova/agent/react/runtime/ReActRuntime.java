package com.nova.agent.react.runtime;

import com.alibaba.fastjson.JSON;
import com.nova.agent.entity.UserInvokeInput;
import com.nova.agent.model.po.Agent;
import com.nova.agent.react.convert.ObservationMapper;
import com.nova.agent.react.entity.*;
import com.nova.agent.react.enums.ReActSignal;
import com.nova.agent.react.enums.ReActionType;
import com.nova.agent.react.planner.Planner;
import com.nova.agent.react.tool.ToolRegistry;
import com.nova.agent.react.tool.ToolRuntime;
import com.nova.agent.react.trace.TraceService;
import com.nova.agent.react.util.AgentConfigParser;
import com.nova.agent.react.util.PlannerRequestBuilder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * ReAct main loop engine.
 *
 * Full cycle:
 * 1. Load/Create AgentState from Redis
 * 2. Build PlannerRequest from config + state
 * 3. Call Planner → get structured action
 * 4. If FINAL: save state, return answer
 * 5. Execute Tool via ToolRuntime
 * 6. Convert result to Observation, update memory
 * 7. Repeat (up to maxLoop)
 */
@Slf4j
@Service
public class ReActRuntime {

    @Autowired
    private AgentStateManager stateManager;

    @Autowired
    private Planner planner;

    @Autowired
    private ToolRuntime toolRuntime;

    @Autowired
    private ToolRegistry toolRegistry;

    @Autowired
    private TraceService traceService;

    /**
     * Execute a ReAct agent conversation turn.
     *
     * @param agent   the Agent record from DB
     * @param input   user invocation input
     * @return structured response with final answer and stats
     */
    public ReActResponse execute(Agent agent, UserInvokeInput input) {
        long startTime = System.currentTimeMillis();
        String appId = agent.getAppId();
        String conversationId = input.getConversation_id();
        log.info("ReActRuntime start: appId={}, conversationId={}, query={}",
                appId, conversationId, input.getQuery());

        // 1. Parse agent config
        AutoAgentConfig config = AgentConfigParser.parse(
                appId, agent.getName(), agent.getConfig(),
                agent.getMemorySchema(), agent.getReferenceTurns());

        // 2. Build tool summaries for Planner
        List<PlannerRequest.ToolSummary> toolSummaries =
                toolRuntime.buildToolSummaries(config.getTools());

        // 3. Load or create AgentState
        AgentState state = stateManager.loadOrCreate(
                appId, conversationId, input, config.getName(), config.getMaxLoop());

        // Update current query
        state.getConversation().setCurrentQuery(input.getQuery());
        state.getRuntime().setStatus("RUNNING");

        // 4. Initialize memory with schema defaults if empty
        if (config.getMemorySchema() != null && !config.getMemorySchema().isEmpty()
                && state.getMemory().isEmpty()) {
            for (AutoAgentConfig.MemorySchemaEntry entry : config.getMemorySchema()) {
                state.getMemory().putIfAbsent(entry.getName(), null);
            }
        }

        int totalTokens = 0;
        String finalAnswer = null;

        try {
            // 5. Main ReAct loop
            for (int loop = state.getPlanner().getLoop(); loop < config.getMaxLoop(); loop++) {
                state.getPlanner().setLoop(loop);
                state.getRuntime().setCurrentStep(loop + 1);

                log.info("ReAct loop {}/{}: appId={}, conversationId={}",
                        loop + 1, config.getMaxLoop(), appId, conversationId);

                // 5a. Build Planner request
                PlannerRequest plannerReq = PlannerRequestBuilder.build(
                        config, state, toolSummaries, Collections.emptyList());

                // 5b. Call Planner
                long plannerStart = System.currentTimeMillis();
                PlannerResponse plannerResp = planner.plan(plannerReq);
                int plannerCost = (int) (System.currentTimeMillis() - plannerStart);
                totalTokens += plannerResp.getUsedTokens() != null ? plannerResp.getUsedTokens() : 0;

                // Track last action
                state.getPlanner().setLastAction(plannerResp.getAction());

                // 5c. Check for FINAL
                if (plannerResp.getAction().getType() == ReActionType.FINAL) {
                    state.getRuntime().setStatus("FINISH");
                    traceService.recordFinal(appId, conversationId,
                            input.getExecuteId(), loop, plannerResp, plannerCost);

                    // Extract final answer
                    Map<String, Object> finalArgs = plannerResp.getAction().getArguments();
                    finalAnswer = (finalArgs != null && finalArgs.containsKey("finalAnswer"))
                            ? String.valueOf(finalArgs.get("finalAnswer"))
                            : plannerResp.getThought();
                    break;
                }

                // 5d. Record Planner trace
                traceService.recordPlanner(appId, conversationId,
                        input.getExecuteId(), loop, plannerResp, plannerCost);

                // 5e. Execute Tool
                ExecutionResult result = toolRuntime.execute(
                        plannerResp.getAction(), state, input);

                totalTokens += result.getUsedTokens() != null ? result.getUsedTokens() : 0;

                // 5f. Convert to Observation
                Observation obs = ObservationMapper.toObservation(result);
                state.getObservations().getCurrent().add(obs);

                // 5g. Apply memory mapping from tool definition
                applyMemoryMappingForAction(result, state);
                // Also write all outputs to memory by default
                applyOutputsToMemory(result, state);

                // 5h. Record Tool trace
                traceService.recordTool(appId, conversationId,
                        input.getExecuteId(), loop,
                        plannerResp.getAction(), result);

                // 5i. Check signal
                ReActSignal signal = result.getSignal();
                if (signal == ReActSignal.FINISH) {
                    state.getRuntime().setStatus("FINISH");
                    finalAnswer = formatFinalOutput(result);
                    break;
                } else if (signal == ReActSignal.ERROR) {
                    // On error, Planner can decide whether to try a different approach
                    // Continue to next loop — Planner will see the error observation
                    log.warn("Tool returned ERROR, Planner will decide next: toolId={}, msg={}",
                            result.getSourceId(), result.getMessage());
                }
                // else CONTINUE — go to next loop
            }

            // 6. Check if maxLoop reached without FINAL
            if (finalAnswer == null) {
                state.getRuntime().setStatus("FINISH");
                finalAnswer = buildTimeoutAnswer(state);
            }

        } finally {
            // 7. Archive current observations to history
            state.getObservations().getHistory().addAll(state.getObservations().getCurrent());
            state.getObservations().setCurrent(new ArrayList<>());

            // 8. Save state to Redis
            stateManager.save(state);
        }

        // 9. Build response
        long totalCost = System.currentTimeMillis() - startTime;
        ReActResponse response = new ReActResponse();
        response.setFinalAnswer(finalAnswer != null ? finalAnswer : "处理完成，但未能生成最终答案。");
        response.setTotalUsedTokens(totalTokens);
        response.setTotalCostTime(totalCost);
        response.setLoopCount(state.getPlanner().getLoop());
        response.setStatus(state.getRuntime().getStatus());

        log.info("ReActRuntime complete: appId={}, conversationId={}, loops={}, tokens={}, cost={}ms",
                appId, conversationId,
                state.getPlanner().getLoop() + 1, totalTokens, totalCost);

        return response;
    }

    /**
     * Apply memory mapping from ToolDefinition's memoryMapping config.
     */
    private void applyMemoryMappingForAction(ExecutionResult result, AgentState state) {
        if (result.getOutputs() == null || result.getOutputs().isEmpty()) return;
        // Load the tool definition to read its memoryMapping
        com.nova.agent.react.tool.ToolDefinition toolDef = toolRegistry.getById(result.getSourceId());
        if (toolDef != null) {
            toolRuntime.applyMemoryMapping(toolDef, result, state.getMemory());
        }
    }

    /**
     * Write all result outputs to memory as a fallback.
     */
    private void applyOutputsToMemory(ExecutionResult result, AgentState state) {
        if (result.getOutputs() == null || result.getOutputs().isEmpty()) return;
        for (Map.Entry<String, Object> entry : result.getOutputs().entrySet()) {
            if (entry.getValue() != null) {
                state.getMemory().put(entry.getKey(), entry.getValue());
            }
        }
    }

    private String formatFinalOutput(ExecutionResult result) {
        if (result.getOutputs() != null && !result.getOutputs().isEmpty()) {
            return JSON.toJSONString(result.getOutputs());
        }
        return result.getSummary();
    }

    private String buildTimeoutAnswer(AgentState state) {
        return "处理已超过最大步骤限制（" + state.getPlanner().getMaxLoop() + "步），请简化您的问题或稍后重试。";
    }

    /**
     * Response from ReActRuntime execution.
     */
    @Data
    public static class ReActResponse {
        private String finalAnswer;
        private Integer totalUsedTokens;
        private Long totalCostTime;
        private Integer loopCount;
        private String status;
    }
}
