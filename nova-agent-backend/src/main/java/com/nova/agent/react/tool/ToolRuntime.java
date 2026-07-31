package com.nova.agent.react.tool;

import com.alibaba.fastjson.JSON;
import com.nova.agent.entity.UserInvokeInput;
import com.nova.agent.react.entity.*;
import com.nova.agent.react.enums.ReActSignal;
import com.nova.agent.react.enums.ToolExecutorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Unified Tool execution entry point.
 * Validates parameters, resolves missing values from memory,
 * and delegates to the appropriate executor (NodeAdapter for now).
 */
@Slf4j
@Component
public class ToolRuntime {

    @Autowired
    private ToolRegistry toolRegistry;

    @Autowired
    private NodeAdapter nodeAdapter;

    /**
     * Execute a tool call based on a Planner action.
     *
     * @param action   the action chosen by Planner
     * @param state    current agent state (for memory resolution)
     * @param input   user invocation input
     * @return standardized ExecutionResult
     */
    public ExecutionResult execute(ReActAction action, AgentState state,
                                    UserInvokeInput input) {
        String toolId = action.getTargetId();
        log.info("ToolRuntime execute: toolId={}, arguments={}", toolId, action.getArguments());

        // 1. Lookup tool definition
        ToolDefinition tool = toolRegistry.getById(toolId);
        if (tool == null) {
            return ExecutionResult.builder()
                    .success(false)
                    .sourceType("TOOL")
                    .sourceId(toolId)
                    .summary("Tool not found: " + toolId)
                    .signal(ReActSignal.ERROR)
                    .message("Tool definition not found for id: " + toolId)
                    .build();
        }

        // 2. Parse input schema to validate
        List<ToolInput> inputSchema = parseInputSchema(tool.getInputSchema());

        // 3. Validate required parameters
        Map<String, Object> args = action.getArguments() != null
                ? new LinkedHashMap<>(action.getArguments()) : new LinkedHashMap<>();
        ValidationResult validation = validateParams(inputSchema, args, state.getMemory());
        if (!validation.valid) {
            return ExecutionResult.builder()
                    .success(false)
                    .sourceType("TOOL")
                    .sourceId(toolId)
                    .summary("Parameter validation failed: " + tool.getName())
                    .signal(ReActSignal.ERROR)
                    .message(validation.message)
                    .build();
        }
        Map<String, Object> fullParams = validation.resolvedParams;

        // 4. Execute based on executor type
        try {
            ToolExecutorType executorType = ToolExecutorType.valueOf(
                    tool.getExecutorType().toUpperCase());

            ExecutionResult result = switch (executorType) {
                case NODE -> nodeAdapter.execute(tool, fullParams, input);
                default -> ExecutionResult.builder()
                        .success(false)
                        .sourceType("TOOL")
                        .sourceId(toolId)
                        .summary("Unsupported executor type: " + executorType)
                        .signal(ReActSignal.ERROR)
                        .message("Executor type not supported: " + executorType)
                        .build();
            };

            return result;
        } catch (Exception e) {
            log.error("Tool execution error: toolId={}", toolId, e);
            return ExecutionResult.builder()
                    .success(false)
                    .sourceType("TOOL")
                    .sourceId(toolId)
                    .summary(tool.getName() + " execution failed")
                    .signal(ReActSignal.ERROR)
                    .message(e.getMessage() != null ? e.getMessage() : "Execution error")
                    .build();
        }
    }

    /**
     * Apply memory mapping from a tool's execution result.
     */
    public void applyMemoryMapping(ToolDefinition tool, ExecutionResult result,
                                    Map<String, Object> memory) {
        if (tool.getMemoryMapping() == null || tool.getMemoryMapping().isEmpty()) return;
        if (result.getOutputs() == null || result.getOutputs().isEmpty()) return;
        nodeAdapter.applyMemoryMapping(result.getOutputs(), tool.getMemoryMapping(), memory);
    }

    /**
     * Build ToolSummary objects for Planner from tool definitions.
     */
    public List<PlannerRequest.ToolSummary> buildToolSummaries(List<String> toolIds) {
        return toolIds.stream()
                .map(toolRegistry::getById)
                .filter(t -> t != null)
                .map(ToolRuntime::toToolSummary)
                .toList();
    }

    // ---- private helpers ----

    private static class ValidationResult {
        boolean valid = true;
        String message;
        Map<String, Object> resolvedParams;

        static ValidationResult ok(Map<String, Object> params) {
            ValidationResult r = new ValidationResult();
            r.resolvedParams = params;
            return r;
        }

        static ValidationResult fail(String msg) {
            ValidationResult r = new ValidationResult();
            r.valid = false;
            r.message = msg;
            return r;
        }
    }

    private List<ToolInput> parseInputSchema(String inputSchemaJson) {
        if (inputSchemaJson == null || inputSchemaJson.isEmpty()) return List.of();
        return JSON.parseArray(inputSchemaJson).toJavaList(ToolInput.class);
    }

    private ValidationResult validateParams(List<ToolInput> schema,
                                             Map<String, Object> args,
                                             Map<String, Object> memory) {
        for (ToolInput input : schema) {
            if (input.isRequired() && !args.containsKey(input.getName())) {
                // Try to resolve from memory
                if (memory.containsKey(input.getName())) {
                    args.put(input.getName(), memory.get(input.getName()));
                    log.debug("Resolved param '{}' from memory: {}", input.getName(), memory.get(input.getName()));
                } else {
                    return ValidationResult.fail(
                            "Missing required parameter: " + input.getName()
                            + " (" + input.getDescription() + ")");
                }
            }
        }
        return ValidationResult.ok(args);
    }

    private static PlannerRequest.ToolSummary toToolSummary(ToolDefinition def) {
        PlannerRequest.ToolSummary summary = new PlannerRequest.ToolSummary();
        summary.setId(def.getToolId());
        summary.setName(def.getName());
        summary.setDescription(def.getDescription());

        List<ToolInput> inputs = JSON.parseArray(def.getInputSchema()).toJavaList(ToolInput.class);
        summary.setInputs(inputs.stream().map(in -> {
            PlannerRequest.ToolSummary.InputDef d = new PlannerRequest.ToolSummary.InputDef();
            d.setName(in.getName());
            d.setType(in.getType());
            d.setRequired(in.isRequired());
            d.setDescription(in.getDescription());
            return d;
        }).toList());

        return summary;
    }
}
