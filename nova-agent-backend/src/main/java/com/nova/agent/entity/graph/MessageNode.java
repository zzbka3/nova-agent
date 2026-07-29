package com.nova.agent.entity.graph;

import com.alibaba.fastjson.JSON;
import com.nova.agent.constant.AgentFlowContextVar;
import com.nova.agent.entity.AgentFlow;
import com.nova.agent.entity.InputVar;
import com.nova.agent.entity.OutPutVar;
import com.nova.agent.enums.NodeType;
import com.nova.agent.enums.VarType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 消息输出节点。
 *
 * <p>将输入变量和一段消息模板组合后输出。主要用途：
 * <ul>
 *   <li>在 LLM 响应前/后插入固定文案</li>
 *   <li>将上游节点的输出格式化为特定格式的文本</li>
 *   <li>在流式场景下控制消息的分段输出</li>
 * </ul>
 *
 * <p>支持 {@code {{varName}}} 模板变量替换。
 */
@Slf4j
public class MessageNode extends Node {

    public MessageNode(String nodeId, String name, NodeType nodeType,
                       List<InputVar> inputVars, List<OutPutVar> outPutVars, String config) {
        super(nodeId, name, nodeType, inputVars, outPutVars, config);
    }

    @Override
    public void run(AgentFlow agentFlow) {
        log.debug("message node run, nodeId: {}", this.nodeId);
        MessageConfig msgConfig = JSON.parseObject(config, MessageConfig.class);
        String msg = replaceVar(msgConfig.msg);
        agentFlow.setContextVar(AgentFlowContextVar.NODE_RESULT_PREFIX + this.nodeId, msg);
    }

    @Override
    public void fillOutputVar(AgentFlow agentFlow) {
        String result = (String) agentFlow.getContextVar(
                AgentFlowContextVar.NODE_RESULT_PREFIX + this.nodeId);
        // Propagate input vars and message to outputs
        if (inputVars != null) {
            for (InputVar inputVar : inputVars) {
                OutPutVar out = new OutPutVar();
                out.setVarName(inputVar.getVarName());
                out.setVarType(inputVar.getVarType());
                out.setVarValue(inputVar.getVarValue());
                if (outputVars != null) {
                    outputVars.add(out);
                }
            }
        }
        OutPutVar msgOut = new OutPutVar();
        msgOut.setVarName("msg");
        msgOut.setVarType(VarType.String);
        msgOut.setVarValue(result);
        if (outputVars != null) {
            outputVars.add(msgOut);
        }
    }

    @Data
    public static class MessageConfig {
        private Boolean isStream;
        private String msg;
    }
}
