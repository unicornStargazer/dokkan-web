package com.hb.dokkan.service.translation;

import com.hb.dokkan.common.constants.TranslationConstants;
import com.hb.dokkan.common.domain.dto.translation.LlmTranslationResultDTO;
import com.hb.dokkan.common.domain.po.mysql.category.DokkanCategoryPO;
import com.hb.dokkan.config.http.HttpPoolProperties;
import com.hb.dokkan.config.translation.LlmTranslationProperties;
import com.hb.dokkan.infrastructure.mysql.categories.DokkanCategoryRepository;
import com.hb.dokkan.service.translation.client.DokkanLlmTranslationClient;
import com.hb.dokkan.service.translation.exception.LlmModelQuotaExceededException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Semaphore;
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

    @Resource
    private DokkanLlmModelQuotaService llmModelQuotaService;

    @Resource
    private HttpPoolProperties httpPoolProperties;

    @Resource(name = "dokkanExecutor")
    private TaskExecutor dokkanExecutor;

    /** LLM 外部调用全局并发许可，避免批量卡片重翻时叠加触发过多模型请求。 */
    private Semaphore llmSemaphore;

    /**
     * 初始化 LLM 全局并发许可。
     */
    @PostConstruct
    public void initConcurrency() {
        llmSemaphore = new Semaphore(llmConcurrencyPermits());
        log.info("LLM translation concurrency initialized, permits:{}", llmConcurrencyPermits());
    }

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
        // 按模型剩余额度选择可用模型，并在额度不足时自动切换重试。
        return translateWithModelSwitch(source, systemPrompt);
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
        long startTime = System.currentTimeMillis();
        List<String> orderedTexts = new ArrayList<>(sourceTexts);
        List<String> distinctTexts = orderedTexts.stream()
                .filter(StringUtils::isNotBlank)
                .distinct()
                .toList();
        int permits = llmConcurrencyPermits();
        log.info("LLM batch translate started, textCount:{}, distinctCount:{}, concurrency:{}",
                orderedTexts.size(), distinctTexts.size(), permits);
        List<CompletableFuture<TranslationResult>> futures = distinctTexts.stream()
                .map(text -> CompletableFuture.supplyAsync(() -> translateWithPermit(text), dokkanExecutor))
                .toList();
        Map<String, String> translatedValues = new LinkedHashMap<>();
        futures.stream()
                .map(this::joinTranslationResult)
                .forEach(result -> translatedValues.put(result.source(), result.target()));
        log.info("LLM batch translate completed, textCount:{}, distinctCount:{}, costMs:{}",
                orderedTexts.size(), distinctTexts.size(), System.currentTimeMillis() - startTime);
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
     * 按模型余额智能选择模型并在额度不足时自动切换。
     *
     * @param source       待翻译文本
     * @param systemPrompt 本次翻译使用的 system prompt
     * @return 翻译后的文本
     */
    private String translateWithModelSwitch(String source, String systemPrompt) {
        DokkanLlmModelQuotaService.TokenEstimate estimate = llmModelQuotaService.estimate(source, systemPrompt);
        List<String> attemptedModels = new ArrayList<>();
        int maxAttempts = Math.min(Math.max(properties.getQuotaSwitchMaxAttempts(),
                TranslationConstants.LLM_MIN_CONCURRENCY_PERMITS), llmModelQuotaService.modelCount());
        RuntimeException lastException = null;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            DokkanLlmModelQuotaService.ModelSelection selection = llmModelQuotaService.selectModel(estimate,
                    attemptedModels);
            attemptedModels.add(selection.model());
            try {
                log.info("LLM model selected, model:{}, remainingTokens:{}, estimatedPromptTokens:{}, estimatedTotalTokens:{}, attempt:{}/{}",
                        selection.model(), selection.remainingTokens(), selection.estimatedPromptTokens(),
                        selection.estimatedTotalTokens(), attempt + 1, maxAttempts);
                // 外部接口调用由 Client 统一封装 HTTP、异常和响应解析。
                LlmTranslationResultDTO result = llmTranslationClient.translate(source, systemPrompt, properties,
                        selection.model());
                llmModelQuotaService.recordSuccess(selection.model(), result.getUsage(), estimate);
                return result.getContent();
            } catch (LlmModelQuotaExceededException e) {
                lastException = e;
                // 当前模型额度不足时标记耗尽并继续尝试下一个余额可用模型。
                llmModelQuotaService.markQuotaExceeded(selection.model(), e.getMessage());
                log.warn("LLM model quota switch triggered, model:{}, attemptedModels:{}",
                        selection.model(), attemptedModels, e);
            }
        }
        throw Objects.isNull(lastException)
                ? new IllegalStateException(TranslationConstants.LLM_ALL_MODELS_QUOTA_EXHAUSTED_ERROR_MESSAGE)
                : lastException;
    }

    /**
     * 在并发许可控制下翻译单条文本。
     *
     * @param source 待翻译文本
     * @return 翻译结果
     */
    private TranslationResult translateWithPermit(String source) {
        boolean acquired = false;
        try {
            llmSemaphore.acquire();
            acquired = true;
            // 单条翻译复用现有 LLM prompt、动态术语表和 client 封装，避免批量响应错位。
            return new TranslationResult(source, translate(source));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(TranslationConstants.LLM_CONCURRENCY_INTERRUPTED_ERROR_MESSAGE, e);
        } catch (Exception e) {
            log.error("LLM batch translate failed, text length:{}", StringUtils.length(source), e);
            throw e;
        } finally {
            if (acquired) {
                llmSemaphore.release();
            }
        }
    }

    /**
     * 获取并发翻译 Future 结果并透传真实异常。
     *
     * @param future 翻译 Future
     * @return 翻译结果
     */
    private TranslationResult joinTranslationResult(CompletableFuture<TranslationResult> future) {
        try {
            return future.join();
        } catch (CompletionException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException(cause);
        }
    }

    /**
     * 获取 LLM 并发许可数，复用 HTTP 并发配置并做上限保护。
     *
     * @return 并发许可数
     */
    private int llmConcurrencyPermits() {
        int configured = httpPoolProperties.getConcurrency().getSemaphorePermits();
        return Math.max(TranslationConstants.LLM_MIN_CONCURRENCY_PERMITS,
                Math.min(configured, TranslationConstants.LLM_MAX_CONCURRENCY_PERMITS));
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
        if (StringUtils.isAnyBlank(properties.getBaseUrl(), properties.getApiKey())
                || llmModelQuotaService.modelCount() <= 0) {
            throw new IllegalStateException(TranslationConstants.LLM_CONFIG_ERROR_MESSAGE);
        }
    }

    /**
     * 单条翻译结果。
     *
     * @param source 原文
     * @param target 译文
     */
    private record TranslationResult(String source, String target) {
    }
}
