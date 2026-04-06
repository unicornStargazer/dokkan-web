package com.hb.dokkan.agent.model.schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 工具定义，用于向大模型描述可用工具
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolDefinition {
    /**
     * 工具名称
     */
    private String name;
    
    /**
     * 工具功能描述，大模型根据此描述决定是否调用
     */
    private String description;
    
    /**
     * 工具参数的 JSON Schema 定义
     */
    private Map<String, Object> parameters;
}