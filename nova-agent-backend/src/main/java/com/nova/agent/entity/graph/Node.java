package com.nova.agent.entity.graph;

import com.alibaba.fastjson.JSON;
import com.nova.agent.constant.AgentFlowContextVar;
import com.nova.agent.entity.AgentFlow;
import com.nova.agent.entity.InputVar;
import com.nova.agent.entity.OutPutVar;
import com.nova.agent.entity.UserInvokeInput;
import com.nova.agent.enums.AgentRunningStatus;
import com.nova.agent.enums.NodeStatus;
import com.nova.agent.enums.NodeType;
import com.nova.agent.enums.VarType;
import com.nova.agent.model.po.AgentNodeExecuteLog;
import com.nova.agent.service.AgentFlowService;
import com.nova.agent.utils.SpringContextUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 工作流节点的抽象基类。
 *
 * <h3>职责</h3>
 * <ul>
 *   <li>定义节点的通用属性：ID、名称、类型、输入/输出变量、执行状态等</li>
 *   <li>提供 {@link #execute(AgentFlow)} 模板方法，统一包装：入参填充 → 核心逻辑 → 出参回写 → 日志记录</li>
 *   <li>提供 {@link #replaceVar(String)} 工具方法，用 {{varName}} 占位符替换输入变量值</li>
 *   <li>提供 {@link #findInputVarValue(String, VarType)} 辅助方法，按名称+类型查找输入变量</li>
 * </ul>
 *
 * <h3>子类必须实现</h3>
 * <ul>
 *   <li>{@link #run(AgentFlow)} — 节点的核心业务逻辑</li>
 *   <li>{@link #fillOutputVar(AgentFlow)} — 将执行结果写入输出变量</li>
 * </ul>
 *
 * <h3>equals / hashCode</h3>
 * 仅基于不可变的 {@code nodeId}，满足 JGraphT 对图中顶点/边不可变 hashCode 的要求。
 *
 * @see com.nova.agent.entity.graph.LLMNode
 * @see com.nova.agent.entity.graph.IfNode
 * @see com.nova.agent.entity.AgentFlow
 */
@Data
@Slf4j
public abstract class Node {
    /** Node ID */
    public String nodeId;
    /** Node name */
    public String nodeName;
    /** Node type */
    public NodeType nodeType;
    /** Node input variables */
    public List<InputVar> inputVars;
    /** Node output variables */
    public List<OutPutVar> outputVars;
    /** Node configuration JSON string */
    public String config;
    /** Node execution status */
    public NodeStatus status = NodeStatus.INIT;
    /** Exception message */
    private String exceptionMsg;
    /** Used tokens count */
    public Integer useTokens = 0;

    public Node() {}

    public Node(String nodeId, String name, NodeType nodeType, List<InputVar> inputVars,
                List<OutPutVar> outPutVars, String config) {
        this.nodeId = nodeId;
        this.nodeName = name;
        this.nodeType = nodeType;
        this.config = config;
        this.inputVars = inputVars;
        this.outputVars = outPutVars;
    }

    /**
     * 模板方法：在当前工作流上下文中执行本节点。
     *
     * <p>执行流程：
     * <ol>
     *   <li>记录开始时间，从上下文获取用户输入信息</li>
     *   <li>初始化节点执行日志（状态=INIT）并写入数据库</li>
     *   <li>调用 {@link #fillInputVar(AgentFlow)} 从上下文中解析引用变量</li>
     *   <li>记录入参 JSON，更新日志状态为 RUNNING</li>
     *   <li>调用子类实现的 {@link #run(AgentFlow)} 执行核心逻辑</li>
     *   <li>调用子类实现的 {@link #fillOutputVar(AgentFlow)} 回写输出变量</li>
     *   <li>更新节点状态为 FINISH</li>
     *   <li>在 finally 块中更新日志：耗时、token 用量、状态、异常信息等</li>
     * </ol>
     *
     * <p>异常处理：如果任何步骤抛出异常，节点状态设为 EXCEPTION，
     * 同时整个 {@link AgentFlow} 也被标记为 EXCEPTION 状态。
     *
     * @param agentFlow 当前工作流实例，提供上下文变量存取和状态管理
     */
    public void execute(AgentFlow agentFlow) {
        long start = System.currentTimeMillis();
        UserInvokeInput invokeInput = (UserInvokeInput) agentFlow.getContextVar(
                AgentFlowContextVar.USER_INVOKE_INPUT);
        log.info("node execute start! conversationId: {}, requestId: {}, node: {}, type: {}",
                invokeInput.getConversation_id(), invokeInput.getRequestId(),
                this.nodeId, this.nodeType);

        AgentFlowService agentFlowService = SpringContextUtils.getBean(
                "agentFlowService", AgentFlowService.class);

        AgentNodeExecuteLog nodeExecuteLog = new AgentNodeExecuteLog();
        nodeExecuteLog.setAppId(invokeInput.getApp_id());
        nodeExecuteLog.setConversationId(invokeInput.getConversation_id());
        nodeExecuteLog.setExecuteId(invokeInput.getExecuteId());
        nodeExecuteLog.setNodeId(this.nodeId);
        nodeExecuteLog.setNodeName(this.nodeName);
        nodeExecuteLog.setNodeType(this.nodeType.name());
        nodeExecuteLog.setStatus(NodeStatus.INIT.getStatus());

        try {
            this.setStatus(NodeStatus.RUNNING);
            // Fill input variables from context
            fillInputVar(agentFlow);
            // Record input for logging
            nodeExecuteLog.setInputVars(inputVars == null ? "[]" : JSON.toJSONString(inputVars));
            nodeExecuteLog.setOutputVars("[]");
            nodeExecuteLog.setStatus(NodeStatus.RUNNING.getStatus());
            agentFlowService.insertAgentNodeExecuteLog(nodeExecuteLog);

            // Execute node logic
            run(agentFlow);
            // Fill output variables to context
            fillOutputVar(agentFlow);
            this.setStatus(NodeStatus.FINISH);
        } catch (Exception e) {
            log.error("node execute exception! conversationId: {}, requestId: {}, node: {}, type: {}",
                    invokeInput.getConversation_id(), invokeInput.getRequestId(),
                    this.nodeId, this.nodeType, e);
            setStatus(NodeStatus.EXCEPTION);
            setExceptionMsg(e.getMessage());
            agentFlow.setStatus(AgentRunningStatus.EXCEPTION);
            agentFlow.setExceptionMsg(e.getMessage());
        } finally {
            try {
                if (nodeExecuteLog.getId() != null) {
                    nodeExecuteLog.setCostTime((int) (System.currentTimeMillis() - start));
                    nodeExecuteLog.setUsedTokens(useTokens);
                    nodeExecuteLog.setInputVars(inputVars == null ? "[]" : JSON.toJSONString(inputVars));
                    nodeExecuteLog.setStatus(status.getStatus());
                    nodeExecuteLog.setException(exceptionMsg);
                    nodeExecuteLog.setOutputVars(outputVars == null ? "[]" : JSON.toJSONString(outputVars));
                    nodeExecuteLog.setUpdater("");
                    nodeExecuteLog.setUpdateTime(new Date());
                    agentFlowService.updateAgentNodeExecuteLog(nodeExecuteLog);
                }
            } catch (Exception e) {
                setStatus(NodeStatus.EXCEPTION);
                setExceptionMsg(e.getMessage());
                agentFlow.setStatus(AgentRunningStatus.EXCEPTION);
                agentFlow.setExceptionMsg(e.getMessage());
                log.error("save node log exception!", e);
            }
        }
        log.info("node execute end! node: {}, type: {}, cost: {}ms",
                this.nodeId, this.nodeType, System.currentTimeMillis() - start);
    }

    /**
     * 从工作流上下文中解析引用变量，填充到输入变量列表中。
     *
     * <p>遍历所有输入变量，对类型为 {@code VarType.reference} 的变量，
     * 调用 {@link AgentFlow#fillInputVar(InputVar)} 从前置节点的输出变量中查找匹配值。
     * 支持多级引用（如 {@code data.user.name}）。
     *
     * @param agentFlow 当前工作流实例
     */
    protected void fillInputVar(AgentFlow agentFlow) {
        if (inputVars != null && !inputVars.isEmpty()) {
            for (InputVar var : inputVars) {
                agentFlow.fillInputVar(var);
            }
        }
    }

    /**
     * 节点的核心业务逻辑，由子类实现。
     *
     * <p>典型实现：
     * <ul>
     *   <li>{@code LLMNode}：调用大模型 API，将结果写入上下文</li>
     *   <li>{@code ApiNode}：发送 HTTP 请求，将响应写入上下文</li>
     *   <li>{@code CodeNode}：通过 Python 沙箱执行代码</li>
     *   <li>{@code IfNode}：评估条件表达式，设置出边的匹配状态</li>
     *   <li>{@code KnowledgeNode}：检索知识库，返回检索片段</li>
     * </ul>
     *
     * <p>执行结果通常写入 {@code AgentFlow.context} 中，key 格式为
     * {@code NODE_RESULT_ + nodeId}，供下游节点的 {@link #fillOutputVar(AgentFlow)} 读取。
     *
     * @param agentFlow 当前工作流实例
     */
    public abstract void run(AgentFlow agentFlow);

    /**
     * 从上下文中读取本节点的执行结果，回写到输出变量列表中。
     *
     * <p>此方法在 {@link #run(AgentFlow)} 之后被调用。下游节点通过引用本节点的
     * outputVars 来获取执行结果。基类不提供默认实现，每个节点类型的解析逻辑不同：
     * <ul>
     *   <li>{@code LLMNode}：解析 API 返回的 JSON，提取 choices[0].message.content</li>
     *   <li>{@code ApiNode}：根据输出 schema 提取 HTTP 响应中的字段</li>
     *   <li>{@code CodeNode}：解析 Python 服务返回的 ok/result/error 协议</li>
     * </ul>
     *
     * @param agentFlow 当前工作流实例
     */
    public abstract void fillOutputVar(AgentFlow agentFlow);

    /**
     * 本节点是否为结束节点。默认 {@code false}，{@link EndNode} 覆写为 {@code true}。
     * 当执行到结束节点时，工作流将触发 {@link CountDownLatch} 并返回最终结果。
     *
     * @return true 表示本节点是工作流的最后一个节点
     */
    public boolean isEnd() {
        return false;
    }

    /**
     * 本节点是否为起始节点。默认 {@code false}，{@link StartNode} 覆写为 {@code true}。
     * 工作流始终从 START 节点开始执行。
     *
     * @return true 表示本节点是工作流的入口节点
     */
    public boolean isStart() {
        return false;
    }

    /**
     * 替换字符串中的模板变量占位符。
     *
     * <p>使用 Apache Commons Text {@code StringSubstitutor}，
     * 将 {@code {{varName}}} 格式的占位符替换为输入变量列表中对应变量的实际值。
     * 未匹配的占位符保留不变（不会抛异常）。替换后还会清理残留的 {@code {{...}}} 模式。
     *
     * <p>典型场景：LLM 节点的 systemPrompt/userPrompt 中的 {@code {{query}}}、
     * MessageNode 的 msg 模板中的 {@code {{llm_result}}} 等。
     *
     * @param input 包含模板占位符的原始字符串
     * @return 替换后的字符串
     */
    public String replaceVar(String input) {
        Map<String, Object> params = new HashMap<>();
        if (inputVars != null) {
            for (InputVar var : inputVars) {
                Object rawValue = var.getVarValue();
                String value = rawValue == null ? "" : String.valueOf(rawValue);
                if (value.contains("{{")) {
                    value = value.replaceAll("\\{\\{.*?}}", "");
                }
                params.put(var.getVarName(), value);
            }
        }
        StringSubstitutor sub = new StringSubstitutor(params, "{{", "}}");
        sub.setEnableUndefinedVariableException(false);
        String result = sub.replace(input);
        return result.replaceAll("\\{\\{.*?}}", "");
    }

    /**
     * 按名称和类型查找输入变量的值。
     *
     * <p>对于引用类型变量（{@code reference}），使用 {@code referenceVarType} 进行匹配。
     * 当 {@code paramType} 为 {@code Any} 时，不限制类型匹配。
     *
     * @param paramName 变量名称
     * @param paramType 期望的变量类型，{@code Any} 表示匹配任意类型
     * @return 变量值，未找到则返回 {@code null}
     */
    public Object findInputVarValue(String paramName, VarType paramType) {
        if (inputVars == null) return null;
        for (InputVar inputVar : inputVars) {
            VarType varType = inputVar.getVarType().equals(VarType.reference)
                    ? inputVar.getReferenceVarType() : inputVar.getVarType();
            if (inputVar.getVarName().equals(paramName)
                    && (varType.equals(paramType) || paramType.equals(VarType.Any))) {
                return inputVar.getVarValue();
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Objects.equals(nodeId, node.nodeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId);
    }
}
