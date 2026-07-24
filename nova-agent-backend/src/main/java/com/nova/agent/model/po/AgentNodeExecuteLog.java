package com.nova.agent.model.po;

import lombok.Data;
import java.util.Date;

@Data
public class AgentNodeExecuteLog {
    private Long id;
    private String appId;
    private String conversationId;
    private Long executeId;
    private String nodeId;
    private String nodeName;
    private String nodeType;
    private String inputVars;
    private String outputVars;
    private Integer status;
    private Integer costTime;
    private Integer usedTokens;
    private String exception;
    private String creator;
    private Date createTime;
    private String updater;
    private Date updateTime;
}
