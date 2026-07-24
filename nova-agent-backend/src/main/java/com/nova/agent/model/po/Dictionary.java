package com.nova.agent.model.po;

import lombok.Data;
import java.util.Date;

@Data
public class Dictionary {
    private Long id;
    private String code;
    private String value;
    private String description;
    private Date createTime;
}
