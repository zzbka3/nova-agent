package com.nova.agent.enums;

/**
 * IF node comparison operator enumeration
 */
/**
 * IF 节点条件运算符枚举。
 *
 * <p>支持 16 种比较运算：
 * <ul>
 *   <li>相等性：{@code EQUAL, NOT_EQUAL}</li>
 *   <li>大小比较：{@code GT, GT_EQUAL, LT, LT_EQUAL}</li>
 *   <li>包含性：{@code CONTAINS, NOT_CONTAINS}</li>
 *   <li>长度比较：{@code LENGTH_GT, LENGTH_GT_EQUAL, LENGTH_LT, LENGTH_LT_EQUAL}</li>
 *   <li>空判断：{@code EMPTY, NOT_EMPTY}</li>
 *   <li>布尔判断：{@code IS_TRUE, IS_FALSE}</li>
 * </ul>
 *
 * @see com.nova.agent.utils.ConditionUtils
 */
public enum IfNodeOpType {
    EQUAL,
    NOT_EQUAL,
    GT,
    GT_EQUAL,
    LT,
    LT_EQUAL,
    CONTAINS,
    NOT_CONTAINS,
    LENGTH_GT,
    LENGTH_GT_EQUAL,
    LENGTH_LT,
    LENGTH_LT_EQUAL,
    EMPTY,
    NOT_EMPTY,
    IS_TRUE,
    IS_FALSE
}
