package com.nova.agent.model.vo;

import lombok.Data;
import java.util.Map;

@Data
public class RunCodeReq {
    private String code;
    private Map<String, Object> params;
}
