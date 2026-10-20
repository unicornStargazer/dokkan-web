package com.hb.dokkan.translation.provider;

import com.hb.dokkan.translation.DokkanLlmTranslationService;
import com.hb.dokkan.translation.domain.TranslationItem;
import com.hb.dokkan.translation.domain.TranslationProviderType;
import com.hb.dokkan.translation.domain.CardTextType;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** 将现有 LLM 翻译及额度记账链路适配到统一 provider 契约。 */
@Component
public class LlmTranslationProvider implements TranslationProvider {

    @Resource
    private DokkanLlmTranslationService translationService;

    /** {@inheritDoc} */
    @Override
    public TranslationProviderType type() {
        return TranslationProviderType.LLM;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> translate(List<TranslationItem> items) {
        return translateByType(items, true);
    }

    /** {@inheritDoc} */
    @Override
    public List<String> retranslate(List<TranslationItem> items) {
        return translateByType(items, false);
    }

    /** 同类字段跨卡批量翻译，按原位置回填，避免将其他技能的模板应用到当前字段。 */
    private List<String> translateByType(List<TranslationItem> items, boolean useCache) {
        Map<CardTextType, List<Integer>> indexesByType = new LinkedHashMap<>();
        for (int index = 0; index < items.size(); index++) {
            indexesByType.computeIfAbsent(items.get(index).textType(), ignored -> new ArrayList<>()).add(index);
        }
        List<String> results = new ArrayList<>(Collections.nCopies(items.size(), null));
        indexesByType.forEach((type, indexes) -> {
            List<String> sources = indexes.stream().map(index -> items.get(index).sourceText()).toList();
            List<String> targets = translationService.translateAll(sources, type, useCache);
            if (targets == null || targets.size() != indexes.size()) {
                throw new IllegalStateException("typed translation response count mismatch: " + type);
            }
            for (int index = 0; index < indexes.size(); index++) {
                results.set(indexes.get(index), targets.get(index));
            }
        });
        return results;
    }
}
