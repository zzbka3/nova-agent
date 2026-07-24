package com.nova.agent.model.vo;

import lombok.Data;
import java.util.List;

@Data
public class CanvasInitInfoVO {
    private List<SupportLlmVO> supportModels;
    private List<String> nodeTypes;
}
