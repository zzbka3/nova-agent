package com.nova.agent.enums;

/**
 * Variable type enumeration
 */
/**
 * 变量类型枚举。
 *
 * <p>定义了工作流节点输入/输出参数的取值范围：
 * <ul>
 *   <li>基础类型：{@code String, Integer, Number, Boolean, Object}</li>
 *   <li>数组类型：{@code ArrayString, ArrayInteger, ArrayNumber, ArrayBoolean, ArrayObject, ArrayAny}</li>
 *   <li>特殊类型：{@code Any}（匹配任意类型），{@code reference}（引用其他节点的输出变量）</li>
 * </ul>
 */
public enum VarType {
    String,
    Integer,
    Number,
    Boolean,
    Object,
    ArrayString,
    ArrayInteger,
    ArrayNumber,
    ArrayBoolean,
    ArrayObject,
    ArrayAny,
    Any,
    reference
}
