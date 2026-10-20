package com.hb.dokkan.translation;

import com.hb.dokkan.translation.domain.TranslationBatchRequest;
import com.hb.dokkan.translation.domain.TranslationItem;
import com.hb.dokkan.translation.domain.TranslationItemResult;
import com.hb.dokkan.translation.domain.TranslationProviderType;
import com.hb.dokkan.translation.domain.TranslationScene;
import com.hb.dokkan.translation.provider.TranslationProvider;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

/** 统一处理业务场景路由、映射、缓存和译文结构校验。 */
@Slf4j
@Service
public class BusinessTranslationService {

    @Resource
    private TranslationMappingService mappingService;

    @Resource
    private List<TranslationProvider> providers;

    @Resource
    private TranslationResultValidator resultValidator;

    private final Map<String, String> translationCache = new ConcurrentHashMap<>();

    /** 通知 provider 刷新其依赖的术语快照。 */
    public void refreshGlossary() {
        translationCache.clear();
        providers.forEach(TranslationProvider::refreshGlossary);
    }

    /**
     * 翻译一组同场景文本，并按输入关联键返回逐项状态。
     *
     * @param request 场景化批量翻译请求
     * @return 与输入顺序一致的逐项结果
     */
    public List<TranslationItemResult> translate(TranslationBatchRequest request) {
        if (request == null || request.scene() == null || request.providerType() == null
                || request.items() == null || request.items().isEmpty()) {
            return Collections.emptyList();
        }
        long startTime = System.currentTimeMillis();
        Map<String, String> mappings = request.force() ? Map.of() : mappingService.listEnabledMappingMap();
        List<TranslationItemResult> results = new ArrayList<>(Collections.nCopies(request.items().size(), null));
        Map<String, String> mappingSnapshot = mappings;
        int mappingVersion = mappingSnapshot.hashCode();
        List<TranslationItem> pending = new ArrayList<>();
        Map<String, List<Integer>> pendingIndexes = new LinkedHashMap<>();
        for (int index = 0; index < request.items().size(); index++) {
            TranslationItem item = request.items().get(index);
            if (item == null || StringUtils.isBlank(item.sourceText())) {
                results.set(index, result(item, item == null ? null : item.sourceText(),
                        TranslationItemResult.Status.SUCCESS, TranslationItemResult.Source.NONE, List.of()));
                continue;
            }
            if (!request.force() && mappings.containsKey(item.sourceText())) {
                results.set(index, result(item, mappings.get(item.sourceText()),
                        TranslationItemResult.Status.SUCCESS, TranslationItemResult.Source.MAPPING, List.of()));
                continue;
            }
            String cacheKey = cacheKey(request, mappingVersion, item);
            String cached = request.force() ? null : translationCache.get(cacheKey);
            if (StringUtils.isNotBlank(cached)) {
                results.set(index, result(item, cached, TranslationItemResult.Status.SUCCESS,
                        TranslationItemResult.Source.CACHE, List.of()));
                continue;
            }
            List<Integer> indexes = pendingIndexes.computeIfAbsent(contentKey(item), ignored -> new ArrayList<>());
            if (indexes.isEmpty()) {
                pending.add(item);
            }
            indexes.add(index);
        }
        if (!pending.isEmpty()) {
            translatePending(request, pending, pendingIndexes, results, mappingVersion);
        }
        log.info("business translation completed, scene:{}, itemCount:{}, providerItemCount:{}, cacheHitCount:{}, costMs:{}",
                request.scene(), request.items().size(), pending.size(),
                request.items().size() - pending.size(), System.currentTimeMillis() - startTime);
        return List.copyOf(results);
    }

