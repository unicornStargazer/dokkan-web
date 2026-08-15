package com.hb.dokkan.service.helper;

import com.google.common.collect.Lists;
import com.hb.dokkan.common.constants.CardSyncConstants;
import com.hb.dokkan.common.constants.EsCardAttributeKey;
import com.hb.dokkan.common.domain.dto.cards.CardAttributeDTO;
import com.hb.dokkan.common.domain.po.es.cards.CardEsPO;
import com.hb.dokkan.common.domain.po.mysql.cards.CardPO;
import com.hb.dokkan.common.domain.po.mysql.cards.EzaCardPO;
import com.hb.dokkan.common.domain.po.mysql.cards.SkillPO;
import com.hb.dokkan.common.domain.po.mysql.cards.SpecialPO;
import com.hb.dokkan.common.domain.po.mysql.category.DokkanCategoryPO;
import com.hb.dokkan.common.domain.po.mysql.link.DokkanLinkPO;
import com.hb.dokkan.common.utils.DateUtils;
import com.hb.dokkan.common.utils.JsonUtils;
import com.hb.dokkan.config.mybatis.IdGeneratorUtil;
import com.hb.dokkan.service.convert.DokkanEsSyncConvert;
import com.hb.dokkan.service.storage.CardIconStorageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.hb.dokkan.common.constants.DokkanConstants.CARD;
import static com.hb.dokkan.common.constants.DokkanConstants.CATEGORY;
import static com.hb.dokkan.common.constants.DokkanConstants.EZA_CARD;
import static com.hb.dokkan.common.constants.DokkanConstants.LINK;
import static com.hb.dokkan.common.constants.DokkanConstants.SKILL;
import static com.hb.dokkan.common.constants.DokkanConstants.SPECIAL;

/**
 * @Description es卡片同步助手
 * @Author stargazer
 * @Date 2025/10/18 17:42
 **/
@Component
@Slf4j
public class EsCardSyncHelper {

    @Resource
    private DokkanEsSyncConvert dokkanEsSyncConvert;

    @Resource
    private CardIconStorageService cardIconStorageService;

    /**
     * 构建 ES 卡片文档。
     *
     * @param dataMap MySQL 卡片及关联数据
     * @return ES 卡片文档列表
     */
    public List<CardEsPO> buildEsCardPO(Map<String, List<?>> dataMap) {
        List<CardEsPO> esCards = new ArrayList<>();
        List<CardPO> cards = typedList(dataMap, CARD);
        List<DokkanCategoryPO> categories = typedList(dataMap, CATEGORY);
        List<DokkanLinkPO> links = typedList(dataMap, LINK);
        List<EzaCardPO> ezaCardPOS = typedList(dataMap, EZA_CARD);
        List<SpecialPO> specialPOS = typedList(dataMap, SPECIAL);
        List<SkillPO> skillPOS = typedList(dataMap, SKILL);
        if (CollectionUtils.isEmpty(cards)) {
            return esCards;
        }
        cards.forEach(card -> {
            CardEsPO esCardPO = new CardEsPO();
            esCards.add(esCardPO);
            esCardPO.setId(IdGeneratorUtil.generate16CharUuidSimple());
            esCardPO.setCardId(card.getCardId());
            esCardPO.setCardIcon(cardIconStorageService.saveCardIcon(card.getCardId()));
            esCardPO.setCardName(card.getCardName());
            esCardPO.setPropType(card.getPropType());
            esCardPO.setTitle(card.getTitle());
            esCardPO.setType(card.getType());
            esCardPO.setCost(card.getCost());
            esCardPO.setRarity(card.getRarity());
            esCardPO.setHpValue(card.getHpValue());
            esCardPO.setDefValue(card.getDefValue());
            esCardPO.setAtkValue(card.getAtkValue());
            esCardPO.setPublishTime(card.getPublishTime());
            CardAttributeDTO attribute = JsonUtils.json2Object(card.getAttributes(), CardAttributeDTO.class);

            // 按卡片属性中的关联 ID 组装分类、链接、技能、必杀等 ES 展示字段。
            buildCardBaseAttr(esCardPO, attribute, links, categories);
            buildEzaInfo(esCardPO, ezaCardPOS);
            buildSuperInfo(esCardPO, specialPOS, attribute);
            buildSkillInfo(esCardPO, skillPOS, attribute);
            buildAttributeInfo(esCardPO, card);
        });
        return esCards;
    }

    /**
     * 构建 ES 扩展属性。
     *
     * @param esCardPO ES 卡片文档
     * @param card     MySQL 卡片数据
     */
    private void buildAttributeInfo(CardEsPO esCardPO, CardPO card) {
        if (StringUtils.isBlank(card.getAttributes())) {
            return;
        }
        Map<String, Object> attributes = new HashMap<>();
        CardAttributeDTO cardAttributeDTO = JsonUtils.json2Object(card.getAttributes(), CardAttributeDTO.class);
        if (Objects.isNull(cardAttributeDTO)) {
            return;
        }
        attributes.put(EsCardAttributeKey.TRANSFORMATIONS, JsonUtils.object2Json(cardAttributeDTO.getNextCards()));
        esCardPO.setAttributes(attributes);
    }

