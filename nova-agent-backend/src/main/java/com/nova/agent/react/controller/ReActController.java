package com.nova.agent.react.controller;

import com.nova.agent.entity.AgentFlowOutput;
import com.nova.agent.entity.UserInvokeInput;
import com.nova.agent.model.po.Agent;
import com.nova.agent.model.vo.Result;
import com.nova.agent.react.runtime.ReActRuntime;
import com.nova.agent.react.tool.ToolRegistry;
import com.nova.agent.service.AgentFlowService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/agent/re-act")
public class ReActController {

    @Autowired private AgentFlowService agentFlowService;
    @Autowired private ReActRuntime reActRuntime;
    @Autowired private ToolRegistry toolRegistry;

    @PostMapping("/talk")
    public Result<AgentFlowOutput> talk(@RequestBody UserInvokeInput input) {
        String appId = input.getApp_id();
        String conversationId = input.getConversation_id();

        Agent agent = (input.getDebug() != null && input.getDebug() == 1)
                ? agentFlowService.queryDebugAgentById(appId)
                : agentFlowService.queryAgentById(appId);

        if (agent == null) return Result.error("App ID not found!");
        if (StringUtils.isBlank(input.getRequestId())) {
            input.setRequestId(UUID.randomUUID().toString());
        }

        Long historyId = agentFlowService.newConversationHistory(appId, conversationId, input);
        input.setExecuteId(historyId);

        AgentFlowOutput output = new AgentFlowOutput();
        output.setTotalUsedTokens(0); output.setTotalCostTime(0L);

        try {
            ReActRuntime.ReActResponse resp = reActRuntime.execute(agent, input);
            String answer = resp.getFinalAnswer() != null
                    ? resp.getFinalAnswer().replaceAll("\n", "\n\n") : "";
            output.setAnswer(answer);
            output.setRequestId(input.getRequestId());
            output.setTotalCostTime(resp.getTotalCostTime());
            output.setTotalUsedTokens(resp.getTotalUsedTokens());
            return Result.success(output);
        } catch (Exception e) {
            log.error("ReAct execution error", e);
            output.setAnswer("Error: " + e.getMessage());
            return Result.error(e.getMessage());
        } finally {
            agentFlowService.updateConversationHistory(historyId, output);
        }
    }

    @GetMapping("/tool/refresh")
    public Result<String> refreshTools() {
        toolRegistry.refresh();
        return Result.success("OK, " + toolRegistry.listAll().size() + " tools loaded");
    }
}
