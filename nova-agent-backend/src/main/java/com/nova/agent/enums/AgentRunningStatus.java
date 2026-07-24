package com.nova.agent.enums;

import lombok.Getter;

@Getter
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
