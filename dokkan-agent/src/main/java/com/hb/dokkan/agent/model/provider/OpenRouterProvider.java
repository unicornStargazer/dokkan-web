package com.hb.dokkan.agent.model.provider;

import com.alibaba.fastjson.JSON;
import com.hb.dokkan.agent.constants.ProviderEnum;
import com.hb.dokkan.agent.model.schema.ChatChunk;
import com.hb.dokkan.agent.model.schema.ChatMessage;
import com.hb.dokkan.agent.model.schema.ChatResponse;
import com.hb.dokkan.agent.model.schema.ToolDefinition;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenRouter 模型适配器实现（兼容 OpenAI 接口协议）
 */
@Slf4j
@Component("openRouterProvider")
public class OpenRouterProvider implements BaseModelProvider {

    /**
     * OpenRouter API Key
     */
    @Value("${agent.openrouter.api-key:}")
    private String apiKey;

    /**
     * OpenRouter API 基础 URL
     */
    @Value("${agent.openrouter.base-url:https://openrouter.ai/api/v1}")
    private String baseUrl;

    /**
     * 默认使用的模型名称
     */
    @Value("${agent.openrouter.default-model:openai/gpt-4o}")
    private String defaultModel;

    /**
     * OkHttp 客户端实例
     */
    private final OkHttpClient client = new OkHttpClient();

    @Override
    public String getName() {
        return ProviderEnum.OPEN_ROUTE.getProvideName();
    }

    @Override
    public ChatResponse chat(List<ChatMessage> messages, List<ToolDefinition> tools, String model, Map<String, Object> kwargs) {
        String actualModel = (model != null && !model.isEmpty()) ? model : defaultModel;

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", actualModel);
        requestBody.put("messages", messages);

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                JSON.toJSONString(requestBody)
        );

        Request request = new Request.Builder()
                .url(baseUrl + "/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code " + response);
            }
            // TODO: 此处仅为骨架实现，后续需补充完整的响应 JSON 解析逻辑
            return ChatResponse.builder().build();
        } catch (IOException e) {
            log.error("OpenRouter API call failed", e);
            throw new RuntimeException("OpenRouter API call failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Flux<ChatChunk> streamChat(List<ChatMessage> messages, List<ToolDefinition> tools, String model, Map<String, Object> kwargs) {
        // P0 阶段暂不实现流式输出
        return Flux.error(new UnsupportedOperationException("OpenRouter Stream chat not yet implemented for P0"));
    }
}