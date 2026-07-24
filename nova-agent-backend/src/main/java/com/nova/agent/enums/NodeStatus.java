package com.nova.agent.enums;

import lombok.Getter;

@Getter
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
