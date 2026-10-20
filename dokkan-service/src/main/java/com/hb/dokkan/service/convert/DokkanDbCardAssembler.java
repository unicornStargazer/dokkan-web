package com.hb.dokkan.service.convert;

import com.hb.dokkan.common.constants.TranslationConstants;
import com.hb.dokkan.common.domain.dto.data.cards.PotentialDTO;
import com.hb.dokkan.common.domain.dto.data.dokkandb.DokkanDbCardDTO;
import com.hb.dokkan.common.domain.dto.data.dokkandb.DokkanDbCardStatsDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiCardBaseInfoDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiCardCategoryDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiCardDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiCardLinkDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiCardTransformationDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiEzaCardDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiSkillDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiSpecialAttackDTO;
import com.hb.dokkan.common.utils.DateUtils;
import com.hb.dokkan.translation.BusinessTranslationService;
import com.hb.dokkan.translation.domain.TranslationBatchRequest;
import com.hb.dokkan.translation.domain.CardTextType;
import com.hb.dokkan.translation.domain.TranslationItem;
import com.hb.dokkan.translation.domain.TranslationItemResult;
import com.hb.dokkan.translation.domain.TranslationProviderType;
import com.hb.dokkan.translation.domain.TranslationScene;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * @Description DokkanDB卡片转换器
 * @Author stargazer
 * @Date 2026/8/15 19:30
 **/
@Service
@Slf4j
public class DokkanDbCardAssembler {

    @Resource
    private BusinessTranslationService translationService;

    /**
     * 将 DokkanDB 卡片和数值响应转换为现有 wiki 持久化模型。
     *
     * @param source DokkanDB 卡片响应
     * @param stats  DokkanDB 卡片数值响应
     * @return wiki 卡片模型
     */
    public WikiCardDTO assemble(DokkanDbCardDTO source, DokkanDbCardStatsDTO stats) {
        return assemble(source, stats, false);
    }

    /**
     * 将 DokkanDB 卡片和数值响应转换为现有 wiki 持久化模型。
     *
     * @param source           DokkanDB 卡片响应
     * @param stats            DokkanDB 卡片数值响应
     * @param forceRetranslate 是否强制绕过旧翻译缓存重新翻译
     * @return wiki 卡片模型
     */
    public WikiCardDTO assemble(DokkanDbCardDTO source, DokkanDbCardStatsDTO stats, boolean forceRetranslate) {
        return assembleAll(List.of(source), Collections.singletonList(stats), forceRetranslate).getFirst();
    }

    /**
     * 跨卡收集文本并通过统一入口批量翻译，再按卡片和文本位置恢复组装结果。
     *
     * @param cards            卡片详情
     * @param stats            与卡片顺序对应的数值数据
     * @param forceRetranslate 是否绕过 mapping/cache 强制翻译
     * @return 与输入卡片顺序对应的 wiki 卡片模型
     */
    public List<WikiCardDTO> assembleAll(List<DokkanDbCardDTO> cards, List<DokkanDbCardStatsDTO> stats,
                                         boolean forceRetranslate) {
        if (CollectionUtils.isEmpty(cards)) {
            return Collections.emptyList();
        }
        List<TranslationItem> items = new ArrayList<>();
        List<List<CardText>> cardTexts = new ArrayList<>(cards.size());
        for (int cardIndex = 0; cardIndex < cards.size(); cardIndex++) {
            List<CardText> texts = collectTranslationTexts(cards.get(cardIndex));
            cardTexts.add(texts);
            for (int textIndex = 0; textIndex < texts.size(); textIndex++) {
                CardText text = texts.get(textIndex);
                items.add(new TranslationItem(translationKey(cardIndex, textIndex), text.sourceText(), text.type()));
            }
        }
        List<TranslationItemResult> translations = translationService.translate(new TranslationBatchRequest(
                TranslationScene.CARD_TEXT, TranslationProviderType.LLM, items, forceRetranslate));
        Map<String, String> translatedByKey = new LinkedHashMap<>();
        translations.forEach(result -> translatedByKey.put(result.key(),
                result.status() == TranslationItemResult.Status.SUCCESS
                        ? result.translatedText() : result.sourceText()));
        List<WikiCardDTO> assembled = new ArrayList<>(cards.size());
        for (int cardIndex = 0; cardIndex < cards.size(); cardIndex++) {
            int currentCardIndex = cardIndex;
            List<CardText> texts = cardTexts.get(cardIndex);
            Map<CardText, String> cardTranslations = new LinkedHashMap<>();
            for (int textIndex = 0; textIndex < texts.size(); textIndex++) {
                cardTranslations.put(texts.get(textIndex), translatedByKey.getOrDefault(
                        translationKey(currentCardIndex, textIndex), texts.get(textIndex).sourceText()));
            }
            BiFunction<CardTextType, String, String> translate = (type, value) -> StringUtils.isBlank(value) ? value
                    : cardTranslations.getOrDefault(new CardText(type, value), value);
            DokkanDbCardStatsDTO cardStats = stats != null && cardIndex < stats.size() ? stats.get(cardIndex) : null;
            assembled.add(assembleTranslated(cards.get(cardIndex), cardStats, translate));
        }
        return assembled;
    }

