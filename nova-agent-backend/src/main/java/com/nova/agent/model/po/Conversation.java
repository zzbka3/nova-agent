package com.nova.agent.model.po;

import lombok.Data;
import java.util.Date;

@Data
public class Conversation {
    private Long id;
    private String appId;
    private String conversationId;
    private Date createTime;
}
