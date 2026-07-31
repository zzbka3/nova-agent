package com.nova.agent.react.entity;

import com.nova.agent.react.enums.ReActSignal;
import lombok.Builder;
import lombok.Data;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Builder
public class ExecutionResult {
    private boolean success;
    private String sourceType;
    private String sourceId;
    private String summary;
    @Builder.Default
    private Map<String, Object> outputs = new LinkedHashMap<>();
    private ReActSignal signal;
    private String message;
    private Integer usedTokens;
    private Long costTime;
}
