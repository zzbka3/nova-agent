package com.nova.agent.llm;

import com.nova.agent.model.po.AgentExecuteLog;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CallArgs {
    private String userPrompt;
    private String systemPrompt;
    private String model;
    private String modelType;
    private String modelServer;
    private Integer maxOutputTokens;
    private Double temperature;
    private String imageUrl;
    private List<AgentExecuteLog> histories;
    private Long accountId;
    private String requestId;
}
