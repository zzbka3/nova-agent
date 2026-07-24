package com.nova.agent.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nova.agent.controller.request.AgentEditRequest;
import com.nova.agent.entity.AgentFlow;
import com.nova.agent.entity.AgentFlowOutput;
import com.nova.agent.entity.MemoryVar;
import com.nova.agent.entity.UserInvokeInput;
import com.nova.agent.entity.graph.Node;
import com.nova.agent.entity.graph.WorkflowAgentNode;
import com.nova.agent.enums.AgentRunningStatus;
import com.nova.agent.enums.AgentStatus;
import com.nova.agent.enums.NodeType;
import com.nova.agent.model.po.*;
import com.nova.agent.model.vo.*;
import com.nova.agent.repository.*;
import com.nova.agent.service.AgentFlowService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DirectedMultigraph;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service("agentFlowService")
public class AgentFlowServiceImpl implements AgentFlowService {

    @Autowired
    private AgentMapper agentMapper;

    @Autowired
    private AgentDraftMapper agentDraftMapper;

    @Autowired
    private AgentExecuteLogMapper agentExecuteLogMapper;

    @Autowired
    private AgentNodeExecuteLogMapper agentNodeExecuteLogMapper;

    @Autowired
    private AgentEdgeExecuteLogMapper agentEdgeExecuteLogMapper;

    @Autowired
    private ConversationMapper conversationMapper;

    @Autowired
    private SupportLlmMapper supportLlmMapper;

    @Autowired
    private DictionaryMapper dictionaryMapper;

    @Autowired
    private WorkFlowNodeDependencyMapper dependencyMapper;

    @Override
    public AgentFlow constructGraphFromConfig(String config) {
        return new AgentFlow(config);
    }

    @Override
    public Result<Integer> validate(String config, String appId) {
        AgentFlow agentFlow;
        try {
            agentFlow = new AgentFlow(config);
        } catch (Exception e) {
            log.error("agent config validate error!", e);
            return Result.error("Agent construction error!");
        }
        if (agentFlow == null) {
            return Result.error("Agent construction error!");
        }

        CycleDetector<Node, com.nova.agent.entity.graph.Edge> cycleDetector =
                new CycleDetector<>(agentFlow.getGraph());
        if (cycleDetector.detectCycles()) {
            return Result.error("Agent node configuration contains cycles!");
        }

        return Result.success(1);
    }

    @Override
    public AgentFlowOutput executeAgentFlow(AgentFlow agentFlow, UserInvokeInput userInvokeInput) {
        return agentFlow.fire(userInvokeInput);
    }

    @Override
    public Agent queryAgentById(String appId) {
        return agentMapper.selectByAppId(appId);
    }

    @Override
    public Long newConversationHistory(String appId, String conversationId, UserInvokeInput userInvokeInput) {
        AgentExecuteLog log = new AgentExecuteLog();
        log.setAppId(appId);
        log.setConversationId(conversationId);
        log.setRequest(JSON.toJSONString(userInvokeInput.getInputs()));
        log.setStatus(AgentRunningStatus.INIT.getStatus());
        log.setCreator("");
        log.setCreateTime(new Date());
        agentExecuteLogMapper.insert(log);
        return log.getId();
    }

    @Override
    public List<AgentExecuteLog> queryConversationHistory(String appId, String conversationId, Integer turns) {
        return agentExecuteLogMapper.selectByConversation(appId, conversationId, turns);
    }

    @Override
    public void updateConversationHistory(Long id, AgentFlowOutput output) {
        if (output != null) {
            agentExecuteLogMapper.updateResult(id, output.getAnswer());
        }
    }

    @Override
    public void generateConversation(String appId, String conversationId) {
        Conversation conversation = new Conversation();
        conversation.setAppId(appId);
        conversation.setConversationId(conversationId);
        conversation.setCreateTime(new Date());
        conversationMapper.insert(conversation);
    }

    @Override
    public void updateAgentFlowStatus(Long executeId, AgentRunningStatus status, String exceptionMsg) {
        agentExecuteLogMapper.updateStatus(executeId, status.getStatus(), exceptionMsg);
    }

