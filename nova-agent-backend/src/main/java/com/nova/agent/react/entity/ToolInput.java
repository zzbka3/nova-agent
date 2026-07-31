package com.nova.agent.react.entity;

import lombok.Data;

@Data
public class ToolInput {
    private String name;
    private String type;
    private boolean required;
    private String description;
}
