package com.hb.dokkan.translation.domain;

import java.util.List;

/**
 * 单条翻译处理结果。
 *
 * @param key              输入关联键
 * @param sourceText       原文
 * @param translatedText   候选译文
 * @param status           翻译处理状态
 * @param source           结果来源
 * @param validationErrors 校验失败原因
 */
public record TranslationItemResult(String key, String sourceText, String translatedText,
                                    Status status, Source source, List<String> validationErrors) {

    /** 翻译处理状态。 */
    public enum Status {
        SUCCESS,
        FAILED,
        INVALID
    }

    /** 翻译结果来源。 */
    public enum Source {
        MAPPING,
        CACHE,
        GOOGLE,
        LLM,
        NONE
    }
}
