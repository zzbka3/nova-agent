package com.nova.agent.model.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PrepareJsonReq {
    private String promptMD;
    private String promptJson;
    @NotNull
    private Integer transfer; // 1: json->md, 0: md->json
}
