package com.nova.agent.entity.graph;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Edge {
    /** Edge ID */
    private String id;
    /** Source node ID */
    private String fromNodeId;
    /** Target node ID */
    private String targetNodeId;
    /** Edge group (for IF branching) */
    private String group;
    /**
     * Condition match status:
     * -1 = not yet evaluated
     *  0 = condition NOT matched
     *  1 = condition matched
     */
    private int conditionMatch = -1;
    /** Condition expression */
    private String condition;

    public Edge(String id, String fromNodeId, String targetNodeId) {
        this.id = id;
        this.fromNodeId = fromNodeId;
        this.targetNodeId = targetNodeId;
    }

    @JSONField(serialize = false)
    public void conditionMatch() {
        if (condition == null) {
            this.conditionMatch = 1;
        }
    }
}
