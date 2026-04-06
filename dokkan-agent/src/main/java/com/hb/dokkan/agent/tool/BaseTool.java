package com.hb.dokkan.agent.tool;

import com.hb.dokkan.agent.model.schema.ToolDefinition;

import java.util.Map;

/**
 * 工具基类/接口。
 * <p>
 * 所有供 Agent 调用的工具都必须实现此接口（或通过 @AgentTool 注解动态包装成此接口的实现）。
 */
public interface BaseTool {

    /**
     * 获取工具的唯一名称
     *
     * @return 工具名称
     */
    String getName();

    /**
     * 获取工具的功能描述
     *
     * @return 工具描述
     */
    String getDescription();

    /**
     * 将该工具转换为供大模型使用的规范定义 (Function Calling 格式)
     *
     * @return 规范的工具定义
     */
    ToolDefinition toFunctionDefinition();

    /**
     * 校验传入的参数是否符合该工具的要求
     *
     * @param args 大模型传入的参数
     * @return 是否有效
     */
    boolean validateArgs(Map<String, Object> args);

    /**
     * 实际执行工具逻辑
     *
     * @param args 大模型提供的参数字典
     * @return 执行结果封装
     */
    ToolResult execute(Map<String, Object> args);
}