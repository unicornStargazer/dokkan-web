package com.hb.dokkan.service.convert;

import com.hb.dokkan.common.domain.po.es.cards.CardEsSpecialDTO;
import com.hb.dokkan.common.domain.po.es.cards.CardEsPO;
import com.hb.dokkan.common.domain.po.mysql.cards.CardStatePO;
import com.hb.dokkan.common.domain.po.mysql.cards.CardSkillPO;
import com.hb.dokkan.common.enums.CardStateEnum;
import com.hb.dokkan.common.enums.CardSkillTypeEnum;
import com.hb.dokkan.common.utils.JsonUtils;
import org.junit.Test;
import org.mapstruct.factory.Mappers;
import java.util.List;

import static org.junit.Assert.*;

/**
 * 验证新技能表映射至旧 ES/详情响应结构时不丢失展示字段。
 **/
public class KxWikiEsConvertTest {
    /**
     * 三个觉醒阶段的数值和技能必须独立，乱序输入不能改变 Tab 顺序。
     **/
    @Test
    public void shouldKeepEveryAwakeningStateIndependentThroughDetailResponse() {
        var convert = Mappers.getMapper(KxWikiEsConvert.class);
        var base = state(CardStateEnum.BASE, 100L);
        var eza = state(CardStateEnum.EZA, 200L);
        var seza = state(CardStateEnum.SEZA, 300L);
        CardEsPO document = new CardEsPO();
        document.setStates(convert.statesToEs(List.of(seza, base, eza), List.of(
                skill(CardStateEnum.EZA, CardSkillTypeEnum.SUPER, 2),
                skill(CardStateEnum.BASE, CardSkillTypeEnum.SUPER, 0),
                skill(CardStateEnum.SEZA, CardSkillTypeEnum.SUPER, 0),
                skill(CardStateEnum.EZA, CardSkillTypeEnum.SUPER, 1),
                skill(CardStateEnum.EZA, CardSkillTypeEnum.ACTIVE, 0),
                skill(CardStateEnum.SEZA, CardSkillTypeEnum.STANDBY, 0),
                skill(CardStateEnum.SEZA, CardSkillTypeEnum.FINISH, 0))));
        // 覆盖 ES 文档序列化与 PO -> VO -> Response 的完整转换。
        CardEsPO restored = JsonUtils.json2Object(JsonUtils.object2Json(document), CardEsPO.class);
        var cardConvert = Mappers.getMapper(DokkanCardConvert.class);
        var response = cardConvert.detailVoToResponse(cardConvert.convertToCardDetailVO(restored));
        assertEquals(List.of("base", "eza", "seza"), response.getStates().stream()
                .map(value -> value.getStateKey()).toList());
        var baseDetail = response.getStates().get(0);
        var ezaDetail = response.getStates().get(1);
        var sezaDetail = response.getStates().get(2);
        assertEquals(Long.valueOf(100L), baseDetail.getHpValue());
        assertEquals(Long.valueOf(200L), ezaDetail.getHpValue());
        assertEquals(Long.valueOf(300L), sezaDetail.getHpValue());
        assertEquals("eza队长", ezaDetail.getLeaderSkill());
        assertEquals("seza队长", sezaDetail.getLeaderSkill());
        assertEquals("base被动", baseDetail.getPassiveSkill());
        assertEquals("eza被动", ezaDetail.getPassiveSkill());
        assertEquals("seza被动", sezaDetail.getPassiveSkill());
        assertEquals(1, baseDetail.getSpecialSkills().size());
        assertEquals(2, ezaDetail.getSpecialSkills().size());
        assertTrue(ezaDetail.getSpecialSkills().getFirst().getDescription().startsWith("eza-super-1"));
        assertEquals(1, sezaDetail.getSpecialSkills().size());
        assertTrue(baseDetail.getActiveSkills().isEmpty());
        assertEquals("eza-active-0", ezaDetail.getActiveSkills().getFirst().getName());
        assertEquals("eza条件", ezaDetail.getActiveSkills().getFirst().getConditionDescription());
        assertTrue(ezaDetail.getStandBySkills().isEmpty());
        assertEquals("seza-standby-0", sezaDetail.getStandBySkills().getFirst().getName());
        assertEquals("seza-finish-0", sezaDetail.getFinishSkills().getFirst().getName());
    }

