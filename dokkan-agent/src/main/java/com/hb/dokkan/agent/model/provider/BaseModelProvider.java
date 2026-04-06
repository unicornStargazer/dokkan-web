package com.hb.dokkan.agent.model.provider;

import com.hb.dokkan.agent.model.schema.ChatChunk;
import com.hb.dokkan.agent.model.schema.ChatMessage;
import com.hb.dokkan.agent.model.schema.ChatResponse;
import com.hb.dokkan.agent.model.schema.ToolDefinition;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * 模型提供者基类，所有模型适配器必须实现此接口
 */
public interface BaseModelProvider {

    /**
     * 获取提供者的唯一标识名称（如 bailian, openrouter）
     *
     * @return 提供者名称
     */
    String getName();

    /**
     * 同步对话调用
     *
     * @param messages 历史对话消息列表
     * @param tools    可选的工具定义列表
     * @param model    指定的模型名称（为空则使用默认模型）
     * @param kwargs   扩展参数（如 temperature 等）
     * @return 完整的对话响应
     */
    ChatResponse chat(List<ChatMessage> messages, List<ToolDefinition> tools, String model, Map<String, Object> kwargs);

    /**
     * 流式对话调用
     *
     * @param messages 历史对话消息列表
     * @param tools    可选的工具定义列表
     * @param model    指定的模型名称（为空则使用默认模型）
     * @param kwargs   扩展参数（如 temperature 等）
     * @return 包含增量响应的响应流
     */
    Flux<ChatChunk> streamChat(List<ChatMessage> messages, List<ToolDefinition> tools, String model, Map<String, Object> kwargs);
}