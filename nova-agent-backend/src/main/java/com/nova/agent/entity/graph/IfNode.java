package com.nova.agent.entity.graph;

import com.alibaba.fastjson.JSON;
import com.nova.agent.constant.AgentFlowContextVar;
import com.nova.agent.entity.AgentFlow;
import com.nova.agent.entity.IFCondition;
import com.nova.agent.entity.InputVar;
import com.nova.agent.entity.OutPutVar;
import com.nova.agent.enums.IfNodeOpType;
import com.nova.agent.enums.NodeType;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 条件分支节点。
 *
 * <p>根据输入变量的值评估条件列表，决定走哪条分支路径。
 *
 * <p>条件配置包含：
 * <ul>
 *   <li>{@code conditionList}：多个条件组，每组有内部逻辑（AND/OR）和多个 InnerCondition</li>
 *   <li>{@code defaultTargetNodes}：兜底分支，没有任何条件命中时走此路径</li>
 * </ul>
 *
 * <p>每个 InnerCondition 由左操作数、运算符、右操作数组成，
 * 运算符包括 EQUAL、GT、CONTAINS、EMPTY 等 16 种（参见 {@link com.nova.agent.enums.IfNodeOpType}）。
 *
 * <p>执行结果：修改出边的 {@code conditionMatch} 字段（1=命中，0=未命中），
 * 后续由 {@link AgentFlow} 的调度逻辑决定走哪条边。
 */
@Slf4j
public class IfNode extends Node {

    public IfNode(String nodeId, String name, NodeType nodeType,
                  List<InputVar> inputVars, List<OutPutVar> outPutVars, String config) {
        super(nodeId, name, nodeType, inputVars, outPutVars, config);
    }

    @Override
    public void run(AgentFlow agentFlow) {
        log.debug("if node run, nodeId: {}", this.nodeId);

        IFCondition ifCondition = JSON.parseObject(config, IFCondition.class);
        List<IFCondition.ConditionEdge> defaultConditionList = ifCondition.getDefaultTargetNodes();
        List<IFCondition.IFConditionItem> conditionList = ifCondition.getConditionList();

        Set<Edge> targetEdges = agentFlow.getGraph().outgoingEdgesOf(this);

        IFCondition.IFConditionItem firstMatch = null;
        for (IFCondition.IFConditionItem item : conditionList) {
            List<String> edgeIds = item.getTargetNodes().stream()
                    .map(IFCondition.ConditionEdge::getEdgeId).toList();

            if (firstMatch == null) {
                String innerLogic = item.getInnerLogic();
                List<IFCondition.InnerCondition> innerConditions = item.getInnerConditions();
                List<Boolean> results = new ArrayList<>();
                for (IFCondition.InnerCondition condition : innerConditions) {
                    InputVar left = condition.getLeft();
                    InputVar right = condition.getRight();
                    agentFlow.fillInputVar(left);
                    agentFlow.fillInputVar(right);
                    IfNodeOpType op = condition.getOp();
                    results.add(agentFlow.evalCondition(left, right, op));
                }
                boolean match = calculate(results, innerLogic);
                if (match) {
                    firstMatch = item;
                    agentFlow.updateEdge(targetEdges, edgeIds,
                            AgentFlowContextVar.EDGE_DEFAULT_CONDITION, 1);
                } else {
                    agentFlow.updateEdge(targetEdges, edgeIds,
                            AgentFlowContextVar.EDGE_DEFAULT_NOMATCH_CONDITION, 0);
                }
            } else {
                agentFlow.updateEdge(targetEdges, edgeIds,
                        AgentFlowContextVar.EDGE_DEFAULT_NOMATCH_CONDITION, 0);
            }
        }

        // Default branch
        List<String> defaultEdgeIds = defaultConditionList.stream()
                .map(IFCondition.ConditionEdge::getEdgeId).toList();
        if (firstMatch == null) {
            agentFlow.updateEdge(targetEdges, defaultEdgeIds,
                    AgentFlowContextVar.EDGE_DEFAULT_CONDITION, 1);
        } else {
            agentFlow.updateEdge(targetEdges, defaultEdgeIds,
                    AgentFlowContextVar.EDGE_DEFAULT_NOMATCH_CONDITION, 0);
        }
    }

    private boolean calculate(List<Boolean> results, String innerLogic) {
        if ("OR".equals(innerLogic)) {
            for (Boolean result : results) {
                if (result) return true;
            }
            return false;
        }
        if ("AND".equals(innerLogic)) {
            for (Boolean result : results) {
                if (!result) return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public void fillOutputVar(AgentFlow agentFlow) {
        // IfNode does not produce output variables
    }
}
