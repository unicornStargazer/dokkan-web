package com.hb.dokkan.agent.model.schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 工具调用信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolCall {
    /**
     * 工具调用的唯一 ID
     */
    private String id;
    
    /**
     * 要调用的函数/工具名称
     */
    private String functionName;
    
    /**
     * 工具调用的参数键值对
     */
    private Map<String, Object> arguments;
}