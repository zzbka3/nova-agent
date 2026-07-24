package com.nova.agent.model.po;

import lombok.Data;
import java.util.Date;

@Data
public class SupportLlm {
    private Long id;
    private String llmCode;
    private String displayName;
    private String modelServer;
    private String modelType;
    private Integer status;
    private Date createTime;
}