    /**
     * 使用已完成的文本翻译结果组装卡片数据。
     *
     * @param source    DokkanDB 卡片
     * @param stats     DokkanDB 卡片数值
     * @param translate 原文到译文的映射函数
     * @return wiki 卡片模型
     */
    private WikiCardDTO assembleTranslated(DokkanDbCardDTO source, DokkanDbCardStatsDTO stats,
                                           BiFunction<CardTextType, String, String> translate) {
        // 组装基础卡片信息，保持现有 wiki 持久化结构不变。
        WikiCardBaseInfoDTO card = new WikiCardBaseInfoDTO();
        card.setId(source.getId());
        card.setName(translate.apply(CardTextType.CHARACTER_NAME, source.getName()));
        card.setTitle(translate.apply(CardTextType.TITLE, source.getTitle()));
        card.setRarity(source.getRarity());
        card.setPropType(source.getElement());
        card.setCost(estimateCost(source));
        card.setOpenAt(DateUtils.parseDokkanDbDate(source.getOpenAt()));
        card.setHpValue(stats == null ? null : stats.getHpMax());
        card.setAtkValue(stats == null ? null : stats.getAtkMax());
        card.setDefValue(stats == null ? null : stats.getDefMax());
        card.setLeaderSkillSetId(source.getLeaderSkillSetId());
        card.setLeaderSkill(translate.apply(CardTextType.LEADER_SKILL, source.getLeaderSkill()));
        card.setPassiveSkillSetId(resolvePassiveSkillId(source));
        card.setPassiveSkillName(translate.apply(CardTextType.SKILL_NAME, source.getPassiveSkillName()));
        card.setPassiveSkillDesc(translate.apply(CardTextType.PASSIVE_SKILL, source.getPassiveSkillDescription()));
        card.setActiveSkillId(source.getActiveSkillSetId());
        card.setActiveSkillName(translate.apply(CardTextType.SKILL_NAME, source.getActiveSkillName()));
        card.setActiveSkillEffect(translate.apply(CardTextType.ACTIVE_EFFECT, source.getActiveSkillEffect()));
        card.setActiveSkillCondition(translate.apply(CardTextType.ACTIVE_CONDITION, source.getActiveSkillCondition()));
        card.setSkillLevelMax(source.getSkillLvMax());
        card.setDokkanFesFlag(hasTag(source, TranslationConstants.CARD_TAG_DOKKAN_FESTIVAL));
        card.setCarnivalFlag(hasTag(source, TranslationConstants.CARD_TAG_CARNIVAL));
        card.setFreeCardFlag(isFreeCard(source));

        return WikiCardDTO.builder()
                .card(card)
                .potential(buildPotential(stats, false))
                .categories(safe(source.getCategoryIds()).stream().map(this::category).toList())
                .cardLinks(safe(source.getLinkIds()).stream().map(this::link).toList())
                .specials(buildSpecials(source, translate))
                .standbySkills(buildStandbySkills(source, translate))
                .finishSkills(buildFinishSkills(source, translate))
                .transformations(buildTransformations(source, translate))
                .ezaCardInfos(buildEza(source, stats, translate))
                .awakeningRoutes(Collections.emptyList())
                .build();
    }

