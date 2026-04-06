package com.hb.dokkan.agent.tool;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工具执行的结果封装。
 * <p>
 * 包含了工具执行是否成功、结果内容、错误信息以及耗时。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolResult {
    /**
     * 执行的工具名称
     */
    private String toolName;

    /**
     * 工具是否执行成功
     */
    private boolean success;

    /**
     * 工具执行返回的结果，可以是 String, Object, Map 等任意可序列化的类型
     */
    private Object result;

    /**
     * 如果执行失败，这里存放错误信息
     */
    private String error;

    /**
     * 工具执行耗时，单位毫秒
     */
    private long executionTimeMs;

    /**
     * 快速构建成功的执行结果
     *
     * @param toolName 工具名称
     * @param result 结果内容
     * @param timeMs 耗时
     * @return 成功的 ToolResult 实例
     */
    public static ToolResult success(String toolName, Object result, long timeMs) {
        return ToolResult.builder()
                .toolName(toolName)
                .success(true)
                .result(result)
                .executionTimeMs(timeMs)
                .build();
    }

    /**
     * 快速构建失败的执行结果
     *
     * @param toolName 工具名称
     * @param error 错误信息
     * @param timeMs 耗时
     * @return 失败的 ToolResult 实例
     */
    public static ToolResult failure(String toolName, String error, long timeMs) {
        return ToolResult.builder()
                .toolName(toolName)
                .success(false)
                .error(error)
                .executionTimeMs(timeMs)
                .build();
    }
}