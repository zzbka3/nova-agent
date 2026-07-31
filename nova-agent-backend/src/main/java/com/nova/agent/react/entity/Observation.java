package com.nova.agent.react.entity;

import lombok.Data;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class Observation {
    private boolean success;
    private String sourceType;
    private String sourceId;
    private String summary;
    private Map<String, Object> outputs = new LinkedHashMap<>();
    private String signal;
    private String message;
}
