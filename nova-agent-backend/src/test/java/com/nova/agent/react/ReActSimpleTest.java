package com.nova.agent.react;

import com.nova.agent.entity.AgentFlowOutput;
import com.nova.agent.entity.UserInvokeInput;
import com.nova.agent.model.vo.Result;
import com.nova.agent.react.controller.ReActController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ReAct 简单测试 — 假设 DB 中已有 tool_definition 和 agent（agent_type=2）。
 *
 * 改 APP_ID 后直接跑：
 *   mvn test -Dtest=com.nova.agent.react.ReActSimpleTest -pl .
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("dev")
class ReActSimpleTest {

    @Autowired
    private ReActController controller;

    private static final String APP_ID = "your-app-id-here";


    @Test
    void initData(){

    }
    @Test
    @DisplayName("talk")
    void talk() {
        UserInvokeInput in = new UserInvokeInput();
        in.setApp_id(APP_ID);
        in.setConversation_id("conv-" + UUID.randomUUID().toString().substring(0, 8));
        in.setQuery("你好");
        in.setRequestId(UUID.randomUUID().toString());
        in.setInputs(Map.of("query", "你好"));

        Result<AgentFlowOutput> r = controller.talk(in);

        assertNotNull(r);
        assertEquals(0, r.getCode(), r.getMessage());
        assertNotNull(r.getData().getAnswer());

        System.out.println("answer: " + r.getData().getAnswer());
        System.out.println("tokens: " + r.getData().getTotalUsedTokens());
        System.out.println("cost: " + r.getData().getTotalCostTime() + "ms");
    }
}
