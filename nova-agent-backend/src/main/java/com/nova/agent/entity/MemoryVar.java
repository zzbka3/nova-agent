package com.nova.agent.entity;

import lombok.Data;

@Data
public class MemoryVar {
    /** Variable name */
    private String varName;
    /** Variable value */
    private Object varValue;
    /** Variable type */
    private String varType;
    /** Last update timestamp */
    private long lastUpdateTime;
}
