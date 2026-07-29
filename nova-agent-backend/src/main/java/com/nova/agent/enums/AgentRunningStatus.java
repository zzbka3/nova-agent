package com.nova.agent.enums;

import lombok.Getter;

@Getter
/**
 * 工作流整体运行状态枚举。
 *
 * <ul>
 *   <li>{@code INIT(0)} — 初始化</li>
 *   <li>{@code RUNNING(1)} — 运行中</li>
 *   <li>{@code FINISH(2)} — 已完成</li>
 *   <li>{@code EXCEPTION(3)} — 异常终止</li>
 * </ul>
 */
public enum AgentRunningStatus {
    INIT(0),
    RUNNING(1),
    FINISH(2),
    EXCEPTION(3);

    private final int status;

    AgentRunningStatus(int status) {
        this.status = status;
    }
}