    /** 按字段语义收集当前卡片实际使用的文本，普通与极限技能共用对应模板。 */
    private List<CardText> collectTranslationTexts(DokkanDbCardDTO card) {
        List<CardText> texts = new ArrayList<>();
        add(texts, CardTextType.CHARACTER_NAME, card.getName());
        add(texts, CardTextType.TITLE, card.getTitle());
        add(texts, CardTextType.LEADER_SKILL, card.getLeaderSkill(), card.getEzaLeaderSkill());
        add(texts, CardTextType.PASSIVE_SKILL, card.getPassiveSkillDescription(), card.getEzaPassiveSkillDescription());
        add(texts, CardTextType.SKILL_NAME, card.getPassiveSkillName(), card.getEzaPassiveSkillName(),
                card.getActiveSkillName(), card.getStandbySkillName());
        add(texts, CardTextType.ACTIVE_EFFECT, card.getActiveSkillEffect());
        add(texts, CardTextType.ACTIVE_CONDITION, card.getActiveSkillCondition());
        add(texts, CardTextType.STANDBY_EFFECT, card.getStandbySkillEffect());
        add(texts, CardTextType.STANDBY_CONDITION, card.getStandbySkillCondition());
        add(texts, CardTextType.SKILL_NAME, safe(card.getSpecialNames()).toArray(String[]::new));
        add(texts, CardTextType.SUPER_ATTACK, safe(card.getSpecialDescriptions()).toArray(String[]::new));
        add(texts, CardTextType.SKILL_NAME, safe(card.getFinishSkillNames()).toArray(String[]::new));
        add(texts, CardTextType.FINISH_EFFECT, safe(card.getFinishSkillEffects()).toArray(String[]::new));
        add(texts, CardTextType.FINISH_CONDITION, safe(card.getFinishSkillConditions()).toArray(String[]::new));
        add(texts, CardTextType.CHARACTER_NAME, safe(card.getTransformationNames()).toArray(String[]::new));
        return texts;
    }

    /**
     * 构造卡片和文本位置组成的唯一关联键。
     *
     * @param cardIndex 卡片批次下标
     * @param textIndex 卡片文本下标
     * @return 唯一关联键
     */
    private String translationKey(int cardIndex, int textIndex) {
        return cardIndex + ":" + textIndex;
    }

    /**
     * 构建普通必杀技列表。
     *
     * @param card      DokkanDB 卡片响应
     * @param translate 文本翻译函数
     * @return 必杀技列表
     */
    private List<WikiSpecialAttackDTO> buildSpecials(DokkanDbCardDTO card, BiFunction<CardTextType, String, String> translate) {
        List<WikiSpecialAttackDTO> result = new ArrayList<>();
        for (int i = 0; i < safe(card.getSpecialIds()).size(); i++) {
            result.add(WikiSpecialAttackDTO.builder()
                    .id(at(card.getSpecialIds(), i))
                    .name(translate.apply(CardTextType.SKILL_NAME, at(card.getSpecialNames(), i)))
                    .description(translate.apply(CardTextType.SUPER_ATTACK, at(card.getSpecialDescriptions(), i)))
                    .increaseRate(at(card.getSpecialIncreaseRates(), i))
                    .lvBonus(valueOrZero(at(card.getSpecialLvBonuses(), i)))
                    .style(at(card.getSpecialStyles(), i))
                    .lvStart(TranslationConstants.SPECIAL_ATTACK_LEVEL_START)
                    .eballNumStart(at(card.getSpecialKis(), i))
                    .build());
        }
        return result;
    }

    /**
     * 构建 standby 技能列表。
     *
     * @param card      DokkanDB 卡片响应
     * @param translate 文本翻译函数
     * @return standby 技能列表
     */
    private List<WikiSkillDTO> buildStandbySkills(DokkanDbCardDTO card, BiFunction<CardTextType, String, String> translate) {
        if (card.getStandbySkillSetId() == null) {
            return Collections.emptyList();
        }
        return List.of(WikiSkillDTO.builder()
                .id(card.getStandbySkillSetId())
                .name(translate.apply(CardTextType.SKILL_NAME, card.getStandbySkillName()))
                .conditionDescription(translate.apply(CardTextType.STANDBY_CONDITION, card.getStandbySkillCondition()))
                .effectDescription(translate.apply(CardTextType.STANDBY_EFFECT, card.getStandbySkillEffect()))
                .label(TranslationConstants.SKILL_LABEL_STANDBY)
                .build());
    }

