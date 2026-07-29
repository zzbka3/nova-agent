package com.nova.agent.entity.graph;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nova.agent.constant.AgentFlowContextVar;
import com.nova.agent.entity.AgentFlow;
import com.nova.agent.entity.InputVar;
import com.nova.agent.entity.OutPutVar;
import com.nova.agent.enums.NodeType;
import com.nova.agent.enums.VarType;
import com.nova.agent.service.PythonRunner;
import com.nova.agent.utils.SpringContextUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Python 代码沙箱执行节点。
 *
 * <p>将用户编写的 Python 代码和输入参数通过 HTTP 发送到独立的 Python 沙箱服务执行。
 * 沙箱服务返回统一协议：
 * <pre>{@code
 * {
 *   "ok": true|false,
 *   "result": {...},
 *   "error": "错误信息",
 *   "exec_time_ms": 50
 * }
 * }</pre>
 *
 * <p>输入变量自动序列化为 Python 函数参数。输出变量从 {@code result} 字段中按名称提取。
 *
 * <h4>安全说明</h4>
 * 代码在独立的沙箱进程中执行，与 Java 进程完全隔离。
 * 沙箱服务应配置资源限制（内存、CPU、超时）。
 */
@Slf4j
public class CodeNode extends Node {

    public CodeNode(String nodeId, String name, NodeType nodeType,
                    List<InputVar> inputVars, List<OutPutVar> outputVars, String config) {
        super(nodeId, name, nodeType, inputVars, outputVars, config);
    }

    @Override
    public void run(AgentFlow agentFlow) {
        PythonRunner pythonRunner = SpringContextUtils.getBean("pythonRunner", PythonRunner.class);
        CodeConfig codeConfig = JSON.parseObject(config, CodeConfig.class);
        String userCode = codeConfig != null && codeConfig.getCodeData() != null
                ? codeConfig.getCodeData() : "";

        Map<String, Object> paramsMap = new HashMap<>();
        if (inputVars != null) {
            for (InputVar var : inputVars) {
                String key = normalizeKey(var);
                if (key != null) {
                    paramsMap.put(key, var.getVarValue());
                }
            }
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", userCode);
        jsonObject.put("params", paramsMap);

        String result = pythonRunner.runCode(jsonObject.toJSONString());
        agentFlow.setContextVar(AgentFlowContextVar.NODE_RESULT_PREFIX + this.nodeId, result);
    }

    @Override
    public void fillOutputVar(AgentFlow agentFlow) {
        Object ctxObj = agentFlow.getContextVar(
                AgentFlowContextVar.NODE_RESULT_PREFIX + this.nodeId);
        if (ctxObj == null || outputVars == null) return;

        try {
            JSONObject root = JSON.parseObject(String.valueOf(ctxObj));
            if (root == null || !root.containsKey("ok")) {
                clearAllOutputs();
                return;
            }

            Boolean ok = root.getBoolean("ok");
            if (Boolean.TRUE.equals(ok)) {
                JSONObject payload = root.getJSONObject("result");
                if (payload != null && outputVars != null) {
                    for (OutPutVar outVar : outputVars) {
                        String name = outVar.getVarName();
                        if (!isProtocolField(name)) {
                            Object value = payload.get(name);
                            outVar.setVarValue(value);
                        }
                    }
                }
            } else {
                clearAllOutputs();
            }
        } catch (Exception e) {
            log.error("CodeNode fill output error!", e);
            clearAllOutputs();
        }
    }

    private void clearAllOutputs() {
        if (outputVars != null) {
            for (OutPutVar v : outputVars) {
                v.setVarValue(null);
            }
        }
    }

    private boolean isProtocolField(String name) {
        return name != null && ("ok".equals(name) || "result".equals(name)
                || "error".equals(name) || "exec_time_ms".equals(name));
    }

    private String normalizeKey(InputVar var) {
        String k = var.getVarName();
        if (k == null || k.trim().isEmpty() || "undefined".equalsIgnoreCase(k)) {
            if (var.getVarType() == VarType.reference && var.getReferenceVarName() != null) {
                return var.getReferenceVarName().trim();
            }
            return null;
        }
        return k.trim();
    }

    @Data
    public static class CodeConfig {
        private String codeData;
        private String baseUrl;
        private Integer timeoutMs;
    }
}
