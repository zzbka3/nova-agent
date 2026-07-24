package com.nova.agent.llm;

/**
 * LLM client interface for large language model calls
 */
public interface LLMClient {

    /**
     * Simple chat without history
     */
    String chat(String prompt, String model);

    /**
     * Chat with full CallArgs configuration
     */
    String chat(CallArgs args);

    /**
     * Chat with fallback model support
     */
    String chatWithFallback(CallArgs args, ModelFallback fallback);
}
