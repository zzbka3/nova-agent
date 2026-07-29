package com.nova.agent.service;

import com.alibaba.fastjson.JSONObject;
import com.nova.agent.controller.request.AgentEditRequest;
import com.nova.agent.entity.AgentFlow;
import com.nova.agent.entity.AgentFlowOutput;
import com.nova.agent.entity.MemoryVar;
import com.nova.agent.entity.UserInvokeInput;
import com.nova.agent.enums.AgentRunningStatus;
import com.nova.agent.model.po.Agent;
import com.nova.agent.model.po.AgentEdgeExecuteLog;
import com.nova.agent.model.po.AgentExecuteLog;
import com.nova.agent.model.po.AgentNodeExecuteLog;
import com.nova.agent.model.vo.AgentVo;
import com.nova.agent.model.vo.ApiDebugReq;
import com.nova.agent.model.vo.ApiVerifyReq;
import com.nova.agent.model.vo.CanvasInitInfoVO;
import com.nova.agent.model.vo.Result;
import com.nova.agent.model.vo.SupportLlmVO;
import com.github.pagehelper.PageInfo;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Agent 工作流核心服务接口。
 *
 * <h3>主要功能模块</h3>
 * <ul>
 *   <li><b>生命周期管理</b>：创建、更新、删除、发布、复制、导入/导出智能体</li>
 *   <li><b>执行引擎</b>：构造 DAG、校验配置、执行工作流</li>
 *   <li><b>会话管理</b>：生成会话 ID、保存/查询对话历史</li>
 *   <li><b>日志追踪</b>：节点执行日志、边执行日志、Token 统计</li>
 *   <li><b>辅助功能</b>：文件上传、模型列表、API 调试、代码调试</li>
 * </ul>
 *
 * @see AgentFlowServiceImpl
 * @see com.nova.agent.entity.AgentFlow
 */
public interface AgentFlowService {

    
    /**
     * 从 JSON 配置构造工作流 DAG 实例。
     *
     * @param config JSON 格式的工作流配置
     * @return 构造好的 AgentFlow 实例
     */
    AgentFlow constructGraphFromConfig(String config);

    
    /**
     * 校验工作流配置的合法性（环检测、连通性、依赖关系等）。
     *
     * @param config 工作流 JSON 配置
     * @param appId  智能体 ID（用于检查 WORKFLOW_AGENT 依赖）
     * @return 校验结果，成功返回 1
     */
    Result<Integer> validate(String config, String appId);

    
    /**
     * 执行工作流实例。
     *
     * @param agentFlow 已构造的工作流实例
     * @param userInvokeInput 用户输入
     * @return 执行结果（回答文本、Token 数、耗时）
     */
    AgentFlowOutput executeAgentFlow(AgentFlow agentFlow, UserInvokeInput userInvokeInput);

    Agent queryAgentById(String appId);

    Long newConversationHistory(String appId, String conversationId, UserInvokeInput userInvokeInput);

    List<AgentExecuteLog> queryConversationHistory(String appId, String conversationId, Integer turns);

    void updateConversationHistory(Long id, AgentFlowOutput output);

    void generateConversation(String appId, String conversationId);

    void updateAgentFlowStatus(Long executeId, AgentRunningStatus status, String exceptionMsg);

    void insertAgentNodeExecuteLog(AgentNodeExecuteLog nodeExecuteLog);

    void updateAgentNodeExecuteLog(AgentNodeExecuteLog nodeExecuteLog);

    void updateEdgeToDb(AgentEdgeExecuteLog executeLog);

    AgentExecuteLog getLastConversationHistory(String appId, String conversationId, Long timestamp);

    List<AgentNodeExecuteLog> getAgentNodeLog(String appId, String conversationId, Long executeId);

    Integer countUsedTokens(String appId, String conversationId, Long executeId);

    List<AgentEdgeExecuteLog> getAgentEdgeLog(String appId, String conversationId, Long executeId);

    Result<String> updateAgent(AgentEditRequest editRequest);

    Result<String> insertAgent(AgentEditRequest editRequest);

    Result<Integer> publish(String appId);

    PageInfo<AgentVo> listAgent(String query, Integer isPublished, Integer agentType, Integer page, Integer pageSize);

    CanvasInitInfoVO initInfo();

    String copy(String appId);

    void export(String appId, HttpServletResponse response);

    String importAgent(MultipartFile file);

    void delete(String appId);

    JSONObject apiDebug(ApiDebugReq apiDebugReq);

    List<SupportLlmVO> listSupportModels();

    List<SupportLlmVO> listSupportModelsForIndependentPlanning();

    AgentVo getAgent(String appId);

    void verifyRequestJsonSchema(List<ApiVerifyReq> apiVerifyReqList);

    void verifyResponseJsonSchema(List<ApiVerifyReq> apiVerifyReqList);

    
    /**
     * 按 appId 查询 Debug 模式的智能体（优先使用草稿版本）。
     *
     * @param appId 智能体唯一标识
     * @return 草稿版本或已发布版本的 Agent
     */
    Agent queryDebugAgentById(String appId);

    Result<Object> codeDebug(String req);

    ConcurrentHashMap<String, MemoryVar> getMemoryVar(String appId, String schema);

    boolean verifyWorkflowAgent(String parentId, String childId);

    Boolean checkPublishAgent(String appId);

    Result<Long> latestPublishedTime(String appId);

    String convertPromptFormat(String promptMD, String promptJson, Integer transfer);
}
