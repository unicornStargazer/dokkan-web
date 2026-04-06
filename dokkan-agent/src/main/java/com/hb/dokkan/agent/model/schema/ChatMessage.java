package com.hb.dokkan.agent.model.schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 对话消息模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    /**
     * 角色：user, assistant, system, tool 等
     */
    private String role;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 工具调用列表（当 role 为 assistant 时可能存在）
     */
    private List<ToolCall> toolCalls;
    
    /**
     * 工具调用 ID（当 role 为 tool 时必须存在，用于关联是哪个工具的返回结果）
     */
    private String toolCallId;
}