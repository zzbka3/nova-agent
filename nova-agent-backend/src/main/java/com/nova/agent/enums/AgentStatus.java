package com.nova.agent.enums;

import lombok.Getter;

@Getter
/**
 * 智能体发布状态。
 * <ul>
 *   <li>{@code DRAFT(0)} — 草稿，尚未发布</li>
 *   <li>{@code PUBLISHED(1)} — 已发布</li>
 * </ul>
 */
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
