package com.hb.dokkan.translation.domain;

import java.util.List;

/**
 * 场景化批量翻译请求。
 *
 * @param scene        业务翻译场景
 * @param providerType provider 选择模式
 * @param items        待翻译文本及关联键
 * @param force        是否绕过缓存和精确映射
 */
public record TranslationBatchRequest(TranslationScene scene, TranslationProviderType providerType,
                                      List<TranslationItem> items, boolean force) {
}
