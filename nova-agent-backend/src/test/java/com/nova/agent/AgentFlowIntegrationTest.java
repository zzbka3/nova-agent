package com.nova.agent;

import com.nova.agent.entity.AgentFlow;
import com.nova.agent.entity.AgentFlowOutput;
import com.nova.agent.entity.UserInvokeInput;
import com.nova.agent.model.po.*;
import com.nova.agent.repository.*;
import com.nova.agent.service.AgentFlowService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 工作流执行测试 - 只测最核心的：保存 → 执行 → 查日志
 */
@SpringBootTest
@ActiveProfiles("dev")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AgentFlowIntegrationTest {

    @Autowired
    private AgentFlowService agentFlowService;

    @Autowired
    private AgentMapper agentMapper;

    @Autowired
    private AgentDraftMapper agentDraftMapper;

    @Autowired
    private AgentNodeExecuteLogMapper agentNodeExecuteLogMapper;

    private String testAppId;
    private String testConversationId;

    @BeforeAll
    void setUp() throws Exception {
        testConversationId = UUID.randomUUID().toString().toLowerCase();
        agentFlowService.generateConversation("test-fixed-app", testConversationId);
        System.out.println("conversationId = " + testConversationId);
    }

    @Test
    @DisplayName("保存并执行工作流 START→LLM→MESSAGE→END")
    void shouldExecuteWorkflow() throws Exception {
        // 1. 加载工作流 JSON
        String json = Files.readString(Paths.get("test_workflow.json"), StandardCharsets.UTF_8);

        // 2. 保存
        var req = new com.nova.agent.controller.request.AgentEditRequest();
        req.setName("SimpleFlowTest");
        req.setConfig(json);
        req.setAgentType(0);
        req.setReferenceTurns(5);
        var saveResult = agentFlowService.insertAgent(req);
        testAppId = saveResult.getData();
        System.out.println("appId = " + testAppId);

        // 3. 构造 AgentFlow
        AgentFlow flow = agentFlowService.constructGraphFromConfig(json);
        assertTrue(flow.checkGraph(), "DAG 应无环");

        // 4. 构造用户输入
        UserInvokeInput input = new UserInvokeInput();
        input.setApp_id(testAppId);
        input.setConversation_id(testConversationId);
        input.setQuery("你好");
        input.setRequestId(UUID.randomUUID().toString());
        input.setDebug(0);
        input.setRealHistory(new ArrayList<>());
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("query", "你好");
        input.setInputs(inputs);

        Long executeId = agentFlowService.newConversationHistory(testAppId, testConversationId, input);
        input.setExecuteId(executeId);

        // 5. 执行！
        AgentFlowOutput output = agentFlowService.executeAgentFlow(flow, input);

        // 6. 验证结果
        assertNotNull(output, "output 不应为空");
        assertNotNull(output.getAnswer(), "answer 不应为空");
        System.out.println("answer: " + output.getAnswer().substring(0, Math.min(200, output.getAnswer().length())));
        System.out.println("totalUsedTokens: " + output.getTotalUsedTokens());
        System.out.println("totalCostTime: " + output.getTotalCostTime() + "ms");

        // 7. 验证日志
        List<AgentNodeExecuteLog> nodeLogs = agentNodeExecuteLogMapper.selectByExecuteId(
                testAppId, testConversationId, executeId);
        assertNotNull(nodeLogs);
        assertFalse(nodeLogs.isEmpty());
        System.out.println("\n节点执行日志 (" + nodeLogs.size() + " 条):");
        nodeLogs.forEach(nl -> System.out.printf("  [%s] %-12s %-10s cost=%dms tokens=%d%n",
                nl.getNodeId(), nl.getNodeType(), nl.getNodeName(), nl.getCostTime(), nl.getUsedTokens()));
    }

    @AfterAll
    void cleanUp() {
        try {
            if (testAppId != null) {
                agentMapper.deleteByAppId(testAppId);
                agentDraftMapper.deleteByAppId(testAppId);
                System.out.println("\n测试数据已清理");
            }
        } catch (Exception ignored) {}
    }
}
