package com.nova.agent.react.trace;

import lombok.Data;
import java.util.Date;

@Data
public class PlannerTrace {
    private Long id;
    private String appId;
    private String conversationId;
    private Long executeId;
    private Integer loopIndex;
    private String traceType;
    private String thought;
    private String actionType;
    private String actionTarget;
    private String actionArguments;
    private String resultSummary;
    private Integer resultSuccess;
    private String resultOutputs;
    private String signal;
    private Integer costTime;
    private Integer usedTokens;
    private Date createTime;
}
