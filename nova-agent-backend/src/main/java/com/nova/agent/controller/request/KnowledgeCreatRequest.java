package com.nova.agent.controller.request;

import lombok.Data;

@Data
public class KnowledgeCreatRequest {
    private String name;
    private String queryConfig;
    private String reRankConfig;
    private Integer reCallCount;
}
