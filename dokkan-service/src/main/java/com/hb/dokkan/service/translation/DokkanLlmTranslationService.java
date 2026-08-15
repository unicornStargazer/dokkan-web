package com.hb.dokkan.service.translation;

import com.hb.dokkan.common.constants.TranslationConstants;
import com.hb.dokkan.common.domain.po.mysql.category.DokkanCategoryPO;
import com.hb.dokkan.config.translation.LlmTranslationProperties;
import com.hb.dokkan.infrastructure.mysql.categories.DokkanCategoryRepository;
import com.hb.dokkan.service.translation.client.DokkanLlmTranslationClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Description LLM翻译服务
 * @Author stargazer
 * @Date 2026/8/15 19:00
 **/
@Slf4j
@Service
public class DokkanLlmTranslationService {

    @Resource
    private LlmTranslationProperties properties;

    @Resource
    private DokkanLlmTranslationClient llmTranslationClient;

    @Resource
    private DokkanCategoryRepository categoryRepository;

    @Resource
    private TranslationMappingService translationMappingService;

    /**
     * 使用配置的 OpenAI 兼容 LLM 翻译单条文本。
     *
     * @param source 待翻译文本
     * @return 翻译后的简体中文文本
     */
    public String translate(String source) {
        // 先做业务配置校验，避免未启用或缺配置时请求外部接口。
        checkConfig();
        // 根据页面维护映射与本地分类英文名构建动态术语表，保证技能文本中的专有名词稳定。
        String systemPrompt = buildSystemPromptWithGlossary();
        // 外部接口调用由 Client 统一封装 HTTP、异常和响应解析。
        return llmTranslationClient.translate(source, systemPrompt, properties);
    }

    /**
     * 使用 LLM 批量翻译文本集合，保留入参顺序并对重复文本做请求去重。
     *
     * @param sourceTexts 待翻译文本集合
     * @return 翻译后的文本列表
     */
    public List<String> translateAll(Collection<String> sourceTexts) {
        if (Objects.isNull(sourceTexts) || sourceTexts.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> orderedTexts = new ArrayList<>(sourceTexts);
        List<String> distinctTexts = orderedTexts.stream()
                .filter(StringUtils::isNotBlank)
                .distinct()
                .toList();
        log.info("LLM batch translate text count:{}, distinct count:{}", orderedTexts.size(), distinctTexts.size());
        Map<String, String> translatedValues = new LinkedHashMap<>();
        for (String text : distinctTexts) {
            try {
                // 单条翻译复用现有 LLM prompt、动态术语表和 client 封装，避免批量响应错位。
                translatedValues.put(text, translate(text));
            } catch (Exception e) {
                log.error("LLM batch translate failed, text length:{}", StringUtils.length(text), e);
                throw e;
            }
        }
        return orderedTexts.stream()
                .map(text -> StringUtils.isBlank(text) ? text : translatedValues.getOrDefault(text, text))
                .toList();
    }

    /**
     * 使用 LLM 强制重新翻译文本集合，不复用普通翻译缓存。
     *
     * @param sourceTexts 待重新翻译文本集合
     * @return 重新翻译后的文本列表
     */
    public List<String> retranslateAll(Collection<String> sourceTexts) {
        // 当前 LLM 链路不接入普通翻译缓存，强制重翻与批量翻译保持一致。
        return translateAll(sourceTexts);
    }

    /**
     * 构建带运行期分类术语表的 system prompt。
     *
     * @return system prompt
     */
    private String buildSystemPromptWithGlossary() {
        Map<String, String> glossary = buildRuntimeGlossary();
        if (glossary.isEmpty()) {
            return properties.getSystemPrompt();
        }
        String terms = glossary.entrySet().stream()
                .limit(TranslationConstants.LLM_DYNAMIC_GLOSSARY_MAX_TERMS)
                .map(entry -> TranslationConstants.LLM_DYNAMIC_GLOSSARY_ENTRY_FORMAT.formatted(
                        entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(TranslationConstants.BATCH_ROW_SEPARATOR));
        return properties.getSystemPrompt()
                + TranslationConstants.LLM_PROMPT_SECTION_SEPARATOR
                + TranslationConstants.LLM_DYNAMIC_GLOSSARY_TITLE
                + TranslationConstants.BATCH_ROW_SEPARATOR
                + terms;
    }

    /**
     * 根据页面维护映射和本地分类中英文名称构建运行期术语表。
     *
     * @return 英文原文到本地中文术语的映射
     */
    private Map<String, String> buildRuntimeGlossary() {
        try {
            Map<String, String> glossary = new LinkedHashMap<>();
            // 页面维护的 DB 映射优先级最高，后续分类中英文映射不覆盖用户手动配置。
            glossary.putAll(translationMappingService.listEnabledMappingMap());
            categoryRepository.list().stream()
                    .filter(row -> StringUtils.isNoneBlank(row.getCategoryNameEn(), row.getCategoryName()))
                    .forEach(row -> glossary.putIfAbsent(row.getCategoryNameEn(), row.getCategoryName()));
            return glossary;
        } catch (Exception e) {
            log.warn("build LLM runtime glossary failed", e);
            return Map.of();
        }
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
