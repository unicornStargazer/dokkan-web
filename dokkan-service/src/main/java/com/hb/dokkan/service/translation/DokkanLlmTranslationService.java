package com.hb.dokkan.service.translation;

import com.hb.dokkan.common.constants.TranslationConstants;
import com.hb.dokkan.config.translation.LlmTranslationProperties;
import com.hb.dokkan.service.translation.client.DokkanLlmTranslationClient;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @Description LLM翻译服务
 * @Author stargazer
 * @Date 2026/8/15 19:00
 **/
@Service
public class DokkanLlmTranslationService {

    @Resource
    private LlmTranslationProperties properties;

    @Resource
    private DokkanLlmTranslationClient llmTranslationClient;

    /**
     * 使用配置的 OpenAI 兼容 LLM 翻译单条文本。
     *
     * @param source 待翻译文本
     * @return 翻译后的简体中文文本
     */
    public String translate(String source) {
        // 先做业务配置校验，避免未启用或缺配置时请求外部接口。
        checkConfig();
        // 外部接口调用由 Client 统一封装 HTTP、异常和响应解析。
        return llmTranslationClient.translate(source, properties);
    }

    /**
     * 校验 LLM 翻译配置是否满足调用条件。
     */
    private void checkConfig() {
        if (!properties.isEnabled()) {
            throw new IllegalStateException(TranslationConstants.LLM_DISABLED_ERROR_MESSAGE);
        }
        if (StringUtils.isAnyBlank(properties.getBaseUrl(), properties.getApiKey(), properties.getModel())) {
            throw new IllegalStateException(TranslationConstants.LLM_CONFIG_ERROR_MESSAGE);
        }
    }
}
