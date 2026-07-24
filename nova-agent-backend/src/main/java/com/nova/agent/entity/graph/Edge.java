package com.nova.agent.entity.graph;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Getter
@Setter
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

    /**
     * equals/hashCode based only on ID (immutable), required by JGraphT.
     * Using @Data would include mutable fields like conditionMatch,
     * which breaks graph internal lookups after conditionMatch() is called.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        return Objects.equals(id, edge.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
