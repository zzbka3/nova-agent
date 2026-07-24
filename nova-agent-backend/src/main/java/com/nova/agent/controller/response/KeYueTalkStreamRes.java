package com.nova.agent.controller.response;

import lombok.Data;

@Data
public class KeYueTalkStreamRes {
    private String text;
    private Integer status;
    private Integer totalTokens;
}
