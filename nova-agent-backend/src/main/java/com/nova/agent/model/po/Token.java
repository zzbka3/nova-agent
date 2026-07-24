package com.nova.agent.model.po;

import lombok.Data;
import java.util.Date;

@Data
public class Token {
    private Long id;
    private String token;
    private Long accountId;
    private String permission;
    private Date expireTime;
    private Date createTime;
}