    /**
     * 构建 finish 技能列表。
     *
     * @param card      DokkanDB 卡片响应
     * @param translate 文本翻译函数
     * @return finish 技能列表
     */
    private List<WikiSkillDTO> buildFinishSkills(DokkanDbCardDTO card, BiFunction<CardTextType, String, String> translate) {
        List<WikiSkillDTO> result = new ArrayList<>();
        for (int i = 0; i < safe(card.getFinishSkillSetIds()).size(); i++) {
            result.add(WikiSkillDTO.builder()
                    .id(at(card.getFinishSkillSetIds(), i))
                    .name(translate.apply(CardTextType.SKILL_NAME, at(card.getFinishSkillNames(), i)))
                    .conditionDescription(translate.apply(CardTextType.FINISH_CONDITION, at(card.getFinishSkillConditions(), i)))
                    .effectDescription(translate.apply(CardTextType.FINISH_EFFECT, at(card.getFinishSkillEffects(), i)))
                    .increaseRate(at(card.getFinishSkillIncreaseRates(), i))
                    .label(TranslationConstants.SKILL_LABEL_FINISH)
                    .build());
        }
        return result;
    }

    /**
     * 构建卡片变身信息列表。
     *
     * @param card      DokkanDB 卡片响应
     * @param translate 文本翻译函数
     * @return 变身信息列表
     */
    private List<WikiCardTransformationDTO> buildTransformations(DokkanDbCardDTO card,
                                                                 BiFunction<CardTextType, String, String> translate) {
        List<WikiCardTransformationDTO> result = new ArrayList<>();
        for (int i = 0; i < safe(card.getTransformationIds()).size(); i++) {
            Long nextId = at(card.getTransformationIds(), i);
            WikiCardTransformationDTO.NextCardDTO next = WikiCardTransformationDTO.NextCardDTO.builder()
                    .id(nextId)
                    .name(translate.apply(CardTextType.CHARACTER_NAME, at(card.getTransformationNames(), i)))
                    .type(TranslationConstants.WIKI_TRANSFORMATION_TYPE)
                    .build();
            result.add(WikiCardTransformationDTO.builder()
                    .startCardId(card.getId())
                    .nextCardId(nextId)
                    .nextCard(next)
                    .build());
        }
        return result;
    }

    /**
     * 构建 EZA 卡片信息列表。
     *
     * @param card      DokkanDB 卡片响应
     * @param stats     DokkanDB 数值响应
     * @param translate 文本翻译函数
     * @return EZA 卡片信息列表
     */
    private List<WikiEzaCardDTO> buildEza(DokkanDbCardDTO card, DokkanDbCardStatsDTO stats,
                                          BiFunction<CardTextType, String, String> translate) {
        if (card.getStep() == null || card.getStep() <= 0) {
            return Collections.emptyList();
        }
        List<WikiEzaCardDTO> result = new ArrayList<>();
        result.add(eza(card, stats, translate, card.getStep(), card.getEzaLeaderSkillSetId(),
                card.getEzaLeaderSkill(), resolveEzaPassiveSkillId(card),
                card.getEzaPassiveSkillName(), card.getEzaPassiveSkillDescription()));
        return result;
    }

    /**
     * 解析普通卡片被动技能 ID，优先使用单值字段，缺失时回退到数组首项。
     *
     * @param card DokkanDB 卡片响应
     * @return 被动技能 ID，均缺失时返回 null
     */
    private Long resolvePassiveSkillId(DokkanDbCardDTO card) {
        Long passiveSkillId = card.getPassiveSkillSetId();
        if (passiveSkillId == null) {
            passiveSkillId = first(card.getPassiveIds());
        }
        if (passiveSkillId == null) {
            log.error("DokkanDB passive skill id missing, cardId:{}", card.getId());
        }
        return passiveSkillId;
    }

    /**
     * 解析当前最新极限阶段的被动技能 ID，优先使用 EZA 单值字段，缺失时回退到数组首项。
     *
     * @param card DokkanDB 卡片响应
     * @return 极限被动技能 ID，均缺失时返回 null
     */
    private Integer resolveEzaPassiveSkillId(DokkanDbCardDTO card) {
        Integer passiveSkillId = card.getEzaPassiveSkillSetId();
        if (passiveSkillId == null) {
            passiveSkillId = first(card.getPassiveIdsEza());
        }
        if (passiveSkillId == null) {
            log.error("DokkanDB EZA passive skill id missing, cardId:{}", card.getId());
        }
        return passiveSkillId;
    }

