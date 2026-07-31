package com.nova.agent.react.runtime;

import com.alibaba.fastjson.JSON;
import com.nova.agent.entity.UserInvokeInput;
import com.nova.agent.react.entity.AgentState;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Manages AgentState persistence using Redis via Redisson.
 * Key format: react:state:{appId}:{conversationId}
 * TTL: 30 minutes, refreshed on each save.
 */
@Slf4j
@Component
public class AgentStateManager {

    private static final String KEY_PREFIX = "react:state:";
    private static final Duration TTL = Duration.ofMinutes(30);

    @Autowired
    private RedissonClient redissonClient;

    /**
     * Load existing state from Redis, or create a new one.
     */
    public AgentState loadOrCreate(String appId, String conversationId,
                                    UserInvokeInput input, String agentName, int maxLoop) {
        String key = buildKey(appId, conversationId);
        RBucket<String> bucket = redissonClient.getBucket(key);
        String json = bucket.get();

        if (json != null && !json.isEmpty()) {
            try {
                AgentState existing = JSON.parseObject(json, AgentState.class);
                existing.getRuntime().setLastUpdateTime(System.currentTimeMillis());
                log.info("AgentState loaded from Redis: key={}, loop={}", key,
                        existing.getPlanner() != null ? existing.getPlanner().getLoop() : 0);
                return existing;
            } catch (Exception e) {
                log.warn("Failed to parse existing AgentState, creating new one: key={}", key, e);
            }
        }

        // Create fresh state
        AgentState state = new AgentState();
        state.getConversation().setConversationId(conversationId);
        state.getConversation().setAgentId(appId);
        state.getConversation().setCurrentQuery(input.getQuery());
        state.getPlanner().setMaxLoop(maxLoop);
        state.getPlanner().setLoop(0);
        state.getRuntime().setStatus("RUNNING");
        state.getRuntime().setStartTime(System.currentTimeMillis());
        state.getRuntime().setLastUpdateTime(System.currentTimeMillis());
        log.info("AgentState created: key={}", key);
        return state;
    }

    /**
     * Persist AgentState to Redis and refresh TTL.
     */
    public void save(AgentState state) {
        if (state == null || state.getConversation() == null) return;
        String key = buildKey(state.getConversation().getAgentId(),
                state.getConversation().getConversationId());
        state.getRuntime().setLastUpdateTime(System.currentTimeMillis());
        String json = JSON.toJSONString(state);
        RBucket<String> bucket = redissonClient.getBucket(key);
        bucket.set(json, TTL);
        log.debug("AgentState saved: key={}, loop={}", key,
                state.getPlanner() != null ? state.getPlanner().getLoop() : 0);
    }

    /**
     * Delete the state for a conversation (e.g., on explicit session end).
     */
    public void delete(String appId, String conversationId) {
        String key = buildKey(appId, conversationId);
        redissonClient.getBucket(key).delete();
        log.info("AgentState deleted: key={}", key);
    }

    private String buildKey(String appId, String conversationId) {
        return KEY_PREFIX + appId + ":" + conversationId;
    }
}
