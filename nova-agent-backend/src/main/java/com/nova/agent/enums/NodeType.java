package com.nova.agent.enums;

/**
 * Workflow node type enumeration
 */
public enum NodeType {
    START,
    END,
    LLM,
    IF,
    LOOP,
    JUMP_LOOP,
    KNOWLEDGE,
    MESSAGE,
    API,
    INTENT,
    WORKFLOW,
    MCP,
    CODE,
    REWRITE,
    TEXT_PROCESSOR,
    MEMORY,
    WORKFLOW_AGENT
}
