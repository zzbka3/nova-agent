package com.nova.agent.model.po;

import lombok.Data;
import java.util.Date;

@Data
public class Account {
    private Long accountId;
    private String accountName;
    private Integer status;
    private Date createTime;
}
