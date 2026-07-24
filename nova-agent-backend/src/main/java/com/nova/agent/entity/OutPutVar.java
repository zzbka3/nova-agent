package com.nova.agent.entity;

import com.nova.agent.enums.VarType;
import lombok.Data;

@Data
public class OutPutVar {
    /** Variable name */
    private String varName;
    /** Variable type */
    private VarType varType;
    /** Variable value */
    private Object varValue;
    /** Object structure for Object/ArrayObject types */
    private ObjectStructure structure;
    /** Variable description */
    private String varRemark;
}