    @Override
    public void insertAgentNodeExecuteLog(AgentNodeExecuteLog nodeExecuteLog) {
        if (nodeExecuteLog.getCreator() == null) nodeExecuteLog.setCreator("");
        if (nodeExecuteLog.getCreateTime() == null) nodeExecuteLog.setCreateTime(new Date());
        agentNodeExecuteLogMapper.insert(nodeExecuteLog);
    }

    @Override
    public void updateAgentNodeExecuteLog(AgentNodeExecuteLog nodeExecuteLog) {
        agentNodeExecuteLogMapper.updateById(nodeExecuteLog);
    }

    @Override
    public void updateEdgeToDb(AgentEdgeExecuteLog executeLog) {
        agentEdgeExecuteLogMapper.insert(executeLog);
    }

    @Override
    public AgentExecuteLog getLastConversationHistory(String appId, String conversationId, Long timestamp) {
        return agentExecuteLogMapper.selectLastByConversation(appId, conversationId, timestamp);
    }

    @Override
    public List<AgentNodeExecuteLog> getAgentNodeLog(String appId, String conversationId, Long executeId) {
        return agentNodeExecuteLogMapper.selectByExecuteId(appId, conversationId, executeId);
    }

    @Override
    public Integer countUsedTokens(String appId, String conversationId, Long executeId) {
        Integer tokens = agentExecuteLogMapper.sumUsedTokens(appId, conversationId, executeId);
        return tokens != null ? tokens : 0;
    }

    @Override
    public List<AgentEdgeExecuteLog> getAgentEdgeLog(String appId, String conversationId, Long executeId) {
        return agentEdgeExecuteLogMapper.selectByExecuteId(appId, conversationId, executeId);
    }

    @Override
    @Transactional
    public Result<String> updateAgent(AgentEditRequest editRequest) {
        AgentDraft draft = new AgentDraft();
        BeanUtils.copyProperties(editRequest, draft);
        draft.setUpdater("");
        draft.setUpdateTime(new Date());
        agentDraftMapper.upsert(draft);
        return Result.success(editRequest.getAppId());
    }

    @Override
    @Transactional
    public Result<String> insertAgent(AgentEditRequest editRequest) {
        String appId = UUID.randomUUID().toString().replace("-", "");
        Agent agent = new Agent();
        agent.setAppId(appId);
        agent.setName(editRequest.getName());
        agent.setConfig(editRequest.getConfig());
        agent.setAgentType(editRequest.getAgentType() != null ? editRequest.getAgentType() : 0);
        agent.setStatus(AgentStatus.DRAFT.getCode());
        agent.setMemorySchema(editRequest.getMemorySchema());
        agent.setReferenceTurns(editRequest.getReferenceTurns() != null ? editRequest.getReferenceTurns() : 5);
        agent.setCreator("");
        agent.setCreateTime(new Date());
        agentMapper.insert(agent);

        // Also create a draft
        AgentDraft draft = new AgentDraft();
        BeanUtils.copyProperties(agent, draft);
        agentDraftMapper.insert(draft);

        return Result.success(appId);
    }

    @Override
    @Transactional
    public Result<Integer> publish(String appId) {
        AgentDraft draft = agentDraftMapper.selectByAppId(appId);
        if (draft == null) {
            return Result.error("No draft found for appId: " + appId);
        }
        Agent agent = new Agent();
        BeanUtils.copyProperties(draft, agent);
        agent.setStatus(AgentStatus.PUBLISHED.getCode());
        agent.setUpdateTime(new Date());
        agentMapper.updateByAppId(agent);
        return Result.success(1);
    }

    @Override
    public PageInfo<AgentVo> listAgent(String query, Integer isPublished, Integer agentType,
                                        Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        List<Agent> agents = agentMapper.selectByQuery(query, isPublished, agentType);
        List<AgentVo> vos = agents.stream().map(a -> {
            AgentVo vo = new AgentVo();
            BeanUtils.copyProperties(a, vo);
            return vo;
        }).collect(Collectors.toList());
        return new PageInfo<>(vos);
    }

    @Override
    public CanvasInitInfoVO initInfo() {
        CanvasInitInfoVO vo = new CanvasInitInfoVO();
        vo.setSupportModels(listSupportModels());
        List<String> nodeTypes = Arrays.stream(NodeType.values())
                .map(Enum::name).collect(Collectors.toList());
        vo.setNodeTypes(nodeTypes);
        return vo;
    }

