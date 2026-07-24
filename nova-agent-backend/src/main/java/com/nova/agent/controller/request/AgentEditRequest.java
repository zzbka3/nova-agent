package com.nova.agent.controller.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AgentEditRequest {
    private String appId;
    @NotBlank(message = "Agent name is required")
    private String name;
    @NotBlank(message = "Agent config is required")
    private String config;
    private Integer agentType;
    private String memorySchema;
    private Integer referenceTurns;
}
