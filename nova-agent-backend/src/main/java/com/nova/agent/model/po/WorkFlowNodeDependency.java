package com.nova.agent.model.po;

import lombok.Data;
import java.util.Date;

@Data
public class WorkFlowNodeDependency {
    private Long id;
    private String parentAgentId;
    private String childAgentId;
    private Date createTime;
}
