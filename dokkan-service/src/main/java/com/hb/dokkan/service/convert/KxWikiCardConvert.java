package com.hb.dokkan.service.convert;

import com.hb.dokkan.common.constants.CardSyncConstants;
import com.hb.dokkan.common.constants.ExceptionErrorCode;
import com.hb.dokkan.common.domain.bo.data.KxWikiCardBO;
import com.hb.dokkan.common.domain.dto.data.kx.*;
import com.hb.dokkan.common.domain.po.mysql.cards.*;
import com.hb.dokkan.common.domain.po.mysql.link.DokkanLinkPO;
import com.hb.dokkan.common.enums.CardPropTypeEnum;
import com.hb.dokkan.common.enums.CardStateEnum;
import com.hb.dokkan.common.enums.CardSkillTypeEnum;
import com.hb.dokkan.common.exception.domain.DokkanBizException;
import com.hb.dokkan.common.utils.DateUtils;
import com.hb.dokkan.common.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description KX Wiki 卡片聚合转换，集中处理继承、状态对齐与源技能映射
 * @Author Codex
 * @Date 2026/11/8
 **/
@Component
@Slf4j
public class KxWikiCardConvert {
    /**
     * 转换完整源数据为本体及形态卡聚合；缺少必要字段时抛错，由调用方隔离该卡。
     * @param detail 卡片详情
     * @param enrichment 必杀倍率和类别
     * @param extras 极限日期；无极限卡可为空
     * @return 五张卡片表与链接补齐记录
     **/
    public KxWikiCardBO toAggregate(KxWikiCardDetailDTO detail, KxWikiEnrichmentDTO enrichment, KxWikiExtrasDTO extras) {
        if (!haveMatchingCardSources(detail, enrichment, extras)) {
            throw new DokkanBizException(ExceptionErrorCode.GET_WIKI_INFO_ERROR);
        }
        // 先校验全部状态，禁止先删后插时把缺失源数据写成空状态。
        Map<String, KxWikiStateDTO> parentStates = stateMap(detail.getStates());
        String hash = JsonUtils.sourceHash(detail.getSourceJson(), enrichment.getSourceJson(),
                extras == null ? null : extras.getSourceJson());
        Date release = DateUtils.parseUtcDate(detail.getFirstReleased() == null ? null : detail.getFirstReleased().getAt());
        KxWikiCardBO aggregate = new KxWikiCardBO();
        CardPO card = baseCard(detail.getId(), detail.getName(), detail.getUnitName(), detail.getTitle(), detail.getRarity(),
                detail.getElement(), detail.getCost(), detail.getResourceId(), detail.getThumbnail(), detail.getCharacterId(),
                release, hash, detail.getPotential(), parentStates);
        aggregate.getCards().add(card);
        appendChildren(aggregate, card, detail.getStates(), parentStates, enrichment.getStates(), extras, false);
        // 形态卡继承对应状态的队长技和分类；资源、属性及技能仍取形态自身。
        for (KxWikiFormDTO form : safe(detail.getForms())) {
            Map<String, KxWikiStateDTO> formStates = stateMap(form.getStates());
            CardPO formCard = baseCard(form.getCardId(), form.getName(), form.getUnitName(), detail.getTitle(),
                    form.getRarity(), form.getElement(), form.getCost(), form.getResourceId(), form.getThumbnail(),
                    detail.getCharacterId(), release, hash, detail.getPotential(), parentStates);
            formCard.setBaseCardId(detail.getId());
            formCard.setFormKind(form.getKind());
            KxWikiStateDTO formBase = formStates.get(CardStateEnum.BASE.getKey());
            formCard.setFormTrigger(formBase.getTrigger() == null ? null : formBase.getTrigger().getDescription());
            aggregate.getCards().add(formCard);
            Map<String, KxWikiEnrichedStateDTO> formEnrichment = safe(enrichment.getForms()).stream()
                    .filter(row -> Objects.equals(row.getCardId(), form.getCardId())).findFirst()
                    .map(KxWikiEnrichedFormDTO::getStates).orElse(Collections.emptyMap());
            appendChildren(aggregate, formCard, form.getStates(), parentStates, formEnrichment, extras, true);
        }
        return aggregate;
    }

