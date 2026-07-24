package com.nova.agent.entity.graph;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nova.agent.constant.AgentFlowContextVar;
import com.nova.agent.entity.AgentFlow;
import com.nova.agent.entity.InputVar;
import com.nova.agent.entity.ObjectStructure;
import com.nova.agent.entity.OutPutVar;
import com.nova.agent.enums.KnowledgeRetrievalType;
import com.nova.agent.enums.NodeType;
import com.nova.agent.enums.VarType;
import com.nova.agent.utils.HttpUtils;
import com.nova.agent.utils.SpringContextUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
public class KnowledgeNode extends Node {
    private List<String> knowledgeIds;
    private KnowledgeRetrievalType strategy;
    private Integer top;
    private Float rankScoreThreshold;

    public KnowledgeNode(String id, String name, NodeType nodeType,
                         List<InputVar> inputVars, List<OutPutVar> outPutVars, String config) {
        super(id, name, nodeType, inputVars, outPutVars, config);
        KnowledgeConfig kc = JSON.parseObject(config, KnowledgeConfig.class);
        this.knowledgeIds = kc.getKnowledgeBaseId().stream()
                .map(KnowledgeBase::getKnowledgeId).toList();
        this.strategy = kc.getStrategy();
        this.top = kc.getReCallCount();
        if (kc.getReSort() != null) {
            this.rankScoreThreshold = kc.getReSort().getMatchScore();
        }
    }

    @Override
    public void run(AgentFlow agentFlow) {
        log.debug("knowledge node run, nodeId: {}", this.nodeId);
        String query = null;
        if (inputVars != null) {
            for (InputVar var : inputVars) {
                if (AgentFlowContextVar.KNOWLEDGE_DEFAULT_INPUT_VAR_NAME.equals(var.getVarName())) {
                    query = (String) var.getVarValue();
                    break;
                }
            }
        }
        if (query == null) {
            throw new RuntimeException(this.getNodeName() + ": Knowledge node must have a 'query' parameter");
        }

        try {
            // Use a simplified knowledge retrieval
            String uri = SpringContextUtils.getProperty("thirdService.knowledge.uri");
            String bns = SpringContextUtils.getProperty("thirdService.knowledge.bns");
            String token = SpringContextUtils.getProperty("thirdService.knowledge.token");

            JSONObject reqBody = new JSONObject();
            reqBody.put("knowledgeIds", knowledgeIds);
            reqBody.put("query", replaceVar(query));
            reqBody.put("top", top);

            String response = HttpUtils.doPost(bns + uri + "/query", reqBody.toJSONString(),
                    token != null ? token : "");
            agentFlow.setContextVar(AgentFlowContextVar.NODE_RESULT_PREFIX + this.nodeId, response);
        } catch (Exception e) {
            throw new RuntimeException(this.getNodeName() + ": Knowledge retrieval error! " + e.getMessage(), e);
        }
    }

    @Override
    public void fillOutputVar(AgentFlow agentFlow) {
        if (outputVars == null || outputVars.isEmpty()) return;

        String response = (String) agentFlow.getContextVar(
                AgentFlowContextVar.NODE_RESULT_PREFIX + this.nodeId);
        try {
            JSONObject json = JSON.parseObject(response);
            JSONArray data = json.getJSONArray("data");
            for (OutPutVar outVar : outputVars) {
                if (AgentFlowContextVar.KNOWLEDGE_DEFAULT_OUTPUT_VAR_NAME.equals(outVar.getVarName())) {
                    outVar.setVarValue(data != null ? data.toJavaList(Object.class) : List.of());
                    outVar.setVarType(VarType.ArrayObject);
                }
            }
        } catch (Exception e) {
            log.error("Knowledge node fill output error!", e);
        }
    }

    @Data public static class KnowledgeConfig {
        private List<KnowledgeBase> knowledgeBaseId;
        private KnowledgeRetrievalType strategy;
        private ReSortConfig reSort;
        private Integer reCallCount;
    }
    @Data public static class KnowledgeBase {
        private String name;
        private String id;
        private String knowledgeId;
        private String knowledgeName;
    }
    @Data public static class ReSortConfig {
        private Integer open;
        private String model;
        private Float matchScore;
    }
}
