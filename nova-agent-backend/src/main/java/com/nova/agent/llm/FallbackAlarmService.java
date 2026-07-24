package com.nova.agent.llm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FallbackAlarmService {

    public void alarm(String model, String fallbackModel, String reason) {
        log.warn("[FALLBACK ALARM] Model '{}' fell back to '{}': {}", model, fallbackModel, reason);
        // TODO: Send alarm to monitoring system
    }
}