    /**
     * 通过兼容规则选择 provider，执行批量翻译并验证返回结构。
     *
     * @param request 原始请求
     * @param pending 未命中映射和缓存的文本项
     * @param results 结果累积表
     */
    private void translatePending(TranslationBatchRequest request, List<TranslationItem> pending,
                                  Map<String, List<Integer>> pendingIndexes,
                                  List<TranslationItemResult> results, int mappingVersion) {
        TranslationProviderType selectedType = resolveProvider(request.scene(), request.providerType());
        TranslationProvider provider = providers.stream()
                .filter(candidate -> candidate.type() == selectedType)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("translation provider is not configured: " + selectedType));
        try {
            List<String> targets = request.force() ? provider.retranslate(pending) : provider.translate(pending);
            List<List<String>> validationErrors = resultValidator.validate(pending, targets);
            for (int index = 0; index < pending.size(); index++) {
                TranslationItem item = pending.get(index);
                List<String> errors = index < validationErrors.size() ? validationErrors.get(index)
                        : List.of("missing_validation_result");
                String target = targets != null && index < targets.size() ? targets.get(index) : null;
                TranslationItemResult.Status status = errors.isEmpty()
                        ? TranslationItemResult.Status.SUCCESS : TranslationItemResult.Status.INVALID;
                pendingIndexes.get(contentKey(item)).forEach(resultIndex -> results.set(resultIndex,
                        result(request.items().get(resultIndex), target, status, source(selectedType), errors)));
                if (status == TranslationItemResult.Status.SUCCESS && !request.force()) {
                    cacheTranslation(cacheKey(request, mappingVersion, item), target);
                }
            }
        } catch (Exception e) {
            log.error("business translation provider failed, scene:{}, provider:{}, itemCount:{}",
                    request.scene(), selectedType, pending.size(), e);
            pending.forEach(item -> pendingIndexes.get(contentKey(item)).forEach(index -> results.set(index,
                    result(request.items().get(index), request.items().get(index).sourceText(),
                            TranslationItemResult.Status.FAILED, TranslationItemResult.Source.NONE,
                            List.of("provider_failed")))));
        }
    }

    /**
     * 解析场景默认 provider；明确指定模式时保留调用方要求。
     *
     * @param scene       业务场景
     * @param providerType 调用方 provider 模式
     * @return 实际 provider
     */
    private TranslationProviderType resolveProvider(TranslationScene scene, TranslationProviderType providerType) {
        if (providerType != TranslationProviderType.AUTO) {
            return providerType;
        }
        return switch (scene) {
            case CARD_TEXT -> TranslationProviderType.LLM;
            case CATEGORY_NAME, LINK_TEXT, GENERAL_TEXT -> TranslationProviderType.GOOGLE;
        };
    }

    /**
     * 构造与场景和来源文本相关的进程内缓存键。
     *
     * @param request 业务场景及 provider
     * @param mappingVersion 映射版本
     * @param item 字段类型及原文
     * @return 缓存键
     */
    private String cacheKey(TranslationBatchRequest request, int mappingVersion, TranslationItem item) {
        return request.scene().name() + ':' + resolveProvider(request.scene(), request.providerType()).name()
                + ':' + mappingVersion + ':' + CardTranslationTemplates.instruction(item.textType()).hashCode()
                + ':' + contentKey(item);
    }

    /** 同类型同原文才允许去重，防止名称与技能描述共享结果。 */
    private String contentKey(TranslationItem item) {
        return item.textType().name() + ':' + item.sourceText();
    }

    /**
     * 按容量上限写入统一翻译缓存。
     *
     * @param key    缓存键
     * @param target 译文
     */
    private void cacheTranslation(String key, String target) {
        if (translationCache.size() >= TranslationModuleConstants.CACHE_MAX_ENTRIES) {
            translationCache.clear();
        }
        translationCache.put(key, target);
    }

    /**
     * 将 provider 类型映射为可审计的结果来源。
     *
     * @param type provider 类型
     * @return 结果来源
     */
    private TranslationItemResult.Source source(TranslationProviderType type) {
        return type == TranslationProviderType.LLM ? TranslationItemResult.Source.LLM
                : TranslationItemResult.Source.GOOGLE;
    }

    /**
     * 创建单项结果。
     *
     * @param item   原始项
     * @param target 译文
     * @param status 处理状态
     * @param source 结果来源
     * @param errors 校验错误
     * @return 结果对象
     */
    private TranslationItemResult result(TranslationItem item, String target, TranslationItemResult.Status status,
                                         TranslationItemResult.Source source, List<String> errors) {
        return new TranslationItemResult(item == null ? null : item.key(),
                item == null ? null : item.sourceText(), target, status, source, errors);
    }

}