    /**
     * 创建卡片主记录，数值归属 card_state，自动标记在持久化合并旧标记后执行。
     **/
    private CardPO baseCard(Long id, String name, String unitName, String title, Integer rarity, Integer element,
                            Integer cost, Long resourceId, String thumbnail, Integer characterId, Date release,
                            String hash, KxWikiPotentialDTO potential, Map<String, KxWikiStateDTO> states) {
        CardPropTypeEnum property = element == null ? null : CardPropTypeEnum.getCardPropEnumByType(element);
        if (!hasRequiredCardFields(id, name, rarity, cost, property)) {
            throw new DokkanBizException(ExceptionErrorCode.GET_WIKI_INFO_ERROR);
        }
        CardPO card = new CardPO();
        card.setCardId(id);
        card.setCardName(name);
        card.setUnitName(unitName);
        KxWikiTextDTO leader = states.get(CardStateEnum.BASE.getKey()).getLeader();
        card.setTitle(StringUtils.defaultIfBlank(title, leader == null ? null : leader.getName()));
        card.setRarity(rarity);
        card.setElement(element);
        card.setPropType(property.getDescription());
        card.setType(CardPropTypeEnum.getProp(element));
        card.setCost(cost);
        card.setResourceId(resourceId);
        card.setCharacterId(characterId);
        card.setThumbnail(thumbnail);
        card.setPublishTime(release);
        card.setSourceHash(hash);
        card.setSyncedTime(new Date());
        card.setTagLocked(false);
        card.setDokkanFesFlag(false);
        card.setCarnivalFlag(false);
        card.setFreeCardFlag(false);
        card.setPotential(JsonUtils.object2Json(safe(potential == null ? null : potential.getPresets()).stream().map(preset -> {
            Map<String, Object> values = new LinkedHashMap<>();
            values.put(CardSyncConstants.POTENTIAL_PERCENTAGE_FIELD, preset.getPercentage());
            if (preset.getBonus() != null) {
                values.putAll(JsonUtils.convert2StringObjectMap(preset.getBonus()));
            }
            return values;
        }).toList()));
        return card;
    }

    /**
     * 转换状态、关系和技能；不同状态关系不一致时始终以 base 为准并告警。
     **/
    private void appendChildren(KxWikiCardBO aggregate, CardPO card, List<KxWikiStateDTO> states,
                                Map<String, KxWikiStateDTO> parentStates, Map<String, KxWikiEnrichedStateDTO> enrichment,
                                KxWikiExtrasDTO extras, boolean form) {
        Map<String, KxWikiStateDTO> ownStates = stateMap(states);
        KxWikiStateDTO base = ownStates.get(CardStateEnum.BASE.getKey());
        List<KxWikiTextDTO> categories = form ? parentStates.get(CardStateEnum.BASE.getKey()).getCategories() : base.getCategories();
        card.setEzaFlag(ownStates.containsKey(CardStateEnum.EZA.getKey()));
        card.setSezaFlag(ownStates.containsKey(CardStateEnum.SEZA.getKey()));
        for (KxWikiStateDTO source : states) {
            KxWikiStateDTO parent = parentStates.getOrDefault(source.getKey(), parentStates.get(CardStateEnum.BASE.getKey()));
            if (haveDifferentStateRelations(base, source, categories, form)) {
                log.warn("KX Wiki state relations differ; using base, cardId={}, state={}", card.getCardId(), source.getKey());
            }
            KxWikiTextDTO leader = form ? parent.getLeader() : source.getLeader();
            aggregate.getStates().add(toState(card, source, leader, extras));
            KxWikiEnrichedStateDTO supplement = enrichment == null ? null : enrichment.get(source.getKey());
            appendSkills(aggregate, card.getCardId(), source, supplement);
        }
        for (Long categoryId : ids(categories)) {
            CardCategoryRelPO relation = new CardCategoryRelPO();
            relation.setCardId(card.getCardId());
            relation.setCategoryId(categoryId);
            aggregate.getCategories().add(relation);
        }
        int sort = 0;
        Set<Long> seenLinks = new HashSet<>();
        for (KxWikiTextDTO source : safe(base.getLinks())) {
            if (source.getId() == null || !seenLinks.add(source.getId())) {
                continue;
            }
            CardLinkRelPO relation = new CardLinkRelPO();
            relation.setCardId(card.getCardId());
            relation.setLinkId(source.getId());
            relation.setSort(sort++);
            aggregate.getLinks().add(relation);
            DokkanLinkPO link = new DokkanLinkPO();
            link.setLinkId(source.getId());
            link.setLinkName(source.getName());
            link.setLevel1Description(text(source));
            link.setLevel10Description(StringUtils.defaultIfBlank(source.getMaxLevelDescriptionTemplate(), source.getMaxLevelDescription()));
            aggregate.getLinkDefinitions().add(link);
        }
    }

