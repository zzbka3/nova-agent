package com.nova.agent.entity;

import com.nova.agent.enums.VarType;
import lombok.Data;

@Data
public class InputVar {
    /** Input variable name */
    private String varName;
    /** Original variable type */
    private VarType originalVarType;
    /** Input variable type */
    private VarType varType;
    /** Input value */
    private Object varValue;
    /** Reference node ID */
    private String referenceNodeId;
    /** Reference variable name */
    private String referenceVarName;
    /** Reference variable type */
    private VarType referenceVarType;
}
