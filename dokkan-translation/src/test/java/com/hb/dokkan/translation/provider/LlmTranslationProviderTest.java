package com.hb.dokkan.translation.provider;

import com.hb.dokkan.translation.DokkanLlmTranslationService;
import com.hb.dokkan.translation.domain.CardTextType;
import com.hb.dokkan.translation.domain.TranslationItem;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/** 验证混合字段按模板拆批后仍保持原顺序及强制重翻语义。 */
public class LlmTranslationProviderTest {

    /** 交错的同类型文本合批，译文按原始位置恢复。 */
    @Test
    public void typedBatchesShouldRestoreOrderAndPassForceFlag() throws Exception {
        LlmTranslationProvider provider = new LlmTranslationProvider();
        CapturingLlmService service = new CapturingLlmService();
        java.lang.reflect.Field field = LlmTranslationProvider.class.getDeclaredField("translationService");
        field.setAccessible(true);
        field.set(provider, service);
        List<TranslationItem> items = List.of(
                new TranslationItem("passive-1", "same", CardTextType.PASSIVE_SKILL),
                new TranslationItem("leader", "same", CardTextType.LEADER_SKILL),
                new TranslationItem("passive-2", "other", CardTextType.PASSIVE_SKILL));

        assertEquals(List.of("PASSIVE_SKILL:same", "LEADER_SKILL:same", "PASSIVE_SKILL:other"),
                provider.translate(items));
        assertEquals(List.of(2, 1), service.batchSizes);
        provider.retranslate(items);
        assertFalse(service.useCache);
    }

    /** 替代外部模型调用并记录每批类型和缓存模式。 */
    private static class CapturingLlmService extends DokkanLlmTranslationService {
        private final List<Integer> batchSizes = new ArrayList<>();
        private boolean useCache;

        /** 返回包含字段类型的译文，检验类型不会在适配时丢失。 */
        @Override
        public List<String> translateAll(Collection<String> texts, CardTextType type, boolean useCache) {
            this.useCache = useCache;
            batchSizes.add(texts.size());
            return texts.stream().map(text -> type.name() + ':' + text).toList();
        }
    }
}
