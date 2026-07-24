package com.nova.agent.exception;

public class AgentFlowConstructException extends RuntimeException {
    public AgentFlowConstructException(String message) {
        super(message);
    }

    public AgentFlowConstructException(String message, Throwable cause) {
        super(message, cause);
    }
}
