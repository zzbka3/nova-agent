package com.nova.agent.model.po;

import lombok.Data;
import java.util.Date;

@Data
public class Agent {
    private Long id;
    private String appId;
    private String name;
    private String config;
    private Integer status;
    private Integer agentType;
    private String memorySchema;
    private Integer referenceTurns;
    private String creator;
    private Date createTime;
    private String updater;
    private Date updateTime;
}
