package com.nova.agent.controller.request;

import lombok.Data;

@Data
public class VarInput {
    private String varName;
    private String varType;
    private Object varValue;
}