    /**
     * 构建单条 EZA 卡片信息。
     *
     * @param card               DokkanDB 卡片响应
     * @param stats              DokkanDB 数值响应
     * @param translate          文本翻译函数
     * @param step               EZA 阶段
     * @param leaderId           队长技 ID
     * @param leader             队长技描述
     * @param passiveId          被动技能 ID
     * @param passiveName        被动技能名称
     * @param passiveDescription 被动技能描述
     * @return EZA 卡片信息
     */
    private WikiEzaCardDTO eza(DokkanDbCardDTO card, DokkanDbCardStatsDTO stats, BiFunction<CardTextType, String, String> translate,
                               Integer step, Integer leaderId, String leader, Integer passiveId,
                               String passiveName, String passiveDescription) {
        WikiEzaCardDTO result = new WikiEzaCardDTO();
        result.setId(card.getId());
        result.setStep(step);
        result.setLvMax(stats != null && stats.getEzaLvMax() != null ? stats.getEzaLvMax()
                : stats == null ? null : stats.getBaseLvMax());
        result.setName(translate.apply(CardTextType.CHARACTER_NAME, card.getName()));
        result.setRarity(card.getRarity());
        result.setPropType(card.getElement());
        result.setCost(estimateCost(card));
        result.setHpValue(ezaStat(stats == null ? null : stats.getHpMaxEza(), stats == null ? null : stats.getHpMax()));
        result.setAtkValue(ezaStat(stats == null ? null : stats.getAtkMaxEza(),
                stats == null ? null : stats.getAtkMax()));
        result.setDefValue(ezaStat(stats == null ? null : stats.getDefMaxEza(),
                stats == null ? null : stats.getDefMax()));
        result.setOpenAt(latestEzaDate(card));
        result.setTitle(translate.apply(CardTextType.TITLE, card.getTitle()));
        result.setLeaderSkillId(leaderId);
        result.setLeaderSkill(translate.apply(CardTextType.LEADER_SKILL, leader));
        result.setPassiveSkillId(passiveId);
        result.setPassiveSkillName(translate.apply(CardTextType.SKILL_NAME, passiveName));
        result.setPassiveSkillDesc(translate.apply(CardTextType.PASSIVE_SKILL, passiveDescription));
        result.setDokkanFesFlag(hasTag(card, TranslationConstants.CARD_TAG_DOKKAN_FESTIVAL));
        result.setCarnivalFlag(hasTag(card, TranslationConstants.CARD_TAG_CARNIVAL));
        result.setFreeCardFlag(isFreeCard(card));
        return result;
    }

    /**
     * 构建潜能数值列表。
     *
     * @param stats DokkanDB 数值响应
     * @param eza   是否使用 EZA 潜能表
     * @return 潜能数值列表
     */
    private List<PotentialDTO> buildPotential(DokkanDbCardStatsDTO stats, boolean eza) {
        if (stats == null) {
            return Collections.emptyList();
        }
        Map<String, Long> hp = eza ? stats.getHpTableEza() : stats.getHpTableMax();
        Map<String, Long> atk = eza ? stats.getAtkTableEza() : stats.getAtkTableMax();
        Map<String, Long> def = eza ? stats.getDefTableEza() : stats.getDefTableMax();
        if (CollectionUtils.isEmpty(hp)) {
            return Collections.emptyList();
        }
        return hp.keySet().stream()
                .sorted((left, right) -> Integer.compare(Integer.parseInt(left), Integer.parseInt(right)))
                .map(order -> PotentialDTO.builder()
                        .order(Integer.parseInt(order))
                        .hp(hp.get(order))
                        .atk(atk == null ? null : atk.get(order))
                        .def(def == null ? null : def.get(order))
                        .build())
                .toList();
    }

    /**
     * 构建卡片分类关联对象。
     *
     * @param id 分类 ID
     * @return 卡片分类关联对象
     */
    private WikiCardCategoryDTO category(Long id) {
        WikiCardCategoryDTO result = new WikiCardCategoryDTO();
        result.setId(id);
        return result;
    }

