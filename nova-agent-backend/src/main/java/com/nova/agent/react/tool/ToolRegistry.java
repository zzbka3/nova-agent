package com.nova.agent.react.tool;

import com.nova.agent.react.repository.ToolDefinitionMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Tool definition registry with in-memory caching.
 * Loads all enabled tool definitions from DB on startup.
 */
@Slf4j
@Component
public class ToolRegistry {

    @Autowired
    private ToolDefinitionMapper toolDefinitionMapper;

    private final ConcurrentHashMap<String, ToolDefinition> cache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        refresh();
    }

    /**
     * Reload all enabled tool definitions from the database.
     */
    public void refresh() {
        try {
            List<ToolDefinition> definitions = toolDefinitionMapper.selectByStatus(1);
            cache.clear();
            for (ToolDefinition def : definitions) {
                cache.put(def.getToolId(), def);
            }
            log.info("ToolRegistry refreshed: {} tools loaded", cache.size());
        } catch (Exception e) {
            log.error("ToolRegistry refresh failed", e);
        }
    }

    /**
     * Get a single tool definition by its toolId.
     */
    public ToolDefinition getById(String toolId) {
        ToolDefinition def = cache.get(toolId);
        if (def == null) {
            // Lazy-load from DB as fallback
            def = toolDefinitionMapper.selectByToolId(toolId);
            if (def != null && def.getStatus() != null && def.getStatus() == 1) {
                cache.put(toolId, def);
            }
        }
        return def;
    }

    /**
     * Batch-load tool definitions by their IDs.
     */
    public Map<String, ToolDefinition> getByIds(Collection<String> toolIds) {
        Map<String, ToolDefinition> result = new LinkedHashMap<>();
        List<String> missing = new ArrayList<>();

        for (String id : toolIds) {
            ToolDefinition def = cache.get(id);
            if (def != null) {
                result.put(id, def);
            } else {
                missing.add(id);
            }
        }

        if (!missing.isEmpty()) {
            List<ToolDefinition> loaded = toolDefinitionMapper.selectByToolIds(missing);
            for (ToolDefinition def : loaded) {
                cache.put(def.getToolId(), def);
                result.put(def.getToolId(), def);
            }
        }

        return result;
    }

    /**
     * List all cached tool definitions.
     */
    public List<ToolDefinition> listAll() {
        return new ArrayList<>(cache.values());
    }
}
