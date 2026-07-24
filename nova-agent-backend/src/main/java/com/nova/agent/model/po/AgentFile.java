package com.nova.agent.model.po;

import lombok.Data;
import java.util.Date;

@Data
public class AgentFile {
    private Long id;
    private String appId;
    private String conversationId;
    private String fileName;
    private String fileUrl;
    private String fileId;
    private String creator;
    private Date createTime;
}
