package com.nova.agent.enums;

import lombok.Getter;

@Getter
public enum AgentStatus {
    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布");

    private final int code;
    private final String desc;

    AgentStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
