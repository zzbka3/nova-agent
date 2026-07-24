package com.nova.agent.llm;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ModelFallback {
    private String fallbackModelName;
    private String fallbackModelServer;
}
