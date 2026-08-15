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
import com.hb.dokkan.service.translation.DokkanTranslationService;
import jakarta.annotation.Resource;
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
import java.util.function.Function;

/**
 * @Description DokkanDB卡片转换器
 * @Author stargazer
 * @Date 2026/8/15 19:30
 **/
@Service
public class DokkanDbCardAssembler {

    @Resource
    private DokkanTranslationService translationService;

    /**
     * 将 DokkanDB 卡片和数值响应转换为现有 wiki 持久化模型。
     *
     * @param source DokkanDB 卡片响应
     * @param stats  DokkanDB 卡片数值响应
     * @return wiki 卡片模型
     */
    public WikiCardDTO assemble(DokkanDbCardDTO source, DokkanDbCardStatsDTO stats) {
        // 先批量翻译当前卡片涉及的文本，避免字段转换时重复请求翻译服务。
        Function<String, String> translate = translatorFor(source);
        // 组装基础卡片信息，保持现有 wiki 持久化结构不变。
        WikiCardBaseInfoDTO card = new WikiCardBaseInfoDTO();
        card.setId(source.getId());
        card.setName(translate.apply(source.getName()));
        card.setTitle(translate.apply(source.getTitle()));
        card.setRarity(source.getRarity());
        card.setPropType(source.getElement());
        card.setCost(estimateCost(source));
        card.setOpenAt(DateUtils.parseDokkanDbDate(source.getOpenAt()));
        card.setHpValue(stats == null ? null : stats.getHpMax());
        card.setAtkValue(stats == null ? null : stats.getAtkMax());
        card.setDefValue(stats == null ? null : stats.getDefMax());
        card.setLeaderSkillSetId(source.getLeaderSkillSetId());
        card.setLeaderSkill(translate.apply(source.getLeaderSkill()));
        card.setPassiveSkillSetId(source.getPassiveSkillSetId());
        card.setPassiveSkillName(translate.apply(source.getPassiveSkillName()));
        card.setPassiveSkillDesc(translate.apply(source.getPassiveSkillDescription()));
        card.setActiveSkillId(source.getActiveSkillSetId());
        card.setActiveSkillName(translate.apply(source.getActiveSkillName()));
        card.setActiveSkillEffect(translate.apply(source.getActiveSkillEffect()));
        card.setActiveSkillCondition(translate.apply(source.getActiveSkillCondition()));
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

    /**
     * 为当前卡片构建批量翻译函数。
     *
     * @param card DokkanDB 卡片响应
     * @return 文本翻译函数
     */
    private Function<String, String> translatorFor(DokkanDbCardDTO card) {
        List<String> texts = new ArrayList<>();
        add(texts, card.getName(), card.getTitle(), card.getLeaderSkill(), card.getPassiveSkillName(),
                card.getPassiveSkillDescription(), card.getActiveSkillName(), card.getActiveSkillEffect(),
                card.getActiveSkillCondition(), card.getStandbySkillName(), card.getStandbySkillEffect(),
                card.getStandbySkillCondition(), card.getEzaLeaderSkill(), card.getEzaPassiveSkillName(),
                card.getEzaPassiveSkillDescription(), card.getEzaLeaderSkillPre(),
                card.getEzaPassiveSkillNamePre(), card.getEzaPassiveSkillDescriptionPre());
        addAll(texts, card.getSpecialNames(), card.getSpecialDescriptions(), card.getFinishSkillNames(),
                card.getFinishSkillEffects(), card.getFinishSkillConditions(), card.getTransformationNames());
        List<String> translated = translationService.translateAll(texts);
        Map<String, String> values = new LinkedHashMap<>();
        for (int i = 0; i < texts.size(); i++) {
            values.put(texts.get(i), translated.get(i));
        }
        return value -> StringUtils.isBlank(value) ? value : values.getOrDefault(value, value);
    }

    /**
     * 构建普通必杀技列表。
     *
     * @param card      DokkanDB 卡片响应
     * @param translate 文本翻译函数
     * @return 必杀技列表
     */
    private List<WikiSpecialAttackDTO> buildSpecials(DokkanDbCardDTO card, Function<String, String> translate) {
        List<WikiSpecialAttackDTO> result = new ArrayList<>();
        for (int i = 0; i < safe(card.getSpecialIds()).size(); i++) {
            result.add(WikiSpecialAttackDTO.builder()
                    .id(at(card.getSpecialIds(), i))
                    .name(translate.apply(at(card.getSpecialNames(), i)))
                    .description(translate.apply(at(card.getSpecialDescriptions(), i)))
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
    private List<WikiSkillDTO> buildStandbySkills(DokkanDbCardDTO card, Function<String, String> translate) {
        if (card.getStandbySkillSetId() == null) {
            return Collections.emptyList();
        }
        return List.of(WikiSkillDTO.builder()
                .id(card.getStandbySkillSetId())
                .name(translate.apply(card.getStandbySkillName()))
                .conditionDescription(translate.apply(card.getStandbySkillCondition()))
                .effectDescription(translate.apply(card.getStandbySkillEffect()))
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
    private List<WikiSkillDTO> buildFinishSkills(DokkanDbCardDTO card, Function<String, String> translate) {
        List<WikiSkillDTO> result = new ArrayList<>();
        for (int i = 0; i < safe(card.getFinishSkillSetIds()).size(); i++) {
            result.add(WikiSkillDTO.builder()
                    .id(at(card.getFinishSkillSetIds(), i))
                    .name(translate.apply(at(card.getFinishSkillNames(), i)))
                    .conditionDescription(translate.apply(at(card.getFinishSkillConditions(), i)))
                    .effectDescription(translate.apply(at(card.getFinishSkillEffects(), i)))
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
                                                                 Function<String, String> translate) {
        List<WikiCardTransformationDTO> result = new ArrayList<>();
        for (int i = 0; i < safe(card.getTransformationIds()).size(); i++) {
            Long nextId = at(card.getTransformationIds(), i);
            WikiCardTransformationDTO.NextCardDTO next = WikiCardTransformationDTO.NextCardDTO.builder()
                    .id(nextId)
                    .name(translate.apply(at(card.getTransformationNames(), i)))
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
                                          Function<String, String> translate) {
        if (card.getStep() == null || card.getStep() <= 0) {
            return Collections.emptyList();
        }
        List<WikiEzaCardDTO> result = new ArrayList<>();
        if (card.getStep() >= TranslationConstants.EZA_PRE_STEP_THRESHOLD && card.getStepPre() != null) {
            result.add(eza(card, stats, translate, card.getStepPre(), card.getEzaLeaderSkillSetIdPre(),
                    card.getEzaLeaderSkillPre(), card.getEzaPassiveSkillSetIdPre(),
                    card.getEzaPassiveSkillNamePre(), card.getEzaPassiveSkillDescriptionPre()));
        }
        result.add(eza(card, stats, translate, card.getStep(), card.getEzaLeaderSkillSetId(),
                card.getEzaLeaderSkill(), card.getEzaPassiveSkillSetId(),
                card.getEzaPassiveSkillName(), card.getEzaPassiveSkillDescription()));
        return result;
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
    private WikiEzaCardDTO eza(DokkanDbCardDTO card, DokkanDbCardStatsDTO stats, Function<String, String> translate,
                               Integer step, Integer leaderId, String leader, Integer passiveId,
                               String passiveName, String passiveDescription) {
        WikiEzaCardDTO result = new WikiEzaCardDTO();
        result.setId(card.getId());
        result.setStep(step);
        result.setLvMax(stats != null && stats.getEzaLvMax() != null ? stats.getEzaLvMax()
                : stats == null ? null : stats.getBaseLvMax());
        result.setName(translate.apply(card.getName()));
        result.setRarity(card.getRarity());
        result.setPropType(card.getElement());
        result.setCost(estimateCost(card));
        result.setHpValue(ezaStat(stats == null ? null : stats.getHpMaxEza(), stats == null ? null : stats.getHpMax()));
        result.setAtkValue(ezaStat(stats == null ? null : stats.getAtkMaxEza(),
                stats == null ? null : stats.getAtkMax()));
        result.setDefValue(ezaStat(stats == null ? null : stats.getDefMaxEza(),
                stats == null ? null : stats.getDefMax()));
        result.setOpenAt(latestEzaDate(card));
        result.setTitle(translate.apply(card.getTitle()));
        result.setLeaderSkillId(leaderId);
        result.setLeaderSkill(translate.apply(leader));
        result.setPassiveSkillId(passiveId);
        result.setPassiveSkillName(translate.apply(passiveName));
        result.setPassiveSkillDesc(translate.apply(passiveDescription));
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

    /**
     * 将多个文本列表中的非空文本追加到目标集合。
     *
     * @param target 目标集合
     * @param values 文本列表数组
     */
    @SafeVarargs
    private final void addAll(List<String> target, List<String>... values) {
        for (List<String> value : values) {
            if (value != null) {
                value.stream().filter(StringUtils::isNotBlank).forEach(target::add);
            }
        }
    }

    /**
     * 将多个非空文本追加到目标集合。
     *
     * @param target 目标集合
     * @param values 文本数组
     */
    private void add(List<String> target, String... values) {
        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                target.add(value);
            }
        }
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
