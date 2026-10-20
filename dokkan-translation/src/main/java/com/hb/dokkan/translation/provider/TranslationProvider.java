package com.hb.dokkan.translation.provider;

import com.hb.dokkan.translation.domain.TranslationItem;
import com.hb.dokkan.translation.domain.TranslationProviderType;

import java.util.List;

/** Provider 协议适配器，封装单一外部翻译服务。 */
public interface TranslationProvider {

    /**
     * 返回当前适配器对应的 provider 类型。
     *
     * @return provider 类型
     */
    TranslationProviderType type();

    /**
     * 批量翻译输入文本，结果顺序必须与入参一致。
     *
     * @param items 待翻译文本
     * @return 对应顺序的译文
     */
    List<String> translate(List<TranslationItem> items);

    /**
     * 按 provider 支持的方式强制重新翻译，默认退回普通调用。
     *
     * @param items 待翻译文本
     * @return 对应顺序的译文
     */
    default List<String> retranslate(List<TranslationItem> items) {
        return translate(items);
    }

    /** 刷新 provider 内部缓存的领域术语。 */
    default void refreshGlossary() {
    }
}
