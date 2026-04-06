package com.hb.dokkan.agent.tool;

import com.hb.dokkan.agent.model.schema.ToolCall;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 工具执行器。
 * <p>
 * 负责安全、可控地调用工具（如超时控制、参数校验）。
 */
@Slf4j
@Component
public class ToolExecutor {

    private final ToolRegistry registry;
    private final ExecutorService executorService;

    /**
     * 工具执行的最大超时时间，默认 30 秒
     */
    @Value("${agent.tool.timeout:30}")
    private int timeoutSeconds;

    public ToolExecutor(ToolRegistry registry) {
        this.registry = registry;
        // 使用一个简单的缓存线程池来控制工具执行超时
        this.executorService = Executors.newCachedThreadPool();
    }

    /**
     * 执行单个工具调用
     *
     * @param toolCall 大模型返回的工具调用请求
     * @return 工具执行结果
     */
    public ToolResult execute(ToolCall toolCall) {
        long startTime = System.currentTimeMillis();
        String toolName = toolCall.getFunctionName();
        
        BaseTool tool = registry.get(toolName);
        if (tool == null) {
            log.warn("Tool not found: {}", toolName);
            return ToolResult.failure(toolName, "Tool '" + toolName + "' not found", System.currentTimeMillis() - startTime);
        }

        try {
            // 1. 参数校验
            if (!tool.validateArgs(toolCall.getArguments())) {
                return ToolResult.failure(toolName, "Invalid arguments provided for tool.", System.currentTimeMillis() - startTime);
            }

            // 2. 超时控制执行
            Future<ToolResult> future = executorService.submit(() -> tool.execute(toolCall.getArguments()));
            return future.get(timeoutSeconds, TimeUnit.SECONDS);

        } catch (TimeoutException e) {
            log.error("Tool execution timeout: {}", toolName);
            return ToolResult.failure(toolName, "Tool execution timeout after " + timeoutSeconds + " seconds.", System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("Tool execution failed: {}", toolName, e);
            return ToolResult.failure(toolName, "Error executing tool: " + e.getMessage(), System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 批量执行多个工具调用
     *
     * @param toolCalls 工具调用请求列表
     * @return 工具执行结果列表
     */
    public List<ToolResult> batchExecute(List<ToolCall> toolCalls) {
        List<ToolResult> results = new ArrayList<>();
        for (ToolCall call : toolCalls) {
            results.add(this.execute(call));
        }
        return results;
    }
}