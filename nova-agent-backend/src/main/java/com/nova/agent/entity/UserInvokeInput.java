package com.nova.agent.entity;

import com.nova.agent.model.po.AgentExecuteLog;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class UserInvokeInput {
    /** Agent app ID */
    private String app_id;
    /** Conversation ID */
    private String conversation_id;
    /** Request ID for tracing */
    private String requestId;
    /** User inputs key-value pairs */
    private Map<String, Object> inputs;
    /** User query text */
    private String query;
    /** Debug mode flag: 0=normal, 1=debug */
    private Integer debug;
    /** Execution record ID */
    private Long executeId;
    /** Historical conversation records */
    private List<AgentExecuteLog> realHistory;
    /** Extra history for appending */
    private String extraHistory;
    /** File URLs uploaded by user */
    private List<String> fileUrls;
}
