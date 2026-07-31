package com.nova.agent.react;

import com.nova.agent.react.controller.ReActController;
import com.nova.agent.entity.AgentFlowOutput;
import com.nova.agent.entity.UserInvokeInput;
import com.nova.agent.model.po.Agent;
import com.nova.agent.model.po.AgentDraft;
import com.nova.agent.model.vo.Result;
import com.nova.agent.react.repository.ToolDefinitionMapper;
import com.nova.agent.react.tool.ToolDefinition;
import com.nova.agent.repository.AgentDraftMapper;
import com.nova.agent.repository.AgentMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("dev")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AutoAgentSeedTest {

    @Autowired private ReActController controller;
    @Autowired private AgentMapper agentMapper;
    @Autowired private AgentDraftMapper draftMapper;
    @Autowired private ToolDefinitionMapper toolMapper;

    private String appId;
    private String convId;

    @BeforeAll
    void seed() {
        convId = "conv-" + UUID.randomUUID().toString().substring(0, 8);

        // Tool 用 LLMNode，真实调 DeepSeek
        String props = "{\"nodeName\":\"订单摘要生成器\",\"model\":\"deepseek-v4-pro\",\"modelServer\":\"https://api.deepseek.com\",\"modelType\":\"LLM\",\"temperature\":0.3,\"maxOutputTokens\":256,\"systemPrompt\":\"你是一个订单摘要生成器，根据订单号生成简短中文摘要（50字以内），总是说订单在配送中。\",\"userPrompt\":\"请根据订单号 {{orderId}} 生成一段简短中文摘要。\",\"talkHistory\":0,\"inputVars\":[{\"varName\":\"orderId\",\"varType\":\"String\",\"originalVarType\":\"String\",\"varValue\":null}],\"outputVars\":[{\"varName\":\"orderSummary\",\"varType\":\"String\"}]}";
        String nodeJson = "{\"id\":\"tool_extract_summary_n\",\"type\":\"LLM\",\"properties\":\"" + esc(props) + "\"}";

        ToolDefinition t = new ToolDefinition();
        t.setToolId("tool_extract_summary"); t.setName("提取订单摘要"); t.setDescription("根据订单号生成订单状态的中文摘要");
        t.setExecutorType("node"); t.setExecutorResourceId(nodeJson);
        t.setInputSchema("[{\"name\":\"orderId\",\"type\":\"string\",\"required\":true,\"description\":\"订单号\"}]");
        t.setOutputSchema("[{\"name\":\"orderSummary\",\"type\":\"string\"}]");
        t.setInputMapping("[{\"source\":\"inputs.orderId\",\"target\":\"node.params.orderId\"}]");
        t.setOutputMapping("[{\"source\":\"result.orderSummary\",\"target\":\"outputs.orderSummary\"}]");
        t.setMemoryMapping("[{\"source\":\"outputs.orderSummary\",\"target\":\"memory.orderSummary\"}]");
        t.setStatus(1); t.setCreator("seed");
        toolMapper.insert(t);

        String cfg = "{\"systemPrompt\":\"你是电商客服助手，需要查询订单时调用 tool_extract_summary 工具获取摘要。\","
                + "\"maxLoop\":5,\"tools\":[\"tool_extract_summary\"],"
                + "\"plannerConfig\":{\"model\":\"deepseek-v4-pro\",\"modelServer\":\"https://api.deepseek.com\",\"temperature\":0.3,\"maxTokens\":4096}}";
        appId = seedAgent(cfg);
    }

    @Test
    @Order(1)
    @DisplayName("全链路真实执行：Planner(LLM) → Tool(LLMNode真实调用) → 回答")
    void fullRealExecution() {
        UserInvokeInput in = new UserInvokeInput();
        in.setApp_id(appId); in.setConversation_id(convId);
        in.setQuery("我想知道订单888的摘要信息");
        in.setRequestId(UUID.randomUUID().toString());
        in.setInputs(Map.of("query", "我想知道订单888的摘要信息"));

        Result<AgentFlowOutput> r = controller.talk(in);
        assertEquals(0, r.getCode(), r.getMessage());

        String answer = r.getData().getAnswer();
        assertNotNull(answer); assertFalse(answer.isEmpty());

        System.out.println("=== Tool 真实执行 (LLMNode via DeepSeek) ===");
        System.out.println("answer: " + answer);
        System.out.println("tokens: " + r.getData().getTotalUsedTokens());
        System.out.println("cost: " + r.getData().getTotalCostTime() + "ms");
    }

    @Test
    @Order(2)
    @DisplayName("第二轮：验证 memory 中保留了 Tool 输出")
    void memoryPersisted() {
        UserInvokeInput in = new UserInvokeInput();
        in.setApp_id(appId); in.setConversation_id(convId);
        in.setQuery("刚才已经查过订单了，直接告诉我摘要就行");
        in.setRequestId(UUID.randomUUID().toString());
        in.setInputs(Map.of("query", "刚才已经查过订单了，直接告诉我摘要就行"));

        Result<AgentFlowOutput> r = controller.talk(in);
        assertEquals(0, r.getCode(), r.getMessage());

        System.out.println("=== 第二轮 (验证 memory) ===");
        System.out.println("answer: " + r.getData().getAnswer());
    }

    @AfterAll
    void clean() {
        agentMapper.deleteByAppId(appId);
        draftMapper.deleteByAppId(appId);
        toolMapper.deleteByToolId("tool_extract_summary");
    }

    String seedAgent(String cfg) {
        String id = "react-" + UUID.randomUUID().toString().substring(0, 6);
        Agent a = new Agent();
        a.setAppId(id); a.setName("智能客服(ReAct)"); a.setAgentType(2); a.setStatus(1);
        a.setConfig(cfg);
        a.setMemorySchema("[{\"name\":\"orderId\",\"type\":\"string\",\"description\":\"订单号\"}]");
        a.setReferenceTurns(5); a.setCreator("seed");
        agentMapper.insert(a);
        AgentDraft d = new AgentDraft();
        d.setAppId(id); d.setName(a.getName()); d.setAgentType(2); d.setStatus(1);
        d.setConfig(cfg); d.setMemorySchema(a.getMemorySchema()); d.setReferenceTurns(5); d.setCreator("seed");
        draftMapper.insert(d);
        return id;
    }

    static String esc(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
