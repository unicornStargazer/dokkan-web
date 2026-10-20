package com.hb.dokkan.translation;

import com.hb.dokkan.config.translation.LlmTranslationProperties;
import com.hb.dokkan.translation.domain.CardTextType;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/** 验证业务模板确实进入模型提示词，且模板改变会隔离 LLM 缓存。 */
public class CardTranslationTemplatesTest {

    /** 同一模型调用入口随字段类型选择模板，并保留批量输出协议。 */
    @Test
    public void promptShouldContainSelectedTemplateAndHaveDistinctCachePrefix() throws Exception {
        DokkanLlmTranslationService service = new DokkanLlmTranslationService();
        setField(service, "properties", new LlmTranslationProperties());
        setField(service, "translationMappingService", new TranslationMappingService() {
            /** 测试仅关心模板，不读取数据库映射。 */
            @Override
            public Map<String, String> listEnabledMappingMap() {
                return Map.of();
            }
        });
        setField(service, "runtimeGlossaryLoaded", true);
        setField(service, "runtimeGlossarySourceHash", Map.of().hashCode());
        Method buildPrompt = DokkanLlmTranslationService.class.getDeclaredMethod(
                "buildBatchSystemPromptWithGlossary", CardTextType.class);
        buildPrompt.setAccessible(true);
        String leader = (String) buildPrompt.invoke(service, CardTextType.LEADER_SKILL);
        String passive = (String) buildPrompt.invoke(service, CardTextType.PASSIVE_SKILL);
        String special = (String) buildPrompt.invoke(service, CardTextType.SUPER_ATTACK);
        String name = (String) buildPrompt.invoke(service, CardTextType.SKILL_NAME);

        assertTrue(leader.contains("队长技模板"));
        assertTrue(passive.contains("受到攻击后"));
        assertTrue(special.contains("mega-colossal damage=超极大伤害"));
        assertFalse(name.contains("必杀技模板"));
        assertTrue(leader.contains(com.hb.dokkan.common.constants.TranslationConstants.LLM_BATCH_PROMPT_INSTRUCTION));
        Method cachePrefix = DokkanLlmTranslationService.class.getDeclaredMethod("buildCacheKeyPrefix", String.class);
        cachePrefix.setAccessible(true);
        assertNotEquals(cachePrefix.invoke(service, leader), cachePrefix.invoke(service, passive));
    }

    /** 注入无容器单元测试所需的最小配置。 */
    private void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
