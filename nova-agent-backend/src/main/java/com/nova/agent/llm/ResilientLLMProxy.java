package com.nova.agent.llm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component("resilientLlmProxy")
public class ResilientLLMProxy implements LLMClient {

    @org.springframework.beans.factory.annotation.Value("${llm.api.default-base:}")
    private String defaultBase;

    @org.springframework.beans.factory.annotation.Value("${llm.api.key:}")
    private String apiKey;

    private static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    @Override
    public String chat(String prompt, String model) {
        return chat(CallArgs.builder().userPrompt(prompt).model(model)
                .modelServer(defaultBase).build());
    }

    @Override
    public String chat(CallArgs args) {
        return doChat(args.getModelServer(), buildBody(args), args.getModel());
    }

    @Override
    public String chatWithFallback(CallArgs args, ModelFallback fallback) {
        try {
            return chat(args);
        } catch (Exception e) {
            if (fallback != null) {
                log.warn("Primary failed, trying fallback: {}", fallback.getFallbackModelName());
                return chat(CallArgs.builder()
                        .userPrompt(args.getUserPrompt()).systemPrompt(args.getSystemPrompt())
                        .model(fallback.getFallbackModelName()).modelType(args.getModelType())
                        .modelServer(fallback.getFallbackModelServer())
                        .maxOutputTokens(args.getMaxOutputTokens()).temperature(args.getTemperature())
                        .imageUrl(args.getImageUrl()).histories(args.getHistories())
                        .accountId(args.getAccountId()).requestId(args.getRequestId()).build());
            }
            throw new RuntimeException("LLM call failed and no fallback configured", e);
        }
    }

    private String doChat(String serverUrl, JSONObject body, String model) {
        String url = (serverUrl != null && !serverUrl.isEmpty() ? serverUrl : defaultBase)
                + "/v1/chat/completions";

        Request req = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create(body.toJSONString(), JSON_TYPE))
                .build();

        log.debug("LLM call: url={}, model={}", url, model);
        try (Response resp = httpClient.newCall(req).execute()) {
            String respBody = resp.body() != null ? resp.body().string() : "";
            if (!resp.isSuccessful()) {
                log.error("LLM API error: code={}, body={}", resp.code(), respBody);
                throw new RuntimeException("LLM API error " + resp.code());
            }
            return respBody;
        } catch (IOException e) {
            throw new RuntimeException("LLM call failed: " + e.getMessage(), e);
        }
    }

    private JSONObject buildBody(CallArgs args) {
        JSONObject body = new JSONObject();
        body.put("model", args.getModel() != null ? args.getModel() : "deepseek-v4-pro");

        JSONArray msgs = new JSONArray();
        if (args.getSystemPrompt() != null && !args.getSystemPrompt().isEmpty()) {
            JSONObject m = new JSONObject();
            m.put("role", "system"); m.put("content", args.getSystemPrompt()); msgs.add(m);
        }
        if (args.getHistories() != null) {
            for (var h : args.getHistories()) {
                if (h.getRequest() != null) {
                    JSONObject m = new JSONObject(); m.put("role", "user"); m.put("content", h.getRequest()); msgs.add(m);
                }
                if (h.getResponse() != null) {
                    JSONObject m = new JSONObject(); m.put("role", "assistant"); m.put("content", h.getResponse()); msgs.add(m);
                }
            }
        }
        JSONObject um = new JSONObject();
        um.put("role", "user"); um.put("content", args.getUserPrompt() != null ? args.getUserPrompt() : ""); msgs.add(um);
        body.put("messages", msgs);

        body.put("temperature", args.getTemperature() != null ? args.getTemperature() : 0.7);
        if (args.getMaxOutputTokens() != null && args.getMaxOutputTokens() > 0) {
            body.put("max_tokens", args.getMaxOutputTokens());
        }
        return body;
    }
}
