package com.nova.agent.controller.request;

import lombok.Data;

@Data
public class KnowledgeDocCreatRequest {
    private String knowledgeId;
    private String documentUrl;
    private String documentName;
}