    /**
     * 将状态数值、队长技、被动与 UTC 开放时间转换为持久化字段；无被动时保留空值。
     **/
    private CardStatePO toState(CardPO card, KxWikiStateDTO source, KxWikiTextDTO leader, KxWikiExtrasDTO extras) {
        if (!hasRequiredStateFields(source)) {
            throw new DokkanBizException(ExceptionErrorCode.GET_WIKI_INFO_ERROR);
        }
        CardStatePO state = new CardStatePO();
        state.setCardId(card.getCardId());
        state.setStateKey(source.getKey());
        state.setStep(source.getStep());
        state.setLvMax(source.getLevel());
        state.setSaLvMax(source.getSuperAttackLevel());
        state.setHpValue(source.getStats().getHp());
        state.setAtkValue(source.getStats().getAtk());
        state.setDefValue(source.getStats().getDef());
        if (leader != null) {
            state.setLeaderSkillId(leader.getId());
            state.setLeaderSkillName(leader.getName());
            state.setLeaderSkillDesc(text(leader));
        }
        // 部分卡片或形态本身没有被动，不阻止导入，也不继承其他状态的被动。
        KxWikiTextDTO passive = source.getPassive();
        if (passive != null) {
            state.setPassiveSkillId(passive.getId());
            state.setPassiveSkillName(passive.getName());
            state.setPassiveSkillDesc(text(passive));
        }
        // 被动关联的领域属于当前状态，与技能行的展开领域关系分别持久化。
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put(CardSyncConstants.SKILL_DOMAINS_FIELD, safe(passive == null ? null : passive.getDomains()));
        state.setAttributes(JsonUtils.object2Json(attributes));
        String openedAt = extras == null || extras.getAwakeningDates() == null ? null : extras.getAwakeningDates().get(source.getKey());
        state.setPublishTime(CardStateEnum.BASE.getKey().equals(source.getKey()) ? card.getPublishTime() : DateUtils.parseUtcDate(openedAt));
        return state;
    }

