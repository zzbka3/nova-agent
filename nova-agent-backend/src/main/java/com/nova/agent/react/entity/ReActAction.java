package com.nova.agent.react.entity;

import com.nova.agent.react.enums.ReActionType;
import lombok.Data;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class ReActAction {
    private ReActionType type;
    private String targetId;
    private Map<String, Object> arguments = new LinkedHashMap<>();
}