    /**
     * 构建卡片链接关联对象。
     *
     * @param id 链接 ID
     * @return 卡片链接关联对象
     */
    private WikiCardLinkDTO link(Long id) {
        WikiCardLinkDTO result = new WikiCardLinkDTO();
        result.setId(id);
        return result;
    }

    /**
     * 判断卡片是否为免费卡。
     *
     * @param card DokkanDB 卡片响应
     * @return 是否免费卡
     */
    private boolean isFreeCard(DokkanDbCardDTO card) {
        return !hasTag(card, TranslationConstants.CARD_TAG_SUMMONABLE)
                && !hasTag(card, TranslationConstants.CARD_TAG_DOKKAN_FESTIVAL)
                && !hasTag(card, TranslationConstants.CARD_TAG_CARNIVAL);
    }

    /**
     * DokkanDB 未暴露 cost，按游戏常见规则估算默认 cost。
     *
     * @param card DokkanDB 卡片响应
     * @return 估算 cost
     */
    private int estimateCost(DokkanDbCardDTO card) {
        if (card.getRarity() != null && card.getRarity() >= TranslationConstants.HIGH_RARITY_COST_THRESHOLD) {
            return TranslationConstants.HIGH_RARITY_DEFAULT_COST;
        }
        if (hasTag(card, TranslationConstants.CARD_TAG_DOKKAN_FESTIVAL)) {
            return TranslationConstants.DOKKAN_FESTIVAL_DEFAULT_COST;
        }
        if (hasTag(card, TranslationConstants.CARD_TAG_SUMMONABLE)) {
            return TranslationConstants.SUMMONABLE_DEFAULT_COST;
        }
        return TranslationConstants.FREE_CARD_DEFAULT_COST;
    }

    /**
     * 判断卡片标签是否包含指定值。
     *
     * @param card  DokkanDB 卡片响应
     * @param value 标签值
     * @return 是否包含标签
     */
    private boolean hasTag(DokkanDbCardDTO card, String value) {
        return StringUtils.containsIgnoreCase(card.getTag(), value);
    }

    /**
     * 获取最新 EZA 开放时间。
     *
     * @param card DokkanDB 卡片响应
     * @return 最新 EZA 开放时间
     */
    private Date latestEzaDate(DokkanDbCardDTO card) {
        return safe(card.getAwakeningData()).stream()
                .map(DokkanDbCardDTO.AwakeningDTO::getOpenAtEza)
                .filter(StringUtils::isNotBlank)
                .map(DateUtils::parseDokkanDbDate)
                .filter(Objects::nonNull)
                .max(Date::compareTo)
                .orElse(DateUtils.parseDokkanDbDate(card.getOpenAtUpdate()));
    }

    /** 收集非空文本并保留字段类型，避免相同原文在不同技能中互相覆盖。 */
    private void add(List<CardText> target, CardTextType type, String... values) {
        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                target.add(new CardText(type, value));
            }
        }
    }

    /** 字段类型与原文共同确定一次翻译的业务语义。 */
    private record CardText(CardTextType type, String sourceText) {
    }

    /**
     * 获取安全列表，空入参返回空集合。
     *
     * @param values 原始列表
     * @param <T>    列表元素泛型
     * @return 非 null 列表
     */
    private <T> List<T> safe(List<T> values) {
        return values == null ? Collections.emptyList() : values;
    }

    /**
     * 按下标安全获取列表元素。
     *
     * @param values 原始列表
     * @param index  目标下标
     * @param <T>    列表元素泛型
     * @return 对应元素，越界或列表为空时返回 null
     */
    private <T> T at(List<T> values, int index) {
        return values == null || index < 0 || index >= values.size() ? null : values.get(index);
    }

    /**
     * 获取列表首项，空列表返回 null。
     *
     * @param values 候选 ID 列表
     * @param <T>    列表元素类型
     * @return 列表首项或 null
     */
    private <T> T first(List<T> values) {
        return CollectionUtils.isEmpty(values) ? null : values.get(0);
    }

    /**
     * 将 Integer 空值兜底为 0。
     *
     * @param value 原始值
     * @return 非空整数
     */
    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    /**
     * EZA 数值为空时回退到普通数值。
     *
     * @param eza  EZA 数值
     * @param base 普通数值
     * @return 可用数值
     */
    private Long ezaStat(Long eza, Long base) {
        return eza == null ? base : eza;
    }
}
