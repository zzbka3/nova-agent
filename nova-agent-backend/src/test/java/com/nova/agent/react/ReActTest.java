package com.nova.agent.react;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nova.agent.entity.AgentFlowOutput;
import com.nova.agent.entity.UserInvokeInput;
import com.nova.agent.model.po.Agent;
import com.nova.agent.model.po.AgentDraft;
import com.nova.agent.model.vo.Result;
import com.nova.agent.react.controller.ReActController;
import com.nova.agent.react.repository.ToolDefinitionMapper;
import com.nova.agent.react.tool.ToolDefinition;
import com.nova.agent.repository.AgentDraftMapper;
import com.nova.agent.repository.AgentMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ReAct 集成测试：前端产出 JSON → 写入 DB → 后端执行 → 验证。
 *
 * JSON 文件: src/test/resources/react/
 *   tool_*.json       前端 Tool 画布编辑产出
 *   agent_*.json      前端 AutoAgent 画布编辑产出
 *
 * mvn test -Dtest=com.nova.agent.react.ReActTest -pl .
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("dev")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReActTest {

    @Autowired private ReActController controller;
    @Autowired private AgentMapper agentMapper;
    @Autowired private AgentDraftMapper draftMapper;
    @Autowired private ToolDefinitionMapper toolMapper;

    static final String DIR = "src/test/resources/react/";
    final List<String> createdAgents = new ArrayList<>();
    final List<String> createdTools = new ArrayList<>();

    // ---- 加载 JSON ----

    void loadTools(String... filenames) {
        for (String f : filenames) {
            JSONObject json = readJson(f);
            if (json.get("executorResourceId") instanceof JSONObject jo)
                json.put("executorResourceId", jo.toJSONString());
            ToolDefinition t = json.toJavaObject(ToolDefinition.class);
            if (t.getStatus() == null) t.setStatus(1);
            t.setCreator("fixture");
            toolMapper.insert(t);
            createdTools.add(t.getToolId());
        }
    }

    String loadAgent(String filename) {
        JSONObject json = readJson(filename);
        String appId = "react-" + UUID.randomUUID().toString().substring(0, 6);

        JSONObject cfg = new JSONObject();
        cfg.put("systemPrompt", json.getString("systemPrompt"));
        cfg.put("maxLoop", json.getOrDefault("maxLoop", 10));
        cfg.put("tools", json.get("tools"));
        cfg.put("workflows", json.getOrDefault("workflows", new ArrayList<>()));
        cfg.put("plannerConfig", json.get("plannerConfig"));

        Agent a = new Agent();
        a.setAppId(appId); a.setName(json.getString("name"));
        a.setAgentType(2); a.setStatus(1); a.setConfig(cfg.toJSONString());
        a.setMemorySchema(json.get("memorySchema") == null ? "[]"
                : json.getJSONArray("memorySchema").toJSONString());
        a.setReferenceTurns(5); a.setCreator("fixture");
        agentMapper.insert(a);

        AgentDraft d = new AgentDraft();
        d.setAppId(appId); d.setName(a.getName()); d.setAgentType(2); d.setStatus(1);
        d.setConfig(a.getConfig()); d.setMemorySchema(a.getMemorySchema());
        d.setReferenceTurns(5); d.setCreator("fixture");
        draftMapper.insert(d);

        createdAgents.add(appId);
        return appId;
    }

    JSONObject readJson(String filename) {
        try { return JSON.parseObject(Files.readString(Path.of(DIR + filename))); }
        catch (Exception e) { throw new RuntimeException("读取 fixture 失败: " + filename, e); }
    }

    // ---- 生命周期 ----

    @BeforeAll
    void seed() {
        loadTools("tool_query_order.json", "tool_query_logistics.json");
        loadAgent("agent_customer_service.json");
    }

    @AfterAll
    void clean() {
        createdAgents.forEach(agentMapper::deleteByAppId);
        createdAgents.forEach(draftMapper::deleteByAppId);
        createdTools.forEach(toolMapper::deleteByToolId);
    }

    // ---- 测试用例 ----

    @Test
    @DisplayName("查询订单")
    void queryOrder() {
        var r = talk("帮我查一下订单888的状态");
        assertOk(r);
        System.out.println("answer: " + r.getData().getAnswer());
    }

    @Test
    @DisplayName("查询物流")
    void queryLogistics() {
        var r = talk("订单888发的什么快递？");
        assertOk(r);
        System.out.println("answer: " + r.getData().getAnswer());
    }

    @Test
    @DisplayName("多轮对话：先查订单 → 追问")
    void multiTurn() {
        String cid = "conv-" + UUID.randomUUID().toString().substring(0, 8);
        var r1 = talk("订单123的摘要", cid);
        assertOk(r1);
        System.out.println("[1] " + r1.getData().getAnswer());

        var r2 = talk("刚才那个摘要再重复一遍", cid);
        assertOk(r2);
        System.out.println("[2] " + r2.getData().getAnswer());
    }

    @Test
    @DisplayName("完整流程：查订单 → 查物流 → 综合回答")
    void fullWorkflow() {
        String cid = "conv-" + UUID.randomUUID().toString().substring(0, 8);
        var r = talk("我想退货订单888，先帮我看看订单状态，再看看物流到哪了", cid);
        assertOk(r);
        System.out.println("answer: " + r.getData().getAnswer());
    }

    // ---- private ----

    Result<AgentFlowOutput> talk(String query) {
        return talk(query, "conv-" + UUID.randomUUID().toString().substring(0, 8));
    }

    Result<AgentFlowOutput> talk(String query, String convId) {
        UserInvokeInput in = new UserInvokeInput();
        in.setApp_id(createdAgents.get(0)); in.setConversation_id(convId);
        in.setQuery(query); in.setRequestId(UUID.randomUUID().toString());
        in.setInputs(Map.of("query", query));
        return controller.talk(in);
    }

    void assertOk(Result<?> r) {
        assertNotNull(r);
        assertEquals(0, r.getCode(), r.getMessage());
        assertNotNull(r.getData());
    }
}
