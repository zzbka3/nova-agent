package com.nova.agent.model.vo;

import lombok.Data;

@Data
public class SupportLlmVO {
    private String llmCode;
    private String displayName;
    private String modelServer;
    private String modelType;
}
