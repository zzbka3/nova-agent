package com.nova.agent.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.nova.agent.constant.ErrorCode;
import com.nova.agent.controller.request.AgentEditRequest;
import com.nova.agent.controller.request.RunningInfoRequest;
import com.nova.agent.controller.response.RunningInfoResponse;
import com.nova.agent.entity.AgentFlow;
import com.nova.agent.entity.AgentFlowOutput;
import com.nova.agent.entity.MemoryVar;
import com.nova.agent.entity.UserInvokeInput;
import com.nova.agent.enums.AgentRunningStatus;
import com.nova.agent.model.po.Agent;
import com.nova.agent.model.po.AgentEdgeExecuteLog;
import com.nova.agent.model.po.AgentExecuteLog;
import com.nova.agent.model.po.AgentNodeExecuteLog;
import com.nova.agent.model.vo.*;
import com.nova.agent.service.AgentFlowService;
import com.nova.agent.utils.CurrentUserContext;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@Slf4j
@RequestMapping("/api/v1/agent")
public class AgentFlowController {

    @Autowired
    private AgentFlowService agentFlowService;

    /**
     * Execute an agent workflow (talk)
     */
    @PostMapping("/talk")
    public Result<AgentFlowOutput> talk(@RequestBody UserInvokeInput userInvokeInput) {
        String appId = userInvokeInput.getApp_id();
        String conversationId = userInvokeInput.getConversation_id();
        long startTime = System.currentTimeMillis();

        Agent agent;
        if (userInvokeInput.getDebug() != null && userInvokeInput.getDebug() == 1) {
            agent = agentFlowService.queryDebugAgentById(appId);
        } else {
            agent = agentFlowService.queryAgentById(appId);
        }
        if (agent == null) {
            return Result.error("App ID not found!");
        }

        if (StringUtils.isBlank(userInvokeInput.getRequestId())) {
            userInvokeInput.setRequestId(UUID.randomUUID().toString());
        }

        String config = agent.getConfig();
        AgentFlow agentFlow = new AgentFlow(config);
        Integer turns = agent.getReferenceTurns();

        List<AgentExecuteLog> history = agentFlowService.queryConversationHistory(appId, conversationId, turns);
        userInvokeInput.setRealHistory(history);

        Long historyId = agentFlowService.newConversationHistory(appId, conversationId, userInvokeInput);
        userInvokeInput.setExecuteId(historyId);

        ConcurrentHashMap<String, MemoryVar> memoryMap = agentFlowService.getMemoryVar(appId, agent.getMemorySchema());
        agentFlow.setContextVar(com.nova.agent.constant.AgentFlowContextVar.MEMORY_VAR, memoryMap);

        agentFlow.setContextVar(com.nova.agent.constant.AgentFlowContextVar.CURRENT_ACCOUNT,
                CurrentUserContext.getUser());

        AgentFlowOutput output = new AgentFlowOutput();
        output.setTotalUsedTokens(0);
        output.setTotalCostTime(0L);

        try {
            output = agentFlowService.executeAgentFlow(agentFlow, userInvokeInput);
            output.setAnswer(output.getAnswer().replaceAll("\n", "\n\n"));
            output.setRequestId(userInvokeInput.getRequestId());
            output.setTotalCostTime(System.currentTimeMillis() - startTime);
            Integer usedTokens = agentFlowService.countUsedTokens(appId, conversationId, historyId);
            output.setTotalUsedTokens(usedTokens);
            return Result.success(output);
        } catch (Exception e) {
            log.error("Agent execution error!", e);
            return Result.error(e.getMessage());
        } finally {
            agentFlowService.updateConversationHistory(historyId, output);
        }
    }

    /**
     * Generate a new conversation ID
     */
    @GetMapping("/conversation")
    public Result<String> conversation(@RequestParam String appId) {
        String conversationId = UUID.randomUUID().toString().toLowerCase();
        agentFlowService.generateConversation(appId, conversationId);
        return Result.success(conversationId);
    }

    /**
     * Check if agent is published
     */
    @GetMapping("/checkPublishAgent")
    public Result<Boolean> checkPublishAgent(@RequestParam String appId) {
        return Result.success(agentFlowService.checkPublishAgent(appId));
    }



    /**
     * Get running status for real-time frontend display
     */
    @PostMapping("/runningInfo")
    public Result<Object> runningInfo(@Valid @RequestBody RunningInfoRequest request) {
        AgentExecuteLog executeLog = agentFlowService.getLastConversationHistory(
                request.getApp_id(), request.getConversation_id(), request.getTimestamp());
        if (executeLog == null) {
            return Result.success(ErrorCode.NO_RUNNING_LOG);
        }

        List<AgentNodeExecuteLog> nodeLogs = agentFlowService.getAgentNodeLog(
                executeLog.getAppId(), executeLog.getConversationId(), executeLog.getId());
        List<AgentEdgeExecuteLog> edgeLogs = agentFlowService.getAgentEdgeLog(
                executeLog.getAppId(), executeLog.getConversationId(), executeLog.getId());

        RunningInfoResponse response = new RunningInfoResponse();
        response.setStatus(executeLog.getStatus());
        boolean finished = AgentRunningStatus.FINISH.getStatus() == executeLog.getStatus()
                || AgentRunningStatus.EXCEPTION.getStatus() == executeLog.getStatus();
        response.setFinish(finished ? 1 : 0);
        response.setNodes(nodeLogs);
        response.setEdges(edgeLogs);

        return Result.success(response);
    }

