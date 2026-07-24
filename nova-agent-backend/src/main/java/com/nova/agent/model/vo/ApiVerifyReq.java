package com.nova.agent.model.vo;

import lombok.Data;

@Data
public class ApiVerifyReq {
    private String jsonData;
    private String jsonSchema;
}
