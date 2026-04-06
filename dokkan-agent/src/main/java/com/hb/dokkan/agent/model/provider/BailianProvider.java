package com.hb.dokkan.agent.model.provider;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.tools.ToolBase;
import com.alibaba.dashscope.tools.ToolCallFunction;
import com.alibaba.fastjson.JSON;
import com.hb.dokkan.agent.constants.ProviderEnum;
import com.hb.dokkan.agent.model.schema.ChatChunk;
import com.hb.dokkan.agent.model.schema.ChatMessage;
import com.hb.dokkan.agent.model.schema.ChatResponse;
import com.hb.dokkan.agent.model.schema.TokenUsage;
import com.hb.dokkan.agent.model.schema.ToolCall;
import com.hb.dokkan.agent.model.schema.ToolDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 阿里云百炼模型适配器实现
 */
@Slf4j
@Component("bailianProvider")
public class BailianProvider implements BaseModelProvider {

    /**
     * 阿里云百炼 API Key
     */
    @Value("${agent.bailian.api-key:}")
    private String apiKey;

    /**
     * 默认使用的模型名称
     */
    @Value("${agent.bailian.default-model:qwen-plus}")
    private String defaultModel;

    @Override
    public String getName() {
        return ProviderEnum.BAI_LIAN.getProvideName();
    }

    @Override
    public ChatResponse chat(List<ChatMessage> messages, List<ToolDefinition> tools, String model, Map<String, Object> kwargs) {
        GenerationParam param = buildParam(messages, tools, model, kwargs);
        try {
            Generation gen = new Generation();
            GenerationResult result = gen.call(param);
            return parseResponse(result);
        } catch (NoApiKeyException | InputRequiredException e) {
            log.error("DashScope api call failed.", e);
            throw new RuntimeException("Bailian model API call failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Flux<ChatChunk> streamChat(List<ChatMessage> messages, List<ToolDefinition> tools, String model, Map<String, Object> kwargs) {
        // P0 阶段暂不实现流式输出
        return Flux.error(new UnsupportedOperationException("Bailian Stream chat not yet implemented for P0"));
    }

    /**
     * 构建百炼 SDK 所需的调用参数
     *
     * @param messages 统一格式的历史消息
     * @param tools    可用的工具定义
     * @param model    指定的模型
     * @param kwargs   扩展参数
     * @return 阿里云百炼生成参数对象
     */
    private GenerationParam buildParam(List<ChatMessage> messages, List<ToolDefinition> tools, String model, Map<String, Object> kwargs) {
        String actualModel = (model != null && !model.isEmpty()) ? model : defaultModel;

        List<Message> dsMessages = new ArrayList<>();
        for (ChatMessage msg : messages) {
            Message dsMsg = Message.builder()
                    .role(msg.getRole())
                    .content(msg.getContent())
                    .build();
            dsMessages.add(dsMsg);
        }

        GenerationParam param;
        if (tools != null && !tools.isEmpty()) {
            List<ToolBase> dsTools = new ArrayList<>();
            // TODO: 此处后续需要完善工具定义的转换逻辑
            param = GenerationParam.builder()
                    .apiKey(apiKey)
                    .model(actualModel)
                    .messages(dsMessages)
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .tools(dsTools)
                    .build();
        } else {
            param = GenerationParam.builder()
                    .apiKey(apiKey)
                    .model(actualModel)
                    .messages(dsMessages)
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .build();
        }

        return param;
    }

    /**
     * 解析百炼 SDK 的返回结果并转换为统一的响应模型
     *
     * @param result 百炼 SDK 返回的原生结果
     * @return 统一的对话响应对象
     */
    private ChatResponse parseResponse(GenerationResult result) {
        Message dsMsg = result.getOutput().getChoices().getFirst().getMessage();
        
        List<ToolCall> toolCalls = null;
        if (dsMsg.getToolCalls() != null && !dsMsg.getToolCalls().isEmpty()) {
            toolCalls = dsMsg.getToolCalls().stream().map(base -> {
                if (base instanceof ToolCallFunction) {
                    ToolCallFunction func = (ToolCallFunction) base;
                    @SuppressWarnings("unchecked")
                    Map<String, Object> args = JSON.parseObject(func.getFunction().getArguments(), Map.class);
                    return ToolCall.builder()
                            .id(func.getId())
                            .functionName(func.getFunction().getName())
                            .arguments(args)
                            .build();
                }
                return null;
            }).collect(Collectors.toList());
        }

        ChatMessage message = ChatMessage.builder()
                .role(dsMsg.getRole())
                .content(dsMsg.getContent())
                .toolCalls(toolCalls)
                .build();

        TokenUsage usage = null;
        if (result.getUsage() != null) {
            usage = TokenUsage.builder()
                    .promptTokens(result.getUsage().getInputTokens())
                    .completionTokens(result.getUsage().getOutputTokens())
                    .totalTokens(result.getUsage().getTotalTokens())
                    .build();
        }

        return ChatResponse.builder()
                .message(message)
                .usage(usage)
                .model(result.getOutput().getChoices().getFirst().getMessage().getRole())
                .finishReason(result.getOutput().getChoices().getFirst().getFinishReason())
                .build();
    }
}