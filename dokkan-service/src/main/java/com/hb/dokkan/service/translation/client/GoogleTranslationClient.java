package com.hb.dokkan.service.translation.client;

import com.hb.dokkan.common.constants.TranslationConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Objects;

/**
 * @Description Google翻译客户端
 * @Author stargazer
 * @Date 2026/8/15 19:30
 **/
@Slf4j
@Component
public class GoogleTranslationClient {

    @Resource(name = "googleTranslationWebClient")
    private WebClient translationClient;

    /**
     * 调用 Google 翻译接口翻译受保护文本，异常时记录日志并返回 null，由业务服务保留源文本兜底。
     *
     * @param protectedText  受保护的合并翻译文本
     * @param sourceLanguage 源语言编码
     * @param sourceCount    原始文本数量
     * @return 翻译接口原始响应，失败时返回 null
     */
    public String translate(String protectedText, String sourceLanguage, int sourceCount) {
        try {
            // 构造 Google 翻译表单参数，保持 Client 内部封装外部接口细节。
            MultiValueMap<String, String> form = buildForm(protectedText, sourceLanguage);
            return translationClient.post()
                    .uri(TranslationConstants.GOOGLE_TRANSLATION_PATH)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(form))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(TranslationConstants.GOOGLE_TRANSLATION_TIMEOUT_SECONDS));
        } catch (Exception e) {
            log.warn("Google translation request failed, path={}, sourceLanguage={}, sourceCount={}",
                    TranslationConstants.GOOGLE_TRANSLATION_PATH, sourceLanguage, sourceCount, e);
            return null;
        }
    }

    /**
     * 构建 Google 翻译请求表单。
     *
     * @param protectedText  受保护的合并翻译文本
     * @param sourceLanguage 源语言编码
     * @return 表单参数
     */
    private MultiValueMap<String, String> buildForm(String protectedText, String sourceLanguage) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add(TranslationConstants.GOOGLE_TRANSLATION_PARAM_CLIENT,
                TranslationConstants.GOOGLE_TRANSLATION_CLIENT_VALUE);
        form.add(TranslationConstants.GOOGLE_TRANSLATION_PARAM_SOURCE_LANGUAGE, sourceLanguage);
        form.add(TranslationConstants.GOOGLE_TRANSLATION_PARAM_TARGET_LANGUAGE, TranslationConstants.LANGUAGE_ZH_CN);
        form.add(TranslationConstants.GOOGLE_TRANSLATION_PARAM_DETAIL,
                TranslationConstants.GOOGLE_TRANSLATION_DETAIL_TEXT_VALUE);
        form.add(TranslationConstants.GOOGLE_TRANSLATION_PARAM_JSON, TranslationConstants.GOOGLE_TRANSLATION_JSON_VALUE);
        form.add(TranslationConstants.GOOGLE_TRANSLATION_PARAM_QUERY,
                Objects.isNull(protectedText) ? TranslationConstants.EMPTY_TEXT : protectedText);
        return form;
    }
}