    /**
     * 构建卡片技能信息。
     *
     * @param esCardPO ES 卡片文档
     * @param skillPOS 技能持久化数据
     * @param attribute 卡片扩展属性
     */
    private void buildSkillInfo(CardEsPO esCardPO, List<SkillPO> skillPOS, CardAttributeDTO attribute) {
        if (CollectionUtils.isEmpty(skillPOS) || Objects.isNull(attribute)) {
            log.error("skill数据查询失败");
            return;
        }
        Map<String, SkillPO> skillPOMap = skillPOS.stream()
                .collect(Collectors.toMap(SkillPO::getSkillId, Function.identity(), (oldValue, newValue) -> newValue));
        if (!CollectionUtils.isEmpty(attribute.getStandBySkillIds())) {
            List<SkillPO> standBySkills = Lists.newArrayList();
            esCardPO.setStandBySkills(standBySkills);
            attribute.getStandBySkillIds().forEach(skillId -> {
                SkillPO skillPO = skillPOMap.get(skillId.toString());
                if (Objects.nonNull(skillPO)) {
                    standBySkills.add(skillPO);
                }
            });
        }
        if (!CollectionUtils.isEmpty(attribute.getFinishSkillIds())) {
            List<SkillPO> finishSkills = Lists.newArrayList();
            esCardPO.setFinishSkills(finishSkills);
            attribute.getFinishSkillIds().forEach(finishSkillId -> {
                SkillPO skillPO = skillPOMap.get(finishSkillId.toString());
                if (Objects.nonNull(skillPO)) {
                    finishSkills.add(skillPO);
                }
            });
        }
    }

    /**
     * 构建卡片必杀信息。
     *
     * @param esCardPO  ES 卡片文档
     * @param specialPOS 必杀持久化数据
     * @param attribute  卡片扩展属性
     */
    private void buildSuperInfo(CardEsPO esCardPO, List<SpecialPO> specialPOS, CardAttributeDTO attribute) {
        if (CollectionUtils.isEmpty(specialPOS) || Objects.isNull(attribute)) {
            log.error("cardId:{} 没有必杀信息", esCardPO.getCardId());
            return;
        }
        Map<Long, SpecialPO> specialMap = specialPOS.stream()
                .collect(Collectors.toMap(SpecialPO::getSpecialId, Function.identity(), (oldValue, newValue) -> newValue));
        if (!CollectionUtils.isEmpty(attribute.getSpecialSkillIds())) {
            List<SpecialPO> specialSkills = Lists.newArrayList();
            esCardPO.setSpecialSkills(specialSkills);
            attribute.getSpecialSkillIds().forEach(specialId -> {
                SpecialPO specialPO = specialMap.get(specialId);
                if (Objects.nonNull(specialPO)) {
                    specialSkills.add(specialPO);
                }
            });
        }
    }

    /**
     * 构建卡片极限信息。
     *
     * @param esCardPO ES 卡片文档
     * @param ezaCardPOS 极限持久化数据
     */
    private void buildEzaInfo(CardEsPO esCardPO, List<EzaCardPO> ezaCardPOS) {
        if (CollectionUtils.isEmpty(ezaCardPOS)) {
            esCardPO.setEzaFlag(false);
            esCardPO.setSuperEzaFlag(false);
            return;
        }
        List<EzaCardPO> currentEzaCards = ezaCardPOS.stream()
                .filter(ezaCardPO -> esCardPO.getCardId().equals(ezaCardPO.getCardId()))
                .sorted(Comparator.comparing(EzaCardPO::getStep))
                .toList();
        if (CollectionUtils.isEmpty(currentEzaCards)) {
            esCardPO.setEzaFlag(false);
            esCardPO.setSuperEzaFlag(false);
            return;
        }
        esCardPO.setEzaFlag(true);
        esCardPO.setSuperEzaFlag(currentEzaCards.size() > CardSyncConstants.SUPER_EZA_COUNT_THRESHOLD);
        for (int i = 0; i < currentEzaCards.size(); i++) {
            EzaCardPO ezaCardPO = currentEzaCards.get(i);
            esCardPO.setEzaHpValue(ezaCardPO.getHpValue());
            esCardPO.setEzaDefValue(ezaCardPO.getDefValue());
            esCardPO.setEzaAtkValue(ezaCardPO.getAtkValue());
            if (i == CardSyncConstants.EZA_BASE_INDEX) {
                esCardPO.setEzaPublishTime(ezaCardPO.getPublishTime());
                esCardPO.setEzaLeaderSkill(ezaCardPO.getLeaderSkill());
                esCardPO.setEzaPassiveSkill(ezaCardPO.getPassiveSkillDesc());
            } else {
                esCardPO.setSuperEzaPublishTime(ezaCardPO.getPublishTime());
                esCardPO.setSuperEzaPassiveSkill(ezaCardPO.getPassiveSkillDesc());
            }
        }
        if (Objects.isNull(esCardPO.getPublishTime()) && Objects.isNull(esCardPO.getEzaPublishTime())
                && Objects.isNull(esCardPO.getSuperEzaPublishTime())) {
            return;
        }
        Date latestPublishTime = DateUtils.latestDate(esCardPO.getPublishTime(), esCardPO.getEzaPublishTime(),
                esCardPO.getSuperEzaPublishTime());
        if (Objects.nonNull(latestPublishTime)) {
            esCardPO.setOrderByTime(latestPublishTime);
        }
    }

