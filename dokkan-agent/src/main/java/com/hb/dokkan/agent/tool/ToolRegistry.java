package com.hb.dokkan.agent.tool;

import com.hb.dokkan.agent.model.schema.ToolDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工具注册中心。
 * <p>
 * 负责集中管理系统内所有可供 Agent 使用的工具。
 */
@Slf4j
@Component
public class ToolRegistry {

    /**
     * 存放所有注册的工具，Key 为工具名称，Value 为工具实例
     */
    private final Map<String, BaseTool> tools = new ConcurrentHashMap<>();

    /**
     * 注册一个新的工具
     *
     * @param tool 工具实例
     */
    public void register(BaseTool tool) {
        if (tool == null || tool.getName() == null) {
            log.warn("Cannot register invalid tool.");
            return;
        }
        tools.put(tool.getName(), tool);
        log.info("Successfully registered Tool: [{}]", tool.getName());
    }

    /**
     * 移除已注册的工具
     *
     * @param name 工具名称
     */
    public void unregister(String name) {
        tools.remove(name);
        log.info("Unregistered Tool: [{}]", name);
    }

    /**
     * 根据名称获取工具实例
     *
     * @param name 工具名称
     * @return 工具实例，如果不存在则返回 null
     */
    public BaseTool get(String name) {
        return tools.get(name);
    }

    /**
     * 获取所有已注册工具的大模型定义列表
     *
     * @return 工具定义列表
     */
    public List<ToolDefinition> listToolDefinitions() {
        List<ToolDefinition> definitions = new ArrayList<>();
        for (BaseTool tool : tools.values()) {
            definitions.add(tool.toFunctionDefinition());
        }
        return definitions;
    }

    /**
     * 获取所有工具，用于生成 Prompt 或供 Executor 使用
     *
     * @return 所有工具实例的列表
     */
    public List<BaseTool> getAllTools() {
        return new ArrayList<>(tools.values());
    }
}