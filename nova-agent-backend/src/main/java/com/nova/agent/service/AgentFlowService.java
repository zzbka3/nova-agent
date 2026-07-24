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

public interface AgentFlowService {

    AgentFlow constructGraphFromConfig(String config);

    Result<Integer> validate(String config, String appId);

    AgentFlowOutput executeAgentFlow(AgentFlow agentFlow, UserInvokeInput userInvokeInput);

    Agent queryAgentById(String appId);

    Long newConversationHistory(String appId, String conversationId, UserInvokeInput userInvokeInput);

    List<AgentExecuteLog> queryConversationHistory(String appId, String conversationId, Integer turns);

    void updateConversationHistory(Long id, AgentFlowOutput output);

    void generateConversation(String appId, String conversationId);

    String saveFile(String appId, String conversationId, String fileName, String url);

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

    Agent queryDebugAgentById(String appId);

    Result<Object> codeDebug(String req);

    ConcurrentHashMap<String, MemoryVar> getMemoryVar(String appId, String schema);

    boolean verifyWorkflowAgent(String parentId, String childId);

    Boolean checkPublishAgent(String appId);

    Result<Long> latestPublishedTime(String appId);

    String convertPromptFormat(String promptMD, String promptJson, Integer transfer);
}
