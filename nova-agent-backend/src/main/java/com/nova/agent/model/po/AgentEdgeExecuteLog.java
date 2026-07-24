package com.nova.agent.model.po;

import lombok.Data;
import java.util.Date;

@Data
public class AgentEdgeExecuteLog {
    private Long id;
    private String appId;
    private String conversationId;
    private Long executeId;
    private String edgeId;
    private String fromNodeId;
    private String targetNodeId;
    private Integer conditionMatch;
    private String creator;
    private Date createTime;
    private String updater;
    private Date updateTime;
}
