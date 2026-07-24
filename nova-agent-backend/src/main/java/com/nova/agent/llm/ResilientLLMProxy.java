package com.nova.agent.llm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resilient LLM proxy with fallback support.
 * Default implementation uses OkHttp to call LLM APIs.
 */
@Slf4j
@Component("resilientLlmProxy")
public class ResilientLLMProxy implements LLMClient {

    @Override
    public String chat(String prompt, String model) {
        log.info("LLM chat called with model: {}", model);
        // TODO: Implement actual LLM API call via OkHttp
        return "{\"choices\":[{\"message\":{\"content\":\"LLM response placeholder\"}}],\"usage\":{\"total_tokens\":0}}";
    }

    @Override
    public String chat(CallArgs args) {
        log.info("LLM chat called with args: model={}, modelType={}", args.getModel(), args.getModelType());
        // TODO: Implement actual LLM API call
        return "{\"choices\":[{\"message\":{\"content\":\"LLM response placeholder\"}}],\"usage\":{\"total_tokens\":0}}";
    }

    @Override
    public String chatWithFallback(CallArgs args, ModelFallback fallback) {
        try {
            return chat(args);
        } catch (Exception e) {
            if (fallback != null) {
                log.warn("Primary model {} failed, trying fallback {}",
                        args.getModel(), fallback.getFallbackModelName());
                CallArgs fallbackArgs = CallArgs.builder()
                        .userPrompt(args.getUserPrompt())
                        .systemPrompt(args.getSystemPrompt())
                        .model(fallback.getFallbackModelName())
                        .modelType(args.getModelType())
                        .modelServer(fallback.getFallbackModelServer())
                        .maxOutputTokens(args.getMaxOutputTokens())
                        .temperature(args.getTemperature())
                        .imageUrl(args.getImageUrl())
                        .histories(args.getHistories())
                        .accountId(args.getAccountId())
                        .requestId(args.getRequestId())
                        .build();
                return chat(fallbackArgs);
            }
            throw e;
        }
    }
}
