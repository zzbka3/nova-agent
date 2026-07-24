package com.nova.agent.model.po;

import lombok.Data;
import java.util.Date;

@Data
public class AgentExecuteLog {
    private Long id;
    private String appId;
    private String conversationId;
    private String request;
    private String response;
    private Integer status;
    private Integer totalUsedTokens;
    private Integer costTime;
    private String creator;
    private Date createTime;
    private String updater;
    private Date updateTime;
}