    /**
     * 普通卡只返回基础阶段，不生成不存在的极限 Tab 或技能。
     **/
    @Test
    public void shouldKeepBaseOnlyCardWithoutInventedAwakeningStates() {
        var convert = Mappers.getMapper(KxWikiEsConvert.class);
        var states = convert.statesToEs(List.of(state(CardStateEnum.BASE, 100L)), List.of());
        assertEquals(1, states.size());
        assertEquals(CardStateEnum.BASE.getKey(), states.getFirst().getStateKey());
        assertTrue(states.getFirst().getSpecialSkills().isEmpty());
        assertTrue(states.getFirst().getActiveSkills().isEmpty());
        assertTrue(states.getFirst().getPassiveDomains().isEmpty());
        assertTrue(convert.statesToEs(List.of(), List.of()).isEmpty());
    }

    /**
     * 领域仅从所属阶段与技能恢复，模板优先，空值和旧记录兼容。
     **/
    @Test
    public void domainsShouldKeepStateIsolationAndPreferTemplate() {
        var convert = Mappers.getMapper(KxWikiEsConvert.class);
        var base = state(CardStateEnum.BASE, 100L);
        var eza = state(CardStateEnum.EZA, 200L);
        base.setAttributes("{\"domains\":[{\"id\":1,\"name\":\"领域\",\"description\":\"普通文本\",\"description_template\":\"模板{passiveImg:up_g}\",\"needed\":true}]}");
        eza.setAttributes("{\"domains\":null}");
        var states = convert.statesToEs(List.of(base, eza), List.of());
        assertEquals(1, states.getFirst().getPassiveDomains().size());
        assertEquals("模板{passiveImg:up_g}", states.getFirst().getPassiveDomains().getFirst().getDescription());
        assertTrue(states.get(1).getPassiveDomains().isEmpty());
        assertTrue(convert.storedDomains(null).isEmpty());
        assertTrue(convert.storedDomains("{}").isEmpty());
        var skill = skill(CardStateEnum.BASE, CardSkillTypeEnum.SUPER, 0);
        skill.setAttributes(base.getAttributes());
        assertEquals(Long.valueOf(1L), convert.skillToSpecial(skill).getDomains().getFirst().getId());
        assertEquals(Long.valueOf(1L), convert.skillToEsSkill(skill).getDomains().getFirst().getId());
    }

    /**
     * 为阶段隔离用例构造具有不同属性和技能描述的状态。
     **/
    private CardStatePO state(CardStateEnum key, long hp) {
        var state = new CardStatePO();
        state.setStateKey(key.getKey());
        state.setHpValue(hp);
        state.setAtkValue(hp + 1);
        state.setDefValue(hp + 2);
        state.setLeaderSkillDesc(key.getKey() + "队长");
        state.setPassiveSkillDesc(key.getKey() + "被动");
        return state;
    }

    /**
     * 构造可区分状态、类型及排序的技能，检查交叉污染和顺序。
     **/
    private CardSkillPO skill(CardStateEnum key, CardSkillTypeEnum type, int sort) {
        var skill = new CardSkillPO();
        skill.setStateKey(key.getKey());
        skill.setSkillType(type.getType());
        skill.setSort(sort);
        skill.setName(key.getKey() + "-" + type.getType() + "-" + sort);
        skill.setEffectDesc(key.getKey() + "效果");
        skill.setConditionDesc(key.getKey() + "条件");
        skill.setAttributes("{}");
        return skill;
    }
    /**
     * 必杀等级、加成等级和攻击分类通过兼容 DTO 原样传到 API 视图。
     **/
    @Test
    public void shouldPreserveSuperAttackFieldsThroughApiMapping() {
        CardSkillPO skill = new CardSkillPO();
        skill.setSkillId(5546L);
        skill.setName("必杀");
        skill.setEffectDesc("效果");
        skill.setKi(12);
        skill.setIncreaseRate(150);
        skill.setAttackCategoryId(1);
        skill.setAttributes("{\"saLevel\":1,\"bonuses\":[{\"name\":\"威力提升30%\",\"super_attack_level\":20}]}");
        CardEsSpecialDTO special = Mappers.getMapper(KxWikiEsConvert.class).skillToSpecial(skill);
        assertEquals(Integer.valueOf(1), special.getLvStart());
        assertEquals(Integer.valueOf(20), special.getSpecialBonus1Lv());
        assertEquals("威力提升30%", special.getSpecialBonus1());
        assertEquals(Long.valueOf(1), special.getSpecialCategoryId());
        assertEquals(Integer.valueOf(12), special.getEballNumStart());
        var document = new com.hb.dokkan.common.domain.po.es.cards.CardEsPO();
        document.setSpecialSkills(java.util.List.of(special));
        var response = Mappers.getMapper(DokkanCardConvert.class).convertToCardDetailVO(document);
        assertEquals(special.getDescription(), response.getSpecialSkills().getFirst().getDescription());
        assertEquals(special.getSpecialBonus1Lv(), response.getSpecialSkills().getFirst().getSpecialBonus1Lv());
    }
}
