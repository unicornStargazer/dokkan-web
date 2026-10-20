package com.hb.dokkan.translation;

import com.hb.dokkan.translation.domain.TranslationBatchRequest;
import com.hb.dokkan.translation.domain.TranslationItem;
import com.hb.dokkan.translation.domain.TranslationItemResult;
import com.hb.dokkan.translation.domain.TranslationProviderType;
import com.hb.dokkan.translation.domain.TranslationScene;
import com.hb.dokkan.translation.domain.CardTextType;
import com.hb.dokkan.translation.provider.TranslationProvider;
import org.junit.Before;
import org.junit.Test;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** 统一翻译入口的逐项回填及映射优先级测试。 */
public class BusinessTranslationServiceTest {

    private BusinessTranslationService service;
    private StubProvider provider;

    /** 初始化无 Spring 容器的翻译服务测试夹具。 */
    @Before
    public void setUp() {
        service = new BusinessTranslationService();
        provider = new StubProvider();
        TranslationMappingService mappingService = new TranslationMappingService() {
            /** 返回隔离测试用的空 mapping 快照。 */
            @Override
            public Map<String, String> listEnabledMappingMap() {
                return Map.of();
            }
        };
        setField("mappingService", mappingService);
        setField("providers", List.of(provider));
        setField("resultValidator", new TranslationResultValidator());
    }

    /** 注入测试依赖，避免测试模块额外依赖 Spring Test。 */
    private void setField(String name, Object value) {
        try {
            java.lang.reflect.Field field = BusinessTranslationService.class.getDeclaredField(name);
            field.setAccessible(true);
            field.set(service, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("failed to set test field: " + name, e);
        }
    }

    /** 同批重复原文只送 provider 一次，结果按各自输入关联键回填。 */
    @Test
    public void duplicateSourcesShouldTranslateOnceAndRestoreEveryItem() {
        List<TranslationItemResult> results = service.translate(new TranslationBatchRequest(
                TranslationScene.CARD_TEXT, TranslationProviderType.LLM,
                List.of(new TranslationItem("card-1:name", "Cooler"),
                        new TranslationItem("card-2:name", "Cooler")), false));

        assertEquals(1, provider.callCount);
        assertEquals("card-1:name", results.get(0).key());
        assertEquals("card-2:name", results.get(1).key());
        assertEquals("古拉", results.get(0).translatedText());
        assertEquals("古拉", results.get(1).translatedText());
    }

    /** Provider 失败必须显式失败，不能把原文伪装为成功译文。 */
    @Test
    public void providerFailureShouldReturnFailedStatus() {
        provider.fail = true;
        List<TranslationItemResult> results = service.translate(new TranslationBatchRequest(
                TranslationScene.CARD_TEXT, TranslationProviderType.LLM,
                List.of(new TranslationItem("card:name", "Cooler")), false));

        assertEquals(TranslationItemResult.Status.FAILED, results.getFirst().status());
        assertTrue(results.getFirst().validationErrors().contains("provider_failed"));
    }

    /** 相同原文跨类型分别翻译，同类型仍去重且缓存不能串用。 */
    @Test
    public void skillTypesShouldIsolateDeduplicationAndCache() {
        provider.typed = true;
        List<TranslationItem> items = List.of(
                new TranslationItem("leader", "Shared", CardTextType.LEADER_SKILL),
                new TranslationItem("passive", "Shared", CardTextType.PASSIVE_SKILL),
                new TranslationItem("passive-copy", "Shared", CardTextType.PASSIVE_SKILL));
        TranslationBatchRequest request = new TranslationBatchRequest(
                TranslationScene.CARD_TEXT, TranslationProviderType.LLM, items, false);
        List<TranslationItemResult> first = service.translate(request);
        assertEquals(2, provider.itemCount);
        assertEquals("LEADER_SKILL", first.get(0).translatedText());
        assertEquals("PASSIVE_SKILL", first.get(1).translatedText());
        assertEquals(first.get(1).translatedText(), first.get(2).translatedText());
        List<TranslationItemResult> cached = service.translate(request);
        assertEquals(1, provider.callCount);
        assertEquals(TranslationItemResult.Source.CACHE, cached.get(0).source());
        assertEquals("PASSIVE_SKILL", cached.get(1).translatedText());
        service.translate(new TranslationBatchRequest(
                TranslationScene.CARD_TEXT, TranslationProviderType.LLM, items, true));
        assertEquals(2, provider.callCount);
    }

    /** 测试用 Provider，按原文顺序生成固定结果。 */
    private static class StubProvider implements TranslationProvider {
        private int callCount;
        private boolean fail;
        private boolean typed;
        private int itemCount;

        /** {@inheritDoc} */
        @Override
        public TranslationProviderType type() {
            return TranslationProviderType.LLM;
        }

        /** {@inheritDoc} */
        @Override
        public List<String> translate(List<TranslationItem> items) {
            callCount++;
            itemCount = items.size();
            if (fail) {
                throw new IllegalStateException("provider unavailable");
            }
            return items.stream().map(item -> typed ? item.textType().name() : "古拉").toList();
        }
    }
}
