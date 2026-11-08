package com.hb.dokkan.service.convert;

import com.hb.dokkan.common.domain.po.mysql.cards.*;
import com.hb.dokkan.common.domain.po.es.cards.*;
import com.hb.dokkan.common.constants.CardSyncConstants;
import com.hb.dokkan.common.utils.JsonUtils;
import org.mapstruct.*;
import java.util.*;

/**
 * 新卡表到兼容 ES 文档的字段转换。
 **/
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true), unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface KxWikiEsConvert {
    /**
     * 主表字段映射，ES 主键按业务卡片 ID 生成。
     **/
    @Mapping(target = "id", expression = "java(String.valueOf(card.getCardId()))")
    @Mapping(target = "superEzaFlag", source = "sezaFlag")
    @Mapping(target = "attributes", ignore = true)
    CardEsPO cardToEs(CardPO card);

    /**
     * 技能映射为 ES 必杀嵌套文档，技能名及效果保留旧 description 展示语义。
     **/
    @Mapping(target = "specialId", source = "skillId")
    @Mapping(target = "description", expression = "java(specialDescription(skill))")
    @Mapping(target = "eballNumStart", source = "ki")
    @Mapping(target = "specialCategoryId", source = "attackCategoryId")
    @Mapping(target = "specialCategoryName", source = "attackCategoryName")
    CardEsSpecialDTO skillToSpecial(CardSkillPO skill);

    /**
     * 恢复旧 ES 必杀解锁等级及前两个满级加成字段。
     **/
    @AfterMapping
    default void appendSuperAttackAttributes(CardSkillPO skill, @MappingTarget CardEsSpecialDTO special) {
        Map<String, Object> attributes = JsonUtils.json2Map(skill.getAttributes(), String.class, Object.class);
        Number level = (Number) attributes.get(CardSyncConstants.SKILL_SA_LEVEL_FIELD);
        special.setLvStart(level == null ? null : level.intValue());
        Object bonuses = attributes.get(CardSyncConstants.SKILL_BONUSES_FIELD);
        if (!(bonuses instanceof List<?> values)) {
            return;
        }
        if (!values.isEmpty() && values.getFirst() instanceof Map<?, ?> bonus) {
            special.setSpecialBonus1(Objects.toString(bonus.get(CardSyncConstants.SKILL_BONUS_NAME_FIELD), null));
            Number bonusLevel = (Number) bonus.get(CardSyncConstants.SKILL_BONUS_LEVEL_FIELD);
            special.setSpecialBonus1Lv(bonusLevel == null ? null : bonusLevel.intValue());
        }
        if (values.size() > 1 && values.get(1) instanceof Map<?, ?> bonus) {
            special.setSpecialBonus2(Objects.toString(bonus.get(CardSyncConstants.SKILL_BONUS_NAME_FIELD), null));
            Number bonusLevel = (Number) bonus.get(CardSyncConstants.SKILL_BONUS_LEVEL_FIELD);
            special.setSpecialBonus2Lv(bonusLevel == null ? null : bonusLevel.intValue());
        }
    }

    /**
     * 预备/终结技能映射为兼容嵌套 DTO。
     **/
    @Mapping(target = "conditionDescription", source = "conditionDesc")
    @Mapping(target = "effectDescription", source = "effectDesc")
    @Mapping(target = "label", source = "skillType")
    @Mapping(target = "specialCategoryId", source = "attackCategoryId")
    @Mapping(target = "specialCategoryName", source = "attackCategoryName")
    CardEsSkillDTO skillToEsSkill(CardSkillPO skill);

    /**
     * 拼接必杀展示描述，维持旧页面从 description 展示名称及效果的约定。
     **/
    default String specialDescription(CardSkillPO skill) {
        return skill.getName() + CardSyncConstants.SKILL_DESCRIPTION_SEPARATOR
                + Objects.toString(skill.getEffectDesc(), CardSyncConstants.EMPTY_TEXT);
    }

    /**
     * 潜力 JSON 转换为旧 ES 列表结构，比例映射到 order。
     **/
    default List<CardEsPotentialDTO> potentials(String json) {
        List<Map<String, Object>> rows = JsonUtils.json2ListMap(json, String.class, Object.class);
        List<CardEsPotentialDTO> values = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            CardEsPotentialDTO value = new CardEsPotentialDTO();
            value.setOrder(((Number) row.get(CardSyncConstants.POTENTIAL_PERCENTAGE_FIELD)).intValue());
            value.setHp((Number) row.get(CardSyncConstants.POTENTIAL_HP_FIELD));
            value.setAtk((Number) row.get(CardSyncConstants.POTENTIAL_ATK_FIELD));
            value.setDef((Number) row.get(CardSyncConstants.POTENTIAL_DEF_FIELD));
            values.add(value);
        }
        return values;
    }
}
