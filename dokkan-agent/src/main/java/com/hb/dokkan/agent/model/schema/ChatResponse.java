package com.hb.dokkan.agent.model.schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 同步对话的统一响应模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    /**
     * 大模型返回的消息对象
     */
    private ChatMessage message;
    
    /**
     * 本次请求的 Token 消耗情况
     */
    private TokenUsage usage;
    
    /**
     * 实际使用的模型名称
     */
    private String model;
    
    /**
     * 结束原因，如 "stop"（正常结束）, "tool_calls"（调用工具）等
     */
    private String finishReason;
}