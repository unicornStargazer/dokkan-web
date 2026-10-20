package com.hb.dokkan.translation.domain;

/**
 * 带业务关联键的单条翻译文本。
 *
 * @param key        调用方用于恢复结果的稳定关联键
 * @param sourceText 原始文本
 * @param textType 卡片字段类型，用于选择模板和隔离缓存
 */
public record TranslationItem(String key, String sourceText, CardTextType textType) {

    /** 兼容未指定卡片字段类型的原有调用方。 */
    public TranslationItem(String key, String sourceText) {
        this(key, sourceText, CardTextType.GENERAL);
    }

    /** 未提供类型时使用通用模板。 */
    public TranslationItem {
        textType = textType == null ? CardTextType.GENERAL : textType;
    }
}
