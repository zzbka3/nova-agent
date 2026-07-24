package com.nova.agent.controller.request;

import com.nova.agent.enums.KnowledgeRetrievalType;
import lombok.Data;
import java.util.List;

@Data
public class KnowledgeQueryRequest {
    private List<String> knowledgeIds;
    private String query;
    private KnowledgeRetrievalType queryConfig2;
    private Integer top;
    private Float rankScoreThreshold;
    private String userId;
    private String extendType;
    private ReRankConfig reRankConfig;

    public void setQueryConfig2(KnowledgeRetrievalType t) { this.queryConfig2 = t; }
    public void setReRankConfig2(Integer open, String model) {
        if (open != null && open > 0 && model != null) {
            this.reRankConfig = new ReRankConfig();
            this.reRankConfig.open = open;
            this.reRankConfig.model = model;
        }
    }
    public void setChunkConfig2(String extendType) { this.extendType = extendType; }

    @Data
    public static class ReRankConfig {
        private Integer open;
        private String model;
    }
}
