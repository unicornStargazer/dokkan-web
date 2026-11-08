package com.hb.dokkan.service.convert;

import com.hb.dokkan.common.domain.po.mysql.cards.*;
import com.hb.dokkan.common.domain.po.es.cards.*;
import com.hb.dokkan.common.constants.CardSyncConstants;
import com.hb.dokkan.common.domain.dto.data.kx.KxWikiDomainDTO;
import com.hb.dokkan.common.enums.CardSkillTypeEnum;
import com.hb.dokkan.common.enums.CardStateEnum;
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
     * 状态表字段映射为独立详情，不使用旧兼容字段的最高阶段数值。
     *
     * @param state 当前觉醒状态的持久化记录
     * @return 当前状态的属性和技能描述，技能列表由调用方补充
     **/
    @Mapping(target = "leaderSkill", source = "leaderSkillDesc")
    @Mapping(target = "passiveSkill", source = "passiveSkillDesc")
    CardEsStateDTO stateToEs(CardStatePO state);

    /**
     * 恢复当前状态被动关联的领域，旧记录缺失扩展属性时返回空列表。
     **/
    @AfterMapping
    default void appendPassiveDomains(CardStatePO state, @MappingTarget CardEsStateDTO detail) {
        detail.setPassiveDomains(storedDomains(state.getAttributes()));
    }

    /**
     * 源站领域转换为 ES 展示字段，描述优先使用模板。
     **/
    @Mapping(target = "description", expression = "java(org.apache.commons.lang3.StringUtils.defaultIfBlank(domain.getDescriptionTemplate(), domain.getDescription()))")
    CardEsDomainDTO domainToEs(KxWikiDomainDTO domain);

    /**
     * 从状态或技能扩展属性恢复领域，保留同一领域不同的展开和依赖关系。
     *
     * @param attributes 数据库存储的扩展属性 JSON，允许为空
     * @return 领域列表；旧数据不含领域时为空列表
     **/
    default List<CardEsDomainDTO> storedDomains(String attributes) {
        Map<String, Object> values = JsonUtils.json2Map(attributes, String.class, Object.class);
        return JsonUtils.convertList(values.get(CardSyncConstants.SKILL_DOMAINS_FIELD), KxWikiDomainDTO.class)
                .stream().map(this::domainToEs).toList();
    }

    /**
     * 按觉醒状态和技能类型分组，避免极限前后的技能混合展示。
     *
     * @param states 同一卡片的状态列表
     * @param skills 同一卡片的技能列表
     * @return 按 base、eza、seza 排列的完整详情
     **/
    default List<CardEsStateDTO> statesToEs(List<CardStatePO> states, List<CardSkillPO> skills) {
        Map<String, List<CardSkillPO>> skillsByState = skills.stream()
                .collect(java.util.stream.Collectors.groupingBy(CardSkillPO::getStateKey));
        List<CardEsStateDTO> details = new ArrayList<>();
        // 使用统一状态枚举决定 Tab 顺序，所有技能严格归属当前状态。
        for (CardStateEnum key : CardStateEnum.values()) {
            states.stream().filter(state -> key.getKey().equals(state.getStateKey())).findFirst().ifPresent(state -> {
                CardEsStateDTO detail = stateToEs(state);
                Map<String, List<CardSkillPO>> byType = skillsByState.getOrDefault(key.getKey(), List.of()).stream()
                        .sorted(Comparator.comparing(CardSkillPO::getSort))
                        .collect(java.util.stream.Collectors.groupingBy(CardSkillPO::getSkillType));
                detail.setSpecialSkills(byType.getOrDefault(CardSkillTypeEnum.SUPER.getType(), List.of())
                        .stream().map(this::skillToSpecial).toList());
                detail.setActiveSkills(byType.getOrDefault(CardSkillTypeEnum.ACTIVE.getType(), List.of())
                        .stream().map(this::skillToEsSkill).toList());
                detail.setStandBySkills(byType.getOrDefault(CardSkillTypeEnum.STANDBY.getType(), List.of())
                        .stream().map(this::skillToEsSkill).toList());
                detail.setFinishSkills(byType.getOrDefault(CardSkillTypeEnum.FINISH.getType(), List.of())
                        .stream().map(this::skillToEsSkill).toList());
                details.add(detail);
            });
        }
        return details;
    }

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
        special.setDomains(storedDomains(skill.getAttributes()));
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
     * 恢复主动、待机及终结技能各自关联的领域。
     **/
    @AfterMapping
    default void appendSkillDomains(CardSkillPO skill, @MappingTarget CardEsSkillDTO detail) {
        detail.setDomains(storedDomains(skill.getAttributes()));
    }

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