    @Override
    @Transactional
    public String copy(String appId) {
        Agent agent = agentMapper.selectByAppId(appId);
        if (agent == null) return null;
        String newAppId = UUID.randomUUID().toString().replace("-", "");
        agent.setAppId(newAppId);
        agent.setId(null);
        agent.setName(agent.getName() + " (Copy)");
        agent.setStatus(AgentStatus.DRAFT.getCode());
        agent.setCreateTime(new Date());
        agent.setUpdateTime(new Date());
        agentMapper.insert(agent);
        return newAppId;
    }

    @Override
    public void export(String appId, HttpServletResponse response) {
        Agent agent = agentMapper.selectByAppId(appId);
        if (agent == null) return;
        try {
            byte[] bytes = agent.getConfig().getBytes(StandardCharsets.UTF_8);
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + appId + ".json");
            OutputStream os = response.getOutputStream();
            os.write(bytes);
            os.flush();
        } catch (Exception e) {
            log.error("export error!", e);
        }
    }

    @Override
    @Transactional
    public String importAgent(MultipartFile file) {
        try {
            String config = new String(file.getBytes(), StandardCharsets.UTF_8);
            AgentEditRequest req = new AgentEditRequest();
            req.setName(file.getOriginalFilename());
            req.setConfig(config);
            return insertAgent(req).getData();
        } catch (Exception e) {
            log.error("importAgent error!", e);
            return null;
        }
    }

    @Override
    @Transactional
    public void delete(String appId) {
        agentMapper.deleteByAppId(appId);
        agentDraftMapper.deleteByAppId(appId);
    }

    @Override
    public JSONObject apiDebug(ApiDebugReq apiDebugReq) {
        // TODO: Implement actual API debug logic
        JSONObject result = new JSONObject();
        result.put("status", 200);
        result.put("body", "{}");
        return result;
    }

    @Override
    public List<SupportLlmVO> listSupportModels() {
        List<SupportLlm> models = supportLlmMapper.selectAll();
        return models.stream().map(m -> {
            SupportLlmVO vo = new SupportLlmVO();
            BeanUtils.copyProperties(m, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<SupportLlmVO> listSupportModelsForIndependentPlanning() {
        return listSupportModels();
    }

    @Override
    public AgentVo getAgent(String appId) {
        Agent agent = agentMapper.selectByAppId(appId);
        if (agent == null) return null;
        AgentVo vo = new AgentVo();
        BeanUtils.copyProperties(agent, vo);
        return vo;
    }

    @Override
    public void verifyRequestJsonSchema(List<ApiVerifyReq> apiVerifyReqList) {
        // TODO: JSON Schema validation
    }

    @Override
    public void verifyResponseJsonSchema(List<ApiVerifyReq> apiVerifyReqList) {
        // TODO: JSON Schema validation
    }

    @Override
    public Agent queryDebugAgentById(String appId) {
        AgentDraft draft = agentDraftMapper.selectByAppId(appId);
        if (draft != null) {
            Agent agent = new Agent();
            BeanUtils.copyProperties(draft, agent);
            return agent;
        }
        return queryAgentById(appId);
    }

    @Override
    public Result<Object> codeDebug(String req) {
        // TODO: implement code debug
        return Result.success("OK");
    }

    @Override
    public ConcurrentHashMap<String, MemoryVar> getMemoryVar(String appId, String schema) {
        return new ConcurrentHashMap<>();
    }

    @Override
    public boolean verifyWorkflowAgent(String parentId, String childId) {
        // Prevent circular dependencies
        if (parentId.equals(childId)) return false;
        // TODO: Check dependency chain
        return true;
    }

    @Override
    public Boolean checkPublishAgent(String appId) {
        Agent agent = agentMapper.selectByAppId(appId);
        return agent != null && AgentStatus.PUBLISHED.getCode() == agent.getStatus();
    }

    @Override
    public Result<Long> latestPublishedTime(String appId) {
        Long time = agentMapper.selectLatestPublishedTime(appId);
        return Result.success(time);
    }

    @Override
    public String convertPromptFormat(String promptMD, String promptJson, Integer transfer) {
        if (transfer == 1) {
            // JSON to Markdown
            return promptJson != null ? promptJson : "";
        } else {
            // Markdown to JSON
            return promptMD != null ? promptMD : "";
        }
    }
}
