package com.nova.agent.react.entity;

import lombok.Data;

@Data
public class PlannerResponse {
    private String thought;
    private ReActAction action;
    private Integer usedTokens;
}
