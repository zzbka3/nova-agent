package com.nova.agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Data
@Configuration
@ConfigurationProperties(prefix = "thread-pool")
public class ThreadPoolConfig {

    private PoolConfig agentExecutor;
    private PoolConfig heavyExecutor;
    private PoolConfig debugExecutor;

    @Data
    public static class PoolConfig {
        private int coreSize = 4;
        private int maxSize = 8;
        private int queueCapacity = 100;
    }

    @Bean("agentExecutor")
    public ThreadPoolTaskExecutor agentExecutor() {
        return createExecutor("agent-exec-", agentExecutor);
    }

    @Bean("heavyExecutor")
    public ThreadPoolTaskExecutor heavyExecutor() {
        return createExecutor("heavy-exec-", heavyExecutor);
    }

    @Bean("debugExecutor")
    public ThreadPoolTaskExecutor debugExecutor() {
        return createExecutor("debug-exec-", debugExecutor);
    }

    private ThreadPoolTaskExecutor createExecutor(String prefix, PoolConfig config) {
        if (config == null) {
            config = new PoolConfig();
        }
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(config.getCoreSize());
        executor.setMaxPoolSize(config.getMaxSize());
        executor.setQueueCapacity(config.getQueueCapacity());
        executor.setThreadNamePrefix(prefix);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
