package com.nova.agent.controller.request;

import lombok.Data;
import java.util.Map;

@Data
public class TalkAgentReq {
    private String app_id;
    private String conversation_id;
    private Map<String, Object> inputs;
    private String query;
    private Integer debug;
}