    /**
     * 构建卡片基础扩展属性。
     *
     * @param esCardPO  ES 卡片文档
     * @param attributes 卡片扩展属性
     * @param links      链接持久化数据
     * @param categories 分类持久化数据
     */
    private void buildCardBaseAttr(CardEsPO esCardPO, CardAttributeDTO attributes,
                                   List<DokkanLinkPO> links, List<DokkanCategoryPO> categories) {
        if (Objects.isNull(attributes)) {
            return;
        }
        esCardPO.setLeaderSkill(attributes.getLeaderSkill());
        esCardPO.setPassiveSkill(attributes.getPassiveSkillDesc());
        esCardPO.setActiveSkill(attributes.getActiveSkillEffect());
        esCardPO.setFreeCardFlag(attributes.getFreeCardFlag());
        esCardPO.setDokkanFesFlag(attributes.getDokkanFesFlag());
        esCardPO.setCarnivalFlag(attributes.getCarnivalFlag());
        esCardPO.setCategories(buildCategoryNames(categories, attributes.getCategoryId()));
        esCardPO.setLinks(buildLinkName(links, attributes.getLinkId()));
        esCardPO.setPotentials(dokkanEsSyncConvert.convert2PotentialDTO(attributes.getPotential()));
        if (!CollectionUtils.isEmpty(attributes.getNextCards())) {
            attributes.getNextCards().stream()
                    .filter(nextCard -> esCardPO.getCardId().equals(nextCard.getStartCardId()))
                    .findAny()
                    .ifPresent(nextCard -> esCardPO.setNextCardId(nextCard.getNextCardId()));
        }
    }

    /**
     * 构建链接名称展示文本。
     *
     * @param links  链接持久化数据
     * @param linkId 链接 ID 列表
     * @return 链接名称展示文本
     */
    private String buildLinkName(List<DokkanLinkPO> links, List<Long> linkId) {
        if (CollectionUtils.isEmpty(links) || CollectionUtils.isEmpty(linkId)) {
            return null;
        }
        List<String> linkNames = new ArrayList<>();
        for (Long id : linkId) {
            DokkanLinkPO link = links.stream()
                    .filter(l -> l.getLinkId().equals(id))
                    .findFirst()
                    .orElse(null);
            if (Objects.nonNull(link)) {
                linkNames.add(link.getLinkName());
            }
        }
        return String.join(CardSyncConstants.DISPLAY_NAME_SEPARATOR, linkNames);
    }

    /**
     * 构建分类名称展示文本。
     *
     * @param categories 分类持久化数据
     * @param categoryId 分类 ID 列表
     * @return 分类名称展示文本
     */
    private String buildCategoryNames(List<DokkanCategoryPO> categories, List<Long> categoryId) {
        if (CollectionUtils.isEmpty(categories) || CollectionUtils.isEmpty(categoryId)) {
            return null;
        }
        List<String> categoryNames = new ArrayList<>();
        for (Long id : categoryId) {
            DokkanCategoryPO category = categories.stream()
                    .filter(c -> c.getCategoryId().equals(id))
                    .findFirst()
                    .orElse(null);
            if (Objects.nonNull(category)) {
                categoryNames.add(category.getCategoryName());
            }
        }
        return String.join(CardSyncConstants.DISPLAY_NAME_SEPARATOR, categoryNames);
    }

    /**
     * 从同步数据 Map 中读取指定类型列表，集中收敛泛型转换告警。
     *
     * @param dataMap 同步数据 Map
     * @param key     数据 key
     * @param <T>     目标元素类型
     * @return 指定类型列表，未提供时返回空列表
     */
    @SuppressWarnings("unchecked")
    private <T> List<T> typedList(Map<String, List<?>> dataMap, String key) {
        if (Objects.isNull(dataMap) || CollectionUtils.isEmpty(dataMap.get(key))) {
            return List.of();
        }
        return (List<T>) dataMap.get(key);
    }
}
