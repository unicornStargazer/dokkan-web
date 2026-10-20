package com.hb.dokkan.service.convert;

import com.hb.dokkan.common.domain.dto.data.dokkandb.DokkanDbCardDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiCardDTO;
import com.hb.dokkan.translation.BusinessTranslationService;
import com.hb.dokkan.translation.domain.TranslationBatchRequest;
import com.hb.dokkan.translation.domain.TranslationItemResult;
import com.hb.dokkan.translation.domain.CardTextType;
import org.junit.Test;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** DokkanDB assembler 跨卡文本批处理测试。 */
public class DokkanDbCardAssemblerTest {

    /** 同批同名卡片进入统一翻译请求后仍能正确恢复各自字段。 */
    @Test
    public void assembleAllShouldBatchAndRestoreRepeatedCardNames() {
        DokkanDbCardAssembler assembler = new DokkanDbCardAssembler();
        CapturingTranslationService translationService = new CapturingTranslationService();
        setField(assembler, "translationService", translationService);
        DokkanDbCardDTO first = card(1001L, "Cooler");
        DokkanDbCardDTO second = card(1002L, "Cooler");

        List<WikiCardDTO> result = assembler.assembleAll(List.of(first, second),
                java.util.Arrays.asList(null, null), false);

        assertEquals(2, result.size());
        assertEquals("Cooler-zh", result.get(0).getCard().getName());
        assertEquals("Cooler-zh", result.get(1).getCard().getName());
        assertEquals(1, translationService.requestCount);
        assertEquals(4, translationService.request.items().size());
    }

    /** 相同原文在不同技能字段使用各自模板，乱序响应仍能按关联键正确回填。 */
    @Test
    public void skillFieldsShouldRetainTypeForNormalEzaAndTransformationData() {
        DokkanDbCardAssembler assembler = new DokkanDbCardAssembler();
        CapturingTranslationService service = new CapturingTranslationService();
        service.typed = true;
        setField(assembler, "translationService", service);
        DokkanDbCardDTO card = card(1001L, "Shared");
        card.setLeaderSkill("Shared");
        card.setPassiveSkillName("Shared");
        card.setPassiveSkillDescription("Shared");
        card.setActiveSkillEffect("Shared");
        card.setActiveSkillCondition("Shared");
        card.setSpecialIds(List.of(10L));
        card.setSpecialNames(List.of("Shared"));
        card.setSpecialDescriptions(List.of("Shared"));
        card.setStep(8);
        card.setEzaLeaderSkill("Shared");
        card.setEzaPassiveSkillDescription("Shared");
        card.setTransformationIds(List.of(1002L));
        card.setTransformationNames(List.of("Shared"));
        WikiCardDTO result = assembler.assemble(card, null, true);

        assertEquals("CHARACTER_NAME", result.getCard().getName());
        assertEquals("LEADER_SKILL", result.getCard().getLeaderSkill());
        assertEquals("PASSIVE_SKILL", result.getCard().getPassiveSkillDesc());
        assertEquals("SKILL_NAME", result.getCard().getPassiveSkillName());
        assertEquals("ACTIVE_EFFECT", result.getCard().getActiveSkillEffect());
        assertEquals("ACTIVE_CONDITION", result.getCard().getActiveSkillCondition());
        assertEquals("SUPER_ATTACK", result.getSpecials().getFirst().getDescription());
        assertEquals("SKILL_NAME", result.getSpecials().getFirst().getName());
        assertEquals("PASSIVE_SKILL", result.getEzaCardInfos().getFirst().getPassiveSkillDesc());
        assertEquals("LEADER_SKILL", result.getEzaCardInfos().getFirst().getLeaderSkill());
        assertEquals("CHARACTER_NAME", result.getTransformations().getFirst().getNextCard().getName());
        assertTrue(service.request.force());
        assertTrue(service.request.items().stream().anyMatch(item -> item.textType() == CardTextType.SUPER_ATTACK));
    }

    /** 测试用统一翻译服务，保留请求以验证批量关联键。 */
    private static class CapturingTranslationService extends BusinessTranslationService {
        private int requestCount;
        private TranslationBatchRequest request;
        private boolean typed;

        /** {@inheritDoc} */
        @Override
        public List<TranslationItemResult> translate(TranslationBatchRequest request) {
            requestCount++;
            this.request = request;
            List<TranslationItemResult> results = new ArrayList<>(request.items().stream()
                    .map(item -> new TranslationItemResult(item.key(), item.sourceText(),
                            typed ? item.textType().name() : item.sourceText() + "-zh",
                            TranslationItemResult.Status.SUCCESS, TranslationItemResult.Source.LLM, List.of()))
                    .toList());
            Collections.reverse(results);
            return results;
        }
    }

    /** 构造用于翻译关联验证的最小卡片 DTO。 */
    private DokkanDbCardDTO card(Long id, String name) {
        DokkanDbCardDTO card = new DokkanDbCardDTO();
        card.setId(id);
        card.setName(name);
        card.setTitle("Title " + id);
        return card;
    }

    /** 注入测试 double，避免测试引入 Spring 容器。 */
    private void setField(Object target, String name, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("failed to set test field: " + name, e);
        }
    }
}
