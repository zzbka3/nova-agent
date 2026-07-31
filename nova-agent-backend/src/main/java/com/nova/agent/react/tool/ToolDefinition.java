package com.nova.agent.react.tool;

import lombok.Data;
import java.util.Date;

/**
 * Tool definition PO, maps to tool_definition table.
 */
@Data
public class ToolDefinition {
    private Long id;
    private String toolId;
    private String name;
    private String description;
    private String executorType;
    private String executorResourceId;
    private String inputSchema;
    private String outputSchema;
    private String inputMapping;
    private String outputMapping;
    private String memoryMapping;
    private Integer status;
    private String creator;
    private Date createTime;
    private String updater;
    private Date updateTime;
}
