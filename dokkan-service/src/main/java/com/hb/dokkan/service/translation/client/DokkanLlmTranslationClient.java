package com.hb.dokkan.service.translation.client;

import com.hb.dokkan.common.constants.TranslationConstants;
import com.hb.dokkan.common.utils.JsonUtils;
import com.hb.dokkan.config.translation.LlmTranslationProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * @Description LLM翻译客户端
 * @Author stargazer
 * @Date 2026/8/15 19:30
 **/
@Slf4j
@Component
public class DokkanLlmTranslationClient {

    /**
     * 调用 OpenAI 兼容聊天补全接口翻译文本，异常时记录日志并抛出明确错误。
     *
     * @param source       待翻译文本
     * @param systemPrompt 本次翻译使用的 system prompt
     * @param properties   LLM 翻译配置
     * @return 翻译后的文本
     */
    public String translate(String source, String systemPrompt, LlmTranslationProperties properties) {
        try {
            // 构造 OpenAI 兼容请求，外部接口字段只在 Client 内部出现。
            Map<String, Object> request = buildRequest(source, systemPrompt, properties);
            String response = buildClient(properties)
                    .post()
                    .uri(properties.getChatCompletionsPath())
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(properties.getTimeoutSeconds()));
            return responseContent(response);
        } catch (IllegalStateException e) {
            log.warn("LLM translation response invalid, model={}, error={}", properties.getModel(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("LLM translation request failed, model={}, path={}",
                    properties.getModel(), properties.getChatCompletionsPath(), e);
            throw new IllegalStateException(TranslationConstants.LLM_REQUEST_FAILED_MESSAGE_PREFIX + e.getMessage(), e);
        }
    }

    /**
     * 构建 OpenAI 兼容聊天补全请求体。
     *
     * @param source       待翻译文本
     * @param systemPrompt 本次翻译使用的 system prompt
     * @param properties   LLM 翻译配置
     * @return 请求体 Map
     */
    private Map<String, Object> buildRequest(String source, String systemPrompt, LlmTranslationProperties properties) {
        return Map.of(
                TranslationConstants.LLM_REQUEST_FIELD_MODEL, properties.getModel(),
                TranslationConstants.LLM_REQUEST_FIELD_TEMPERATURE, properties.getTemperature(),
                TranslationConstants.LLM_REQUEST_FIELD_MESSAGES, List.of(
                        Map.of(TranslationConstants.LLM_REQUEST_FIELD_ROLE, TranslationConstants.LLM_ROLE_SYSTEM,
                                TranslationConstants.LLM_REQUEST_FIELD_CONTENT, systemPrompt),
                        Map.of(TranslationConstants.LLM_REQUEST_FIELD_ROLE, TranslationConstants.LLM_ROLE_USER,
                                TranslationConstants.LLM_REQUEST_FIELD_CONTENT, source)
                )
        );
    }

    /**
     * 按 LLM 配置构建 WebClient，避免 Service 直接处理 HTTP 细节。
     *
     * @param properties LLM 翻译配置
     * @return WebClient 实例
     */
    private WebClient buildClient(LlmTranslationProperties properties) {
        return WebClient.builder()
                .baseUrl(stripTrailingSlash(properties.getBaseUrl()))
                .defaultHeader(HttpHeaders.AUTHORIZATION,
                        TranslationConstants.LLM_AUTHORIZATION_BEARER_PREFIX + properties.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * 从 OpenAI 兼容响应中读取首个候选消息内容。
     *
     * @param response LLM 原始 JSON 响应
     * @return 翻译内容
     */
    private String responseContent(String response) {
        String content = JsonUtils.readFirstArrayObjectText(response,
                TranslationConstants.LLM_RESPONSE_FIELD_CHOICES,
                TranslationConstants.LLM_RESPONSE_FIELD_MESSAGE,
                TranslationConstants.LLM_REQUEST_FIELD_CONTENT);
        if (StringUtils.isNotBlank(content)) {
            return content.trim();
        }
        throw new IllegalStateException(TranslationConstants.LLM_EMPTY_CONTENT_ERROR_MESSAGE);
    }

    /**
     * 去掉 baseUrl 末尾斜杠，避免与接口 path 拼接成双斜杠。
     *
     * @param value 原始 baseUrl
     * @return 规范化后的 baseUrl
     */
    private String stripTrailingSlash(String value) {
        return value.endsWith(TranslationConstants.URL_SLASH)
                ? value.substring(0, value.length() - TranslationConstants.URL_SLASH.length()) : value;
    }
}
