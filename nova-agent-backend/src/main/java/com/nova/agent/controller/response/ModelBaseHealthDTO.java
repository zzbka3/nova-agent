package com.nova.agent.controller.response;

import lombok.Data;

@Data
public class ModelBaseHealthDTO {
    private String model;
    private boolean healthy;
    private long responseTimeMs;
}
