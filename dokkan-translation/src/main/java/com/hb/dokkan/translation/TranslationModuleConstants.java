package com.hb.dokkan.translation;

/** 翻译业务模块的容量与缓存规则。 */
public final class TranslationModuleConstants {

    /** 统一进程内翻译缓存的最大条目数，达到上限后整体清空。 */
    public static final int CACHE_MAX_ENTRIES = 20_000;

    private TranslationModuleConstants() {
    }
}
