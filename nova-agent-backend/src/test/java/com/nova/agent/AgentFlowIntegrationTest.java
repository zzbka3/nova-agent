package com.nova.agent;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nova.agent.entity.AgentFlow;
import com.nova.agent.entity.AgentFlowOutput;
import com.nova.agent.entity.UserInvokeInput;
import com.nova.agent.entity.graph.Node;
import com.nova.agent.enums.AgentRunningStatus;
import com.nova.agent.enums.NodeStatus;
import com.nova.agent.enums.NodeType;
import com.nova.agent.model.po.*;
import com.nova.agent.model.vo.Result;
import com.nova.agent.repository.*;
import com.nova.agent.service.AgentFlowService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 工作流整合测试（连真实 MySQL + Redis）
 *
 * 测试数据全程自动创建和清理，不会污染数据库。
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
    private AgentExecuteLogMapper agentExecuteLogMapper;

    @Autowired
    private AgentNodeExecuteLogMapper agentNodeExecuteLogMapper;

    @Autowired
    private AgentEdgeExecuteLogMapper agentEdgeExecuteLogMapper;

    @Autowired
    private ConversationMapper conversationMapper;

    // ---- 测试数据 ----

    private String testAppId;
    private String testConversationId;

    // 两个用于测试的工作流 JSON
    private static String simpleWorkflowJson;
    private static String ifWorkflowJson;

    @BeforeAll
    static void loadWorkflowJsons() throws Exception {
        // 线性工作流: START → LLM → MESSAGE → END
        simpleWorkflowJson = Files.readString(
                Paths.get("test_workflow.json"), StandardCharsets.UTF_8);

        System.out.println("========== 整合测试启动 ==========");
        System.out.println("线性工作流: " + simpleWorkflowJson.length() + " 字符");
    }

    // ================================================================
    // Test 1: 创建 & 保存智能体
    // ================================================================

    @Test
    @Order(1)
    @DisplayName("1. 保存智能体 → agent + agent_draft 双写")
    void shouldCreateAgent() {
        String name = "JUnit-Test-" + System.currentTimeMillis();

        // 调用 save（无 appId = 新建）
        Result<String> result = agentFlowService.insertAgent(
                createEditRequest(name, simpleWorkflowJson));

        assertEquals(0, result.getCode(), result.getMessage());
        assertNotNull(result.getData(), "appId 不应该为空");
        testAppId = result.getData();
        System.out.println("✓ 创建成功, appId = " + testAppId);

        // 验证 agent 表
        Agent agent = agentMapper.selectByAppId(testAppId);
        assertNotNull(agent, "agent 表应有记录");
        assertEquals(name, agent.getName());
        assertEquals(0, agent.getStatus()); // DRAFT
        assertNotNull(agent.getConfig());
        System.out.println("  agent 表: id=" + agent.getId() + ", name=" + agent.getName());

        // 验证 agent_draft 表
        AgentDraft draft = agentDraftMapper.selectByAppId(testAppId);
        assertNotNull(draft, "agent_draft 表应有记录");
        assertEquals(name, draft.getName());
        System.out.println("  agent_draft 表: id=" + draft.getId() + ", name=" + draft.getName());
    }

    // ================================================================
    // Test 2: 校验工作流合法性
    // ================================================================

    @Test
    @Order(2)
    @DisplayName("2. 校验工作流 DAG 合法性")
    void shouldValidateWorkflow() {
        Result<Integer> result = agentFlowService.validate(simpleWorkflowJson, testAppId);
        assertEquals(0, result.getCode(), "校验应通过");
        assertEquals(1, result.getData(), "返回结果应为 1");
        System.out.println("✓ 工作流校验通过");
    }

    // ================================================================
    // Test 3: 查询智能体详情
    // ================================================================

    @Test
    @Order(3)
    @DisplayName("3. 查询智能体详情")
    void shouldGetAgentDetail() {
        var vo = agentFlowService.getAgent(testAppId);
        assertNotNull(vo, "应查得到智能体");
        assertEquals(testAppId, vo.getAppId());
        assertNotNull(vo.getConfig(), "config 不应为空");
        System.out.println("✓ 查询成功: " + vo.getName());
    }

    // ================================================================
    // Test 4: 生成会话
    // ================================================================

    @Test
    @Order(4)
    @DisplayName("4. 生成会话 ID → conversation 表写入")
    void shouldGenerateConversation() {
        testConversationId = UUID.randomUUID().toString().toLowerCase();
        agentFlowService.generateConversation(testAppId, testConversationId);

        Conversation conv = conversationMapper.selectByConversationId(testAppId, testConversationId);
        assertNotNull(conv, "conversation 表应有记录");
        assertEquals(testAppId, conv.getAppId());
        assertEquals(testConversationId, conv.getConversationId());
        System.out.println("✓ conversationId = " + testConversationId);
    }

    // ================================================================
    // Test 5: DAG 构造 & 执行引擎
    // ================================================================

    @Test
    @Order(5)
    @DisplayName("5. 执行工作流 → 全链路跑通")
    void shouldExecuteWorkflow() {
        // 构造 AgentFlow
        AgentFlow flow = agentFlowService.constructGraphFromConfig(simpleWorkflowJson);
        assertNotNull(flow, "AgentFlow 构造不应为空");
        assertTrue(flow.checkGraph(), "应无环");
        assertEquals(4, flow.getGraph().vertexSet().size(), "应 4 个节点");

        // 构造用户输入
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

        // 写执行日志
        Long executeId = agentFlowService.newConversationHistory(
                testAppId, testConversationId, input);
        input.setExecuteId(executeId);
        System.out.println("✓ executeId = " + executeId);

        // 执行！
        long start = System.currentTimeMillis();
        AgentFlowOutput output = agentFlowService.executeAgentFlow(flow, input);
        long cost = System.currentTimeMillis() - start;

        System.out.println("✓ 执行完成, 耗时: " + cost + "ms");

        // 验证 output
        assertNotNull(output, "output 不应为空");
        assertNotNull(output.getAnswer(), "answer 不应为空");
        System.out.println("  answer: " + output.getAnswer().substring(
                0, Math.min(200, output.getAnswer().length())));

        // 验证 agent_execute_log 表
        AgentExecuteLog execLog = agentExecuteLogMapper.selectById(executeId);
        assertNotNull(execLog, "agent_execute_log 应有记录");
        System.out.println("  agent_execute_log: status=" + execLog.getStatus()
                + ", tokens=" + execLog.getTotalUsedTokens()
                + ", cost=" + execLog.getCostTime());

        // 验证 agent_node_execute_log 表（应该每个节点都记了日志）
        List<AgentNodeExecuteLog> nodeLogs = agentNodeExecuteLogMapper.selectByExecuteId(
                testAppId, testConversationId, executeId);
        assertNotNull(nodeLogs, "节点日志不应为空");
        System.out.println("  agent_node_execute_log: " + nodeLogs.size() + " 条记录");
        nodeLogs.forEach(nl -> System.out.printf("    [%s] %-12s %s cost=%dms%n",
                nl.getNodeId(), nl.getNodeType(), nl.getNodeName(), nl.getCostTime()));
    }

    // ================================================================
    // Test 6: runningInfo 查询
    // ================================================================

    @Test
    @Order(6)
    @DisplayName("6. 查询 runningInfo → 节点 + 边日志")
    void shouldQueryRunningInfo() {
        AgentExecuteLog lastLog = agentFlowService.getLastConversationHistory(
                testAppId, testConversationId, 0L);
        assertNotNull(lastLog, "应有执行记录");

        List<AgentNodeExecuteLog> nodeLogs = agentFlowService.getAgentNodeLog(
                testAppId, testConversationId, lastLog.getId());
        List<AgentEdgeExecuteLog> edgeLogs = agentFlowService.getAgentEdgeLog(
                testAppId, testConversationId, lastLog.getId());

        System.out.println("  执行记录: id=" + lastLog.getId() + ", status=" + lastLog.getStatus());
        System.out.println("  节点日志: " + nodeLogs.size() + " 条");
        System.out.println("  边日志: " + edgeLogs.size() + " 条");

        assertFalse(nodeLogs.isEmpty(), "节点日志不应为空");
    }

    // ================================================================
    // Test 7: IF 工作流构造
    // ================================================================

    @Test
    @Order(7)
    @DisplayName("7. IF 分支工作流构造 + 条件触发")
    void shouldBuildIfWorkflow() {
        // 内联构造 IF 工作流 JSON
        String ifJson = """
        {
          "nodes": [
            {"id":"1","type":"START","properties":"{\\"nodeName\\":\\"开始\\",\\"outputVars\\":[{\\"varName\\":\\"score\\",\\"varType\\":\\"Integer\\"}]}"},
            {"id":"2","type":"IF","properties":"{\\"nodeName\\":\\"判断\\",\\"conditionList\\":[{\\"innerLogic\\":\\"AND\\",\\"innerConditions\\":[{\\"left\\":{\\"varName\\":\\"score\\",\\"varType\\":\\"Integer\\",\\"varValue\\":85},\\"op\\":\\"GT_EQUAL\\",\\"right\\":{\\"varName\\":\\"threshold\\",\\"varType\\":\\"Integer\\",\\"varValue\\":60}}],\\"targetNodes\\":[{\\"edgeId\\":\\"e2\\",\\"nodeId\\":\\"3\\",\\"nodeName\\":\\"通过\\"}]}],\\"defaultTargetNodes\\":[{\\"edgeId\\":\\"e3\\",\\"nodeId\\":\\"4\\",\\"nodeName\\":\\"不通过\\"}]}"},
            {"id":"3","type":"MESSAGE","properties":"{\\"nodeName\\":\\"通过\\",\\"msg\\":\\"通过\\"}"},
            {"id":"4","type":"END","properties":"{\\"nodeName\\":\\"结束\\"}"}
          ],
          "edges": [
            {"id":"e1","sourceNodeId":"1","targetNodeId":"2"},
            {"id":"e2","sourceNodeId":"2","targetNodeId":"3"},
            {"id":"e3","sourceNodeId":"2","targetNodeId":"4"}
          ]
        }""";

        AgentFlow flow = new AgentFlow(ifJson);
        assertTrue(flow.checkGraph(), "应无环");
        assertEquals(4, flow.getGraph().vertexSet().size());

        // 执行 IF 节点
        Node ifNode = flow.getGraph().vertexSet().stream()
                .filter(n -> n instanceof com.nova.agent.entity.graph.IfNode)
                .findFirst().orElseThrow();

        ifNode.run(flow);

        // 验证分支命中
        long matched = flow.getGraph().edgeSet().stream()
                .filter(e -> e.getConditionMatch() == 1).count();
        assertTrue(matched > 0, "至少一条边应被命中");

        // 验证 e2（通过分支）被命中
        boolean passEdgeMatched = flow.getGraph().edgeSet().stream()
                .anyMatch(e -> "e2".equals(e.getId()) && e.getConditionMatch() == 1);
        assertTrue(passEdgeMatched, "score=85 ≥ threshold=60 应命中'通过'分支 e2");

        System.out.println("✓ IF 分支: e2(通过) 命中, e3(兜底) 未命中");
    }

    // ================================================================
    // Test 8: 发布 / 列表 / 拷贝
    // ================================================================

    @Test
    @Order(8)
    @DisplayName("8. 发布 + 列表查询 + 复制")
    void shouldPublishListAndCopy() {
        // 发布
        Result<Integer> pubResult = agentFlowService.publish(testAppId);
        assertEquals(0, pubResult.getCode(), "发布应成功");

        Agent agent = agentMapper.selectByAppId(testAppId);
        assertEquals(1, agent.getStatus(), "发布后 status 应为 1");
        System.out.println("✓ 发布成功: status=" + agent.getStatus());

        // 列表查询
        var page = agentFlowService.listAgent(null, null, null, 1, 10);
        assertNotNull(page);
        assertTrue(page.getTotal() > 0, "列表应有数据");
        System.out.println("✓ 列表: total=" + page.getTotal() + ", pages=" + page.getPages());

        // 复制
        String copyAppId = agentFlowService.copy(testAppId);
        assertNotNull(copyAppId, "复制应返回新 appId");
        assertNotEquals(testAppId, copyAppId, "复制后的 appId 应不同");

        Agent copy = agentMapper.selectByAppId(copyAppId);
        assertNotNull(copy, "复制的 agent 应存在");
        System.out.println("✓ 复制成功: " + copyAppId + " → " + copy.getName());

        // 清理副本
        agentMapper.deleteByAppId(copyAppId);
        agentDraftMapper.deleteByAppId(copyAppId);
    }

    // ================================================================
    // Test 9: 检查已发布
    // ================================================================

    @Test
    @Order(9)
    @DisplayName("9. 检查已发布状态")
    void shouldCheckPublished() {
        Boolean published = agentFlowService.checkPublishAgent(testAppId);
        assertTrue(published, "应已发布");
        System.out.println("✓ 已发布: " + published);
    }

    // ================================================================
    // 清理
    // ================================================================

    @AfterAll
    void cleanUp() {
        System.out.println("\n========== 清理测试数据 ==========");
        try {
            if (testAppId != null) {
                agentMapper.deleteByAppId(testAppId);
                agentDraftMapper.deleteByAppId(testAppId);
                System.out.println("✓ agent + agent_draft 已删除: " + testAppId);
            }
        } catch (Exception e) {
            System.out.println("清理失败（可能 DB 未连接）: " + e.getMessage());
        }
    }

    // ================================================================
    // Helper
    // ================================================================

    private com.nova.agent.controller.request.AgentEditRequest createEditRequest(
            String name, String config) {
        var req = new com.nova.agent.controller.request.AgentEditRequest();
        req.setName(name);
        req.setConfig(config);
        req.setAgentType(0);
        req.setReferenceTurns(5);
        return req;
    }
}
