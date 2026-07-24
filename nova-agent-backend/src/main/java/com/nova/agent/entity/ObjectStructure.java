package com.nova.agent.entity;

import com.nova.agent.enums.VarType;
import lombok.Data;

import java.util.List;

@Data
public class ObjectStructure {
    /** Field name */
    private String field;
    /** Field type */
    private VarType type;
    /** Child structure for nested objects */
    private List<ObjectStructure> children;
    /** Field description */
    private String description;
}
