package com.nova.agent.controller.response;

import com.nova.agent.model.po.AgentEdgeExecuteLog;
import com.nova.agent.model.po.AgentNodeExecuteLog;
import lombok.Data;
import java.util.List;

@Data
public class RunningInfoResponse {
    private Integer status;
    private Integer finish; // 0: running, 1: finished
    private List<AgentNodeExecuteLog> nodes;
    private List<AgentEdgeExecuteLog> edges;
}
