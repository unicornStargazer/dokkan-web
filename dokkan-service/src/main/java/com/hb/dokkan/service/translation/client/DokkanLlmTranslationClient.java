package com.hb.dokkan.service.translation.client;

import com.hb.dokkan.common.constants.TranslationConstants;
import com.hb.dokkan.common.domain.dto.translation.LlmTokenUsageDTO;
import com.hb.dokkan.common.domain.dto.translation.LlmTranslationResultDTO;
import com.hb.dokkan.common.utils.JsonUtils;
import com.hb.dokkan.config.translation.LlmTranslationProperties;
import com.hb.dokkan.service.translation.exception.LlmModelQuotaExceededException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

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
     * @param model        本次请求使用的模型
     * @return 翻译结果，包含译文与 token 用量
     */
    public LlmTranslationResultDTO translate(String source, String systemPrompt, LlmTranslationProperties properties,
                                             String model) {
        long startTime = System.currentTimeMillis();
        try {
            log.info("LLM translation request, model:{}, path:{}, sourceLength:{}, promptLength:{}, timeoutSeconds:{}, temperature:{}",
                    model, properties.getChatCompletionsPath(), StringUtils.length(source),
                    StringUtils.length(systemPrompt), properties.getTimeoutSeconds(), properties.getTemperature());
            // 构造 OpenAI 兼容请求，外部接口字段只在 Client 内部出现。
            Map<String, Object> request = buildRequest(source, systemPrompt, properties, model);
            String response = buildClient(properties)
                    .post()
                    .uri(properties.getChatCompletionsPath())
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(properties.getTimeoutSeconds()));
            LlmTranslationResultDTO result = responseResult(response, model);
            LlmTokenUsageDTO usage = result.getUsage();
            log.info("LLM translation response, model:{}, path:{}, costMs:{}, responseLength:{}, contentLength:{}, totalTokens:{}",
                    model, properties.getChatCompletionsPath(), System.currentTimeMillis() - startTime,
                    StringUtils.length(response), StringUtils.length(result.getContent()),
                    Objects.isNull(usage) ? null : usage.getTotalTokens());
            return result;
        } catch (LlmModelQuotaExceededException e) {
            log.warn("LLM translation quota exceeded, model={}, path={}, costMs={}, error={}",
                    model, properties.getChatCompletionsPath(), System.currentTimeMillis() - startTime,
                    e.getMessage());
            throw e;
        } catch (IllegalStateException e) {
            log.warn("LLM translation response invalid, model={}, path={}, costMs={}, error={}",
                    model, properties.getChatCompletionsPath(), System.currentTimeMillis() - startTime,
                    e.getMessage());
            throw e;
        } catch (WebClientResponseException e) {
            if (isQuotaError(e)) {
                throw new LlmModelQuotaExceededException(TranslationConstants.LLM_QUOTA_EXCEEDED_MESSAGE_PREFIX
                        + e.getResponseBodyAsString(), e);
            }
            log.error("LLM translation request failed, model={}, path={}, sourceLength={}, costMs={}, status={}",
                    model, properties.getChatCompletionsPath(), StringUtils.length(source),
                    System.currentTimeMillis() - startTime, e.getStatusCode(), e);
            throw new IllegalStateException(TranslationConstants.LLM_REQUEST_FAILED_MESSAGE_PREFIX + e.getMessage(), e);
        } catch (Exception e) {
            if (isQuotaError(e)) {
                throw new LlmModelQuotaExceededException(TranslationConstants.LLM_QUOTA_EXCEEDED_MESSAGE_PREFIX
                        + e.getMessage(), e);
            }
            log.error("LLM translation request failed, model={}, path={}, sourceLength={}, costMs={}",
                    model, properties.getChatCompletionsPath(), StringUtils.length(source),
                    System.currentTimeMillis() - startTime, e);
            throw new IllegalStateException(TranslationConstants.LLM_REQUEST_FAILED_MESSAGE_PREFIX + e.getMessage(), e);
        }
    }

    /**
     * 构建 OpenAI 兼容聊天补全请求体。
     *
     * @param source       待翻译文本
     * @param systemPrompt 本次翻译使用的 system prompt
     * @param properties   LLM 翻译配置
     * @param model        本次请求使用的模型
     * @return 请求体 Map
     */
    private Map<String, Object> buildRequest(String source, String systemPrompt, LlmTranslationProperties properties,
                                             String model) {
        return Map.of(
                TranslationConstants.LLM_REQUEST_FIELD_MODEL, model,
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
     * 从 OpenAI 兼容响应中读取首个候选消息内容和 token 用量。
     *
     * @param response LLM 原始 JSON 响应
     * @param model    本次请求使用的模型
     * @return 翻译结果
     */
    private LlmTranslationResultDTO responseResult(String response, String model) {
        String content = JsonUtils.readFirstArrayObjectText(response,
                TranslationConstants.LLM_RESPONSE_FIELD_CHOICES,
                TranslationConstants.LLM_RESPONSE_FIELD_MESSAGE,
                TranslationConstants.LLM_REQUEST_FIELD_CONTENT);
        if (StringUtils.isBlank(content)) {
            throw new IllegalStateException(TranslationConstants.LLM_EMPTY_CONTENT_ERROR_MESSAGE);
        }
        LlmTranslationResultDTO result = new LlmTranslationResultDTO();
        result.setModel(model);
        result.setContent(content.trim());
        result.setUsage(JsonUtils.readObjectField(response, TranslationConstants.LLM_RESPONSE_FIELD_USAGE,
                LlmTokenUsageDTO.class));
        return result;
    }

    /**
     * 判断 HTTP 响应异常是否属于模型额度不足或限流错误。
     *
     * @param e WebClient 响应异常
     * @return true 表示可切换模型重试
     */
    private boolean isQuotaError(WebClientResponseException e) {
        return HttpStatus.TOO_MANY_REQUESTS.equals(e.getStatusCode())
                || HttpStatus.PAYMENT_REQUIRED.equals(e.getStatusCode())
                || containsQuotaKeyword(e.getResponseBodyAsString())
                || containsQuotaKeyword(e.getMessage());
    }

    /**
     * 判断普通异常是否包含模型额度不足语义。
     *
     * @param e 原始异常
     * @return true 表示可切换模型重试
     */
    private boolean isQuotaError(Exception e) {
        return containsQuotaKeyword(e.getMessage());
    }

    /**
     * 判断错误文本是否包含常见额度不足关键字。
     *
     * @param message 错误文本
     * @return true 表示命中限额语义
     */
    private boolean containsQuotaKeyword(String message) {
        if (StringUtils.isBlank(message)) {
            return false;
        }
        String lowerCaseMessage = message.toLowerCase(Locale.ROOT);
        return lowerCaseMessage.contains(TranslationConstants.LLM_QUOTA_ERROR_KEYWORD_QUOTA)
                || lowerCaseMessage.contains(TranslationConstants.LLM_QUOTA_ERROR_KEYWORD_INSUFFICIENT)
                || lowerCaseMessage.contains(TranslationConstants.LLM_QUOTA_ERROR_KEYWORD_RATE_LIMIT)
                || message.contains(TranslationConstants.LLM_QUOTA_ERROR_KEYWORD_BALANCE_CN)
                || message.contains(TranslationConstants.LLM_QUOTA_ERROR_KEYWORD_QUOTA_CN);
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
