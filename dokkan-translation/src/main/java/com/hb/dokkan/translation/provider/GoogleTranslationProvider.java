package com.hb.dokkan.translation.provider;

import com.hb.dokkan.translation.DokkanTranslationService;
import com.hb.dokkan.translation.domain.TranslationItem;
import com.hb.dokkan.translation.domain.TranslationProviderType;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/** 将现有 Google 翻译链路适配到统一 provider 契约。 */
@Component
public class GoogleTranslationProvider implements TranslationProvider {

    @Resource
    private DokkanTranslationService translationService;

    /** {@inheritDoc} */
    @Override
    public TranslationProviderType type() {
        return TranslationProviderType.GOOGLE;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> translate(List<TranslationItem> items) {
        return translationService.translateAll(items.stream().map(TranslationItem::sourceText).toList());
    }

    /** {@inheritDoc} */
    @Override
    public List<String> retranslate(List<TranslationItem> items) {
        return translationService.retranslateAll(items.stream().map(TranslationItem::sourceText).toList());
    }

    /** {@inheritDoc} */
    @Override
    public void refreshGlossary() {
        translationService.refreshCategoryDomainGlossary();
    }
}
