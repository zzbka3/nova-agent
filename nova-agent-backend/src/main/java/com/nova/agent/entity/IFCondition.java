package com.nova.agent.entity;

import lombok.Data;

import java.util.List;

@Data
public class IFCondition {
    /** Default branch target nodes/edges */
    private List<ConditionEdge> defaultTargetNodes;
    /** Condition branch list */
    private List<IFConditionItem> conditionList;

    @Data
    public static class ConditionEdge {
        private String edgeId;
        private String nodeId;
        private String nodeName;
    }

    @Data
    public static class IFConditionItem {
        /** Internal logic: AND or OR */
        private String innerLogic;
        /** Inner conditions */
        private List<InnerCondition> innerConditions;
        /** Target nodes when this condition matches */
        private List<ConditionEdge> targetNodes;
    }

    @Data
    public static class InnerCondition {
        /** Left operand */
        private InputVar left;
        /** Comparison operator */
        private com.nova.agent.enums.IfNodeOpType op;
        /** Right operand */
        private InputVar right;
    }
}