    /**
     * 按源记录 id 保存技能并对齐 enrichment，保留共用效果编号等额外字段供追溯。
     **/
    private void appendSkills(KxWikiCardBO aggregate, Long cardId, KxWikiStateDTO state, KxWikiEnrichedStateDTO enrichment) {
        // skill_id 是可复用的效果编号，不同气力档位必须通过各自的记录 id 对齐。
        Map<Long, KxWikiSkillDTO> superEnrichment = safe(enrichment == null ? null : enrichment.getSuperAttacks())
                .stream().filter(row -> row.getId() != null)
                .collect(Collectors.toMap(KxWikiSkillDTO::getId, row -> row));
        int sort = 0;
        for (KxWikiSkillDTO skill : safe(state.getSuperAttacks())) {
            KxWikiSkillDTO supplement = superEnrichment.get(skill.getId());
            aggregate.getSkills().add(toSkill(cardId, state.getKey(), CardSkillTypeEnum.SUPER, skill, supplement, sort++));
        }
        if (state.getActiveSkill() != null) {
            aggregate.getSkills().add(toSkill(cardId, state.getKey(), CardSkillTypeEnum.ACTIVE, state.getActiveSkill(),
                    enrichment == null ? null : enrichment.getActiveSkill(), 0));
        }
        if (state.getStandbySkill() != null) {
            aggregate.getSkills().add(toSkill(cardId, state.getKey(), CardSkillTypeEnum.STANDBY, state.getStandbySkill(), null, 0));
            sort = 0;
            for (KxWikiSkillDTO skill : safe(state.getStandbySkill().getFinishSkills())) {
                KxWikiSkillDTO supplement = enrichment == null ? null : safe(enrichment.getFinishSkills()).stream()
                        .filter(row -> Objects.equals(row.getId(), skill.getId())).findFirst().orElse(null);
                aggregate.getSkills().add(toSkill(cardId, state.getKey(), CardSkillTypeEnum.FINISH, skill, supplement, sort++));
            }
        }
    }

    /**
     * 映射一个技能，必要 ID/名称缺失时阻止整张卡入库。
     **/
    private CardSkillPO toSkill(Long cardId, String state, CardSkillTypeEnum type, KxWikiSkillDTO source,
                                KxWikiSkillDTO supplement, int sort) {
        Long skillId = source.getId();
        if (skillId == null || StringUtils.isBlank(source.getName())) {
            throw new DokkanBizException(ExceptionErrorCode.GET_WIKI_INFO_ERROR);
        }
        CardSkillPO skill = new CardSkillPO();
        skill.setCardId(cardId);
        skill.setStateKey(state);
        skill.setSkillType(type.getType());
        skill.setSkillId(skillId);
        skill.setName(source.getName());
        skill.setStyle(source.getStyle());
        skill.setKi(source.getKi());
        skill.setConditionDesc(StringUtils.defaultIfBlank(source.getConditionTemplate(), source.getCondition()));
        skill.setEffectDesc(StringUtils.defaultIfBlank(source.getDescriptionTemplate(), source.getDescription()));
        skill.setSort(sort);
        if (supplement != null) {
            if (supplement.getDamage() != null) {
                skill.setIncreaseRate(supplement.getDamage().getIncreaseRate());
                skill.setLvBonus(supplement.getDamage().getLevelBonus());
            }
            safe(supplement.getAttackTypes()).stream().findFirst().ifPresent(attack -> {
                skill.setAttackCategoryId(attack.getCategoryId());
                skill.setAttackCategoryName(attack.getName());
            });
        }
        Map<String, Object> attributes = new LinkedHashMap<>();
        if (type == CardSkillTypeEnum.SUPER) {
            attributes.put(CardSyncConstants.SKILL_EFFECT_ID_FIELD, source.getSkillId());
        }
        attributes.put(CardSyncConstants.SKILL_BONUSES_FIELD, source.getBonuses());
        attributes.put(CardSyncConstants.SKILL_EXEC_LIMIT_FIELD, source.getExecLimit());
        attributes.put(CardSyncConstants.SKILL_TURN_FIELD, source.getTurn());
        attributes.put(CardSyncConstants.SKILL_SA_LEVEL_FIELD, source.getSuperAttackLevel());
        attributes.put(CardSyncConstants.SKILL_DOMAINS_FIELD, safe(source.getDomains()));
        skill.setAttributes(JsonUtils.object2Json(attributes));
        return skill;
    }

