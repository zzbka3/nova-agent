package com.nova.agent.entity;

import lombok.Data;

@Data
public class AgentFlowOutput {
    /** Final answer text */
    private String answer;
    /** Request ID for tracing */
    private String requestId;
    /** Total used tokens across all LLM calls */
    private Integer totalUsedTokens;
    /** Total cost time in milliseconds */
    private Long totalCostTime;
}
