package com.nova.agent.enums;

/**
 * 知识库检索策略。
 * <ul>
 *   <li>{@code SEMANTIC} — 语义检索（向量相似度）</li>
 *   <li>{@code KEYWORD} — 关键词检索（BM25）</li>
 *   <li>{@code HYBRID} — 混合检索（语义+关键词）</li>
 * </ul>
 */
public enum KnowledgeRetrievalType {
    SEMANTIC,
    KEYWORD,
    HYBRID
}
