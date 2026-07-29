package com.nova.agent.enums;

import lombok.Getter;

@Getter
/**
 * 节点执行状态枚举。
 *
 * <ul>
 *   <li>{@code INIT(0)} — 初始状态，尚未被触发</li>
 *   <li>{@code REACH(1)} — 已被触发，等待执行</li>
 *   <li>{@code RUNNING(2)} — 正在执���中</li>
 *   <li>{@code FINISH(3)} — 执行成功完成</li>
 *   <li>{@code EXCEPTION(4)} — 执行异常</li>
 * </ul>
 */
public enum NodeStatus {
    INIT(0),
    REACH(1),
    RUNNING(2),
    FINISH(3),
    EXCEPTION(4);

    private final int status;

    NodeStatus(int status) {
        this.status = status;
    }
}