    /**
     * 校验并索引 base/eza/seza 状态，未知或重复状态视为上游结构变化。
     **/
    private Map<String, KxWikiStateDTO> stateMap(List<KxWikiStateDTO> states) {
        Map<String, KxWikiStateDTO> values = new LinkedHashMap<>();
        Set<String> allowed = Arrays.stream(CardStateEnum.values()).map(CardStateEnum::getKey).collect(Collectors.toSet());
        for (KxWikiStateDTO state : safe(states)) {
            if (!isSupportedState(state, allowed)) {
                throw new DokkanBizException(ExceptionErrorCode.GET_WIKI_INFO_ERROR);
            }
            if (values.putIfAbsent(state.getKey(), state) != null) {
                throw new DokkanBizException(ExceptionErrorCode.GET_WIKI_INFO_ERROR);
            }
        }
        if (!values.containsKey(CardStateEnum.BASE.getKey())) {
            throw new DokkanBizException(ExceptionErrorCode.GET_WIKI_INFO_ERROR);
        }
        return values;
    }

    /**
     * 依次检查详情、倍率和开放时间属于同一卡片，extras 允许缺省。
     **/
    private boolean haveMatchingCardSources(KxWikiCardDetailDTO detail, KxWikiEnrichmentDTO enrichment,
                                           KxWikiExtrasDTO extras) {
        if (detail == null) {
            return false;
        } else if (detail.getId() == null) {
            return false;
        } else if (enrichment == null) {
            return false;
        } else if (!Objects.equals(detail.getId(), enrichment.getCardId())) {
            return false;
        } else if (extras == null) {
            return true;
        } else {
            return Objects.equals(detail.getId(), extras.getCardId());
        }
    }

    /**
     * 依次检查主表必填业务字段，保持 ID 判空优先于数值比较。
     **/
    private boolean hasRequiredCardFields(Long id, String name, Integer rarity, Integer cost,
                                          CardPropTypeEnum property) {
        if (id == null) {
            return false;
        } else if (id <= CardSyncConstants.MIN_VALID_CARD_ID) {
            return false;
        } else if (StringUtils.isBlank(name)) {
            return false;
        } else if (rarity == null) {
            return false;
        } else if (cost == null) {
            return false;
        } else {
            return property != null;
        }
    }

    /**
     * 链接总是和本卡 base 比较，形态卡分类按继承规则跳过自身分类检查。
     **/
    private boolean haveDifferentStateRelations(KxWikiStateDTO base, KxWikiStateDTO source,
                                                List<KxWikiTextDTO> categories, boolean form) {
        if (!ids(base.getLinks()).equals(ids(source.getLinks()))) {
            return true;
        } else if (form) {
            return false;
        } else {
            return !ids(categories).equals(ids(source.getCategories()));
        }
    }

    /**
     * 校验三围对象及满级数值；被动是可选技能，不作为状态完整性的判断条件。
     **/
    private boolean hasRequiredStateFields(KxWikiStateDTO source) {
        if (source.getStats() == null) {
            return false;
        } else if (source.getStats().getHp() == null) {
            return false;
        } else if (source.getStats().getAtk() == null) {
            return false;
        } else {
            return source.getStats().getDef() != null;
        }
    }

    /**
     * 判断源状态键是否受支持；重复状态另由索引写入步骤检查。
     **/
    private boolean isSupportedState(KxWikiStateDTO state, Set<String> allowed) {
        if (state == null) {
            return false;
        } else {
            return allowed.contains(state.getKey());
        }
    }

    /**
     * 模板优先，确保 passiveImg 占位符不会丢失。
     **/
    private String text(KxWikiTextDTO source) {
        return StringUtils.defaultIfBlank(source.getDescriptionTemplate(), source.getDescription());
    }

    /**
     * 提取关系 ID，保持 base 的展示顺序并去重。
     **/
    private List<Long> ids(List<KxWikiTextDTO> source) {
        return safe(source).stream().map(KxWikiTextDTO::getId).filter(Objects::nonNull).distinct().toList();
    }

    /**
     * null 集合统一作为空集合处理。
     **/
    private <T> List<T> safe(List<T> values) {
        return values == null ? Collections.emptyList() : values;
    }
}
