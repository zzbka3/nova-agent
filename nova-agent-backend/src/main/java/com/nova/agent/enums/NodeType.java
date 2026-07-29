package com.nova.agent.enums;

/**
 * Workflow node type enumeration
 */
/**
 * 工作流节点类型枚举。
 *
 * <p>定义了画布上可用的所有节点类型，每个类型对应一个 Node 子类实现。
 *
 * <ul>
 *   <li>{@code START / END} — 流程起止节点</li>
 *   <li>{@code LLM} — 大语言模型调用（{@link com.nova.agent.entity.graph.LLMNode}）</li>
 *   <li>{@code IF} — 条件分支判断（{@link com.nova.agent.entity.graph.IfNode}）</li>
 *   <li>{@code KNOWLEDGE} — 知识库检索（{@link com.nova.agent.entity.graph.KnowledgeNode}）</li>
 *   <li>{@code API} — HTTP API 调用（{@link com.nova.agent.entity.graph.ApiNode}）</li>
 *   <li>{@code CODE} — Python 代码沙箱（{@link com.nova.agent.entity.graph.CodeNode}）</li>
 *   <li>{@code MESSAGE} — 消息输出（{@link com.nova.agent.entity.graph.MessageNode}）</li>
 *   <li>{@code INTENT} — 意图识别/路由（{@link com.nova.agent.entity.graph.IntentNode}）</li>
 *   <li>{@code WORKFLOW} — 子工作流（{@link com.nova.agent.entity.graph.WorkflowNode}）</li>
 *   <li>{@code WORKFLOW_AGENT} — 工作流 Agent（{@link com.nova.agent.entity.graph.WorkflowAgentNode}）</li>
 *   <li>{@code MCP} — MCP 协议工具调用（{@link com.nova.agent.entity.graph.McpNode}）</li>
 *   <li>{@code REWRITE} — 查询改写（{@link com.nova.agent.entity.graph.QueryRewriteNode}）</li>
 *   <li>{@code TEXT_PROCESSOR} — 文本处理（{@link com.nova.agent.entity.graph.TextProcessorNode}）</li>
 *   <li>{@code MEMORY} — 记忆管理（{@link com.nova.agent.entity.graph.MemoryNode}）</li>
 * </ul>
 */
public enum NodeType {
    START,
    END,
    LLM,
    IF,
    KNOWLEDGE,
    MESSAGE,
    API,
    INTENT,
    WORKFLOW,
    MCP,
    CODE,
    REWRITE,
    TEXT_PROCESSOR,
    MEMORY,
    WORKFLOW_AGENT
}
