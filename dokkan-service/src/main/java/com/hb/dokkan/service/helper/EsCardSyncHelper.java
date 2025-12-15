package com.hb.dokkan.service.helper;

import com.google.common.collect.Lists;
import com.hb.dokkan.config.mybatis.IdGeneratorUtil;
import com.hb.dokkan.common.utils.JsonUtils;
import com.hb.dokkan.common.domain.po.es.cards.CardEsPO;
import com.hb.dokkan.common.domain.po.mysql.cards.CardPO;
import com.hb.dokkan.common.domain.po.mysql.cards.EzaCardPO;
import com.hb.dokkan.common.domain.po.mysql.cards.SkillPO;
import com.hb.dokkan.common.domain.po.mysql.cards.SpecialPO;
import com.hb.dokkan.common.domain.po.mysql.category.DokkanCategoryPO;
import com.hb.dokkan.common.domain.po.mysql.link.DokkanLinkPO;
import com.hb.dokkan.service.convert.DokkanEsSyncConvert;
import com.hb.dokkan.common.domain.dto.data.cards.CardBaseInfoAttribute;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.hb.dokkan.common.constants.DokkanConstants.*;

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

    /**
     * 构建es卡片po
     */
    public List<CardEsPO> buildEsCardPO(Map<String, List<?>> dataMap) {
        List<CardEsPO> esCards = new ArrayList<>();
        List<CardPO> cards = (List<CardPO>) dataMap.get(CARD);
        List<DokkanCategoryPO> categories = (List<DokkanCategoryPO>) dataMap.get(CATEGORY);
        List<DokkanLinkPO> links = (List<DokkanLinkPO>) dataMap.get(LINK);
        List<EzaCardPO> ezaCardPOS = (List<EzaCardPO>) dataMap.get(EZA_CARD);
        List<SpecialPO> specialPOS = (List<SpecialPO>) dataMap.get(SPECIAL);
        List<SkillPO> skillPOS = (List<SkillPO>) dataMap.get(SKILL);
        if (CollectionUtils.isEmpty(cards)) {
            return esCards;
        }
        cards.forEach(card -> {
            CardEsPO esCardPO = new CardEsPO();
            esCards.add(esCardPO);
            esCardPO.setId(IdGeneratorUtil.generate16CharUuidSimple());
            esCardPO.setCardId(card.getCardId());
            esCardPO.setCardName(card.getCardName());
            esCardPO.setPropType(card.getPropType());
            esCardPO.setType(card.getType());
            esCardPO.setCost(card.getCost());
            esCardPO.setRarity(card.getRarity());
            esCardPO.setHpValue(card.getHpValue());
            esCardPO.setDefValue(card.getDefValue());
            esCardPO.setAtkValue(card.getAtkValue());
            esCardPO.setPublishTime(card.getPublishTime());
            CardBaseInfoAttribute attribute = JsonUtils.json2Object(card.getAttributes(), CardBaseInfoAttribute.class);
            // card扩展信息
            buildCardBaseAttr(esCardPO, attribute, links, categories);

            // card极限信息
            buildEzaInfo(esCardPO, ezaCardPOS);

            // card必杀信息
            buildSuperInfo(esCardPO, specialPOS, attribute);

            // card技能信息
            buildSkillInfo(esCardPO, skillPOS, attribute);
        });
        return esCards;
    }

    /**
     * 构建卡片技能信息
     */
    private void buildSkillInfo(CardEsPO esCardPO, List<SkillPO> skillPOS, CardBaseInfoAttribute attribute) {
        if (CollectionUtils.isEmpty(skillPOS)) {
            log.error("skill数据查询失败");
            return;
        }
        Map<String, SkillPO> skillPOMap = skillPOS.stream()
                .collect(Collectors.toMap(SkillPO::getSkillId, Function.identity(), (oldValue, newValue) -> newValue));
        if (!CollectionUtils.isEmpty(attribute.getStandBySkillIds()))  {
            List<SkillPO> standBySkills = Lists.newArrayList();
            esCardPO.setStandBySkills(standBySkills);
            attribute.getStandBySkillIds().forEach(skillId -> {
                SkillPO skillPO = skillPOMap.get(skillId.toString());
                if (Objects.nonNull(skillPO)) {
                    standBySkills.add(skillPO);
                }
            });
        }
        if (!CollectionUtils.isEmpty(attribute.getFinishSkillIds()))  {
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
     * 必杀信息构建
     */
    private void buildSuperInfo(CardEsPO esCardPO, List<SpecialPO> specialPOS, CardBaseInfoAttribute attribute) {
        if (CollectionUtils.isEmpty(specialPOS) || Objects.isNull(attribute))  {
            log.error("cardId:{} 没有必杀信息", esCardPO.getCardId());
            return;
        }
        Map<Long, SpecialPO> specialMap = specialPOS.stream()
                .collect(Collectors.toMap(SpecialPO::getSpecialId, Function.identity(), (oldValue, newValue) -> newValue));
        if (!CollectionUtils.isEmpty(attribute.getSpecialSkillIds()))  {
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
     * 构建卡片极限信息
     */
    private void buildEzaInfo(CardEsPO esCardPO, List<EzaCardPO> ezaCardPOS) {
        if (CollectionUtils.isEmpty(ezaCardPOS)) {
            esCardPO.setEzaFlag(false);
            esCardPO.setSuperEzaFlag(false);
        }
        List<EzaCardPO> currentEzaCards = ezaCardPOS.stream()
                .filter(ezaCardPO -> esCardPO.getCardId().equals(ezaCardPO.getCardId()))
                .sorted(Comparator.comparing(EzaCardPO::getStep))
                .toList();
        if (CollectionUtils.isEmpty(currentEzaCards)) {
            esCardPO.setEzaFlag(false);
            esCardPO.setSuperEzaFlag(false);
        }
        esCardPO.setEzaFlag(true);
        esCardPO.setSuperEzaFlag(currentEzaCards.size() > 1);
        for (int i = 0; i < currentEzaCards.size(); i++) {
            EzaCardPO ezaCardPO = currentEzaCards.get(i);
            esCardPO.setEzaHpValue(ezaCardPO.getHpValue());
            esCardPO.setEzaDefValue(ezaCardPO.getDefValue());
            esCardPO.setEzaAtkValue(ezaCardPO.getAtkValue());
            if (i < 1) {
                esCardPO.setEzaPublishTime(ezaCardPO.getPublishTime());
                esCardPO.setEzaLeaderSkill(ezaCardPO.getLeaderSkill());
                esCardPO.setEzaPassiveSkill(ezaCardPO.getPassiveSkillDesc());
            } else {
                esCardPO.setSuperEzaPublishTime(ezaCardPO.getPublishTime());
                esCardPO.setSuperEzaPassiveSkill(ezaCardPO.getPassiveSkillDesc());
            }
        }
    }

    /**
     * 构建卡片基础属性
     */
    private void buildCardBaseAttr(CardEsPO esCardPO, CardBaseInfoAttribute attributes, List<DokkanLinkPO> links, List<DokkanCategoryPO> categories) {
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
                    .filter(nextCard -> esCardPO.getCardId().equals(nextCard.getBaseId()))
                    .findAny()
                    .ifPresent(nextCard -> esCardPO.setNextCardId(nextCard.getId()));
        }
    }

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
        return String.join(",", linkNames);
    }

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
        return String.join(",", categoryNames);
    }
}
