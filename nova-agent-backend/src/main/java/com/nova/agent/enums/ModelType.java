package com.nova.agent.enums;

/**
 * LLM model type enumeration
 */
/**
 * LLM 模型类型。
 * <ul>
 *   <li>{@code LLM} — 纯文本大语言模型</li>
 *   <li>{@code VL} — 视觉-语言多模态模型</li>
 *   <li>{@code EMBEDDING} — 文本嵌入模型</li>
 * </ul>
 */
public enum ModelType {
    LLM,
    VL,
    EMBEDDING
}
