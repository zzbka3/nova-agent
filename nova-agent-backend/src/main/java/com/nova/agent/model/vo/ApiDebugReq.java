package com.nova.agent.model.vo;

import lombok.Data;

@Data
public class ApiDebugReq {
    private String method;
    private String url;
    private String headers;
    private String body;
    private String authType;
    private String authToken;
}