    /**
     * Validate agent config
     */
    @PostMapping("/validate")
    public Result<Integer> validate(@RequestBody AgentEditRequest editRequest) {
        return agentFlowService.validate(editRequest.getConfig(), editRequest.getAppId());
    }

    /**
     * Save agent config (create or update)
     */
    @PostMapping("/save")
    public Result<String> save(@RequestBody AgentEditRequest editRequest) {
        if (StringUtils.isNotEmpty(editRequest.getAppId())) {
            return agentFlowService.updateAgent(editRequest);
        } else {
            return agentFlowService.insertAgent(editRequest);
        }
    }

    /**
     * Publish agent
     */
    @GetMapping("/publish")
    public Result<Integer> publish(@RequestParam String appId) {
        return agentFlowService.publish(appId);
    }

    /**
     * Get latest published time
     */
    @GetMapping("/latestPublishedTime")
    public Result<Long> latestPublishedTime(@RequestParam String appId) {
        return agentFlowService.latestPublishedTime(appId);
    }

    /**
     * List agents with pagination
     */
    @GetMapping("/list")
    public Result<PageInfo<AgentVo>> list(@RequestParam(required = false) String query,
                                          @RequestParam(required = false, defaultValue = "0") Integer isPublished,
                                          @RequestParam(required = false, defaultValue = "0") Integer agentType,
                                          @RequestParam Integer page,
                                          @RequestParam Integer pageSize) {
        return Result.success(agentFlowService.listAgent(query, isPublished, agentType, page, pageSize));
    }

    /**
     * Get agent detail
     */
    @GetMapping("/detail")
    public Result<AgentVo> detail(@RequestParam String appId) {
        AgentVo agentVo = agentFlowService.getAgent(appId);
        if (agentVo == null) {
            return Result.error("App ID not found!");
        }
        return Result.success(agentVo);
    }

    /**
     * Get canvas initialization info
     */
    @GetMapping("/initInfo")
    public Result<CanvasInitInfoVO> initInfo() {
        return Result.success(agentFlowService.initInfo());
    }

    /**
     * Copy an agent
     */
    @GetMapping("/copy")
    public Result<String> copy(@RequestParam @NotBlank(message = "appId is required!") String appId) {
        return Result.success(agentFlowService.copy(appId));
    }

    /**
     * Import an agent from file
     */
    @PostMapping("/import")
    public Result<String> importAgent(@RequestParam("file") MultipartFile file) {
        return Result.success(agentFlowService.importAgent(file));
    }

    /**
     * Delete an agent
     */
    @GetMapping("/delete")
    public Result<Void> delete(@RequestParam @NotBlank(message = "appId is required!") String appId) {
        agentFlowService.delete(appId);
        return Result.success();
    }

    /**
     * Debug API node
     */
    @PostMapping("/apiDebug")
    public Object apiDebug(@RequestBody ApiDebugReq apiDebugReq) {
        JSONObject json = agentFlowService.apiDebug(apiDebugReq);
        return JSON.toJSONString(Result.success(json));
    }

    /**
     * List supported LLM models
     */
    @GetMapping("/listSupportModels")
    public Result<List<SupportLlmVO>> listSupportModels() {
        return Result.success(agentFlowService.listSupportModels());
    }

    /**
     * Debug code node
     */
    @PostMapping("/codeDebug")
    public Result<Object> codeDebug(@RequestBody String req) {
        return agentFlowService.codeDebug(req);
    }

    /**
     * Verify workflow agent dependency
     */
    @GetMapping("/verify")
    public Result<Boolean> verify(@RequestParam(required = false, name = "parentId") String parentId,
                                   @RequestParam(required = true, name = "childId") String childId) {
        return Result.success(agentFlowService.verifyWorkflowAgent(parentId, childId));
    }

    /**
     * Export agent config
     */
    @GetMapping("/export")
    public void export(@RequestParam @NotBlank(message = "appId is required!") String appId,
                        HttpServletResponse response) {
        agentFlowService.export(appId, response);
    }

    /**
     * Convert prompt format between JSON and Markdown
     */
    @PostMapping("/prepareJson")
    public Result<String> prepareJson(@Valid @RequestBody PrepareJsonReq req) {
        return Result.success(agentFlowService.convertPromptFormat(
                req.getPromptMD(), req.getPromptJson(), req.getTransfer()));
    }
}
