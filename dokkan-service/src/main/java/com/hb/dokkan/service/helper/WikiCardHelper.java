package com.hb.dokkan.service.helper;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.hb.dokkan.common.domain.dto.data.cards.*;
import com.hb.dokkan.common.domain.dto.data.wiki.*;
import com.hb.dokkan.service.convert.DokkanSyncConvert;
import com.hb.dokkan.common.domain.bo.data.WikiCardBO;
import com.hb.dokkan.common.enums.CardPropTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

/**
 * @Description wiki card同步数据工具类
 * @Author stargazer
 * @Date 2025/9/15 21:00
 **/
@Component
@Slf4j
public class WikiCardHelper {

    @Resource
    private DokkanSyncConvert convert;

    /**
     * 构建卡牌信息
     */
    public void buildData(WikiCardBO cardBO, List<WikiCardDTO> wikiCardDTOS) {
        // 基础信息构造
        buildCardBaseInfo(cardBO, wikiCardDTOS);
        // eza信息
        buildEzaCardBaseInfo(cardBO, wikiCardDTOS);
        // skill信息
        buildCardSkillInfo(cardBO, wikiCardDTOS);
        // 必杀信息
        buildSpecialAttackInfo(cardBO,wikiCardDTOS);

    }

    /**
     * 必杀信息
     */
    private void buildSpecialAttackInfo(WikiCardBO cardBO, List<WikiCardDTO> wikiCardDTOS) {
        List<SpecialAttackDTO> specialAttacks = Lists.newArrayList();
        wikiCardDTOS.forEach(wikiCardDTO -> {
            if (!CollectionUtils.isEmpty(wikiCardDTO.getSpecials())) {
                List<SpecialAttackDTO> specialAttackDTOS = convert.wikiSpecial2DtoList(wikiCardDTO.getSpecials());
                Optional.ofNullable(specialAttackDTOS).ifPresent(specialAttacks::addAll);
            }
        });
        cardBO.setSpecialAttacks(specialAttacks);
    }

    /**
     * skill信息
     */
    private void buildCardSkillInfo(WikiCardBO cardBO, List<WikiCardDTO> wikiCardDTOS) {
        List<SkillDTO> skills = Lists.newArrayList();
        wikiCardDTOS.forEach(wikiCardDTO -> {
            if (!CollectionUtils.isEmpty(wikiCardDTO.getFinishSkills())) {
                List<SkillDTO> finishSkills = buildSkillDetail(wikiCardDTO.getFinishSkills());
                Optional.ofNullable(finishSkills).ifPresent(skills::addAll);
            }
            if (CollectionUtils.isEmpty(wikiCardDTO.getStandbySkills())) {
                List<SkillDTO> standbySkills = buildSkillDetail(wikiCardDTO.getStandbySkills());
                Optional.ofNullable(standbySkills).ifPresent(skills::addAll);
            }
        });
        cardBO.setDownPullSkills(skills);
    }

    private List<SkillDTO> buildSkillDetail(List<WikiSkillDTO> skills) {
        return convert.wikiSkill2DtoList(skills);
    }

    /**
     * 极限信息构造
     */
    private void buildEzaCardBaseInfo(WikiCardBO cardBO, List<WikiCardDTO> wikiCards) {
        if (CollectionUtils.isEmpty(wikiCards)) {
            return;
        }
        List<EzaCardInfoDTO> ezaCardInfoDTOS = Lists.newArrayList();
        wikiCards.forEach(wikiCardDTO -> {
            List<EzaCardInfoDTO> curCardEza = convert.wikiCard2EzaDtoList(wikiCardDTO.getEzaCardInfos());
            if (CollectionUtils.isEmpty(curCardEza)) {
                return;
            }
            curCardEza.forEach(curCardEzaDTO -> {
                curCardEzaDTO.setCardId(wikiCardDTO.getCard().getId());
                curCardEzaDTO.setCardName(wikiCardDTO.getCard().getName());
                curCardEzaDTO.setTitle(wikiCardDTO.getCard().getTitle());
                curCardEzaDTO.setCost(wikiCardDTO.getCard().getCost());
                curCardEzaDTO.setCarnivalFlag(wikiCardDTO.getCard().getCarnivalFlag());
                curCardEzaDTO.setFreeCardFlag(wikiCardDTO.getCard().getFreeCardFlag());
                curCardEzaDTO.setDokkanFesFlag(wikiCardDTO.getCard().getDokkanFesFlag());
                curCardEzaDTO.setPropType(wikiCardDTO.getCard().getPropType());
                curCardEzaDTO.setType(CardPropTypeEnum.getProp(curCardEzaDTO.getPropType()));
                curCardEzaDTO.setPublishTime(curCardEzaDTO.getPublishTime());
                curCardEzaDTO.setRarity(wikiCardDTO.getCard().getRarity());
                curCardEzaDTO.setAttributes(buildEzaAttributes(curCardEzaDTO));
            });
            ezaCardInfoDTOS.addAll(curCardEza);
        });
        cardBO.setEzaCardInfos(ezaCardInfoDTOS);
    }

    private String buildEzaAttributes(EzaCardInfoDTO curCardEzaDTO) {
        CardBaseInfoAttribute ezaAttr = convert.wikiCard2EzaAttribute(curCardEzaDTO);
        return JSON.toJSONString(ezaAttr);
    }

    /**
     * 基础信息构造
     */
    private void buildCardBaseInfo(WikiCardBO cardBO, List<WikiCardDTO> wikiCards) {
        if (CollectionUtils.isEmpty(wikiCards)) {
            return;
        }
        List<CardBaseInfoDTO> cardBaseInfos = Lists.newArrayList();
        wikiCards.forEach(wikiCard -> {
            WikiCardBaseInfoDTO wikiCardBaseInfo = wikiCard.getCard();
            CardBaseInfoDTO cardBaseInfoDTO = convert.wikiCard2BaseDto(wikiCardBaseInfo);
            cardBaseInfoDTO.setAttributes(buildAttributes(wikiCard));
            cardBaseInfos.add(cardBaseInfoDTO);
        });
        cardBO.setCardBaseData(cardBaseInfos);
    }


    private String buildAttributes(WikiCardDTO wikiCard) {
        WikiCardBaseInfoDTO card = wikiCard.getCard();
        CardBaseInfoAttribute attribute = convert.wikiCard2Attribute(card);
        attribute.setPotential(wikiCard.getPotential());
        List<Long> categoryIds = wikiCard.getCategories().stream().map(WikiCardCategoryDTO::getId).toList();
        List<Long> linkIds = wikiCard.getCardLinks().stream().map(WikiCardLinkDTO::getId).toList();
        List<Integer> standBySkillIds = wikiCard.getStandbySkills().stream().map(WikiSkillDTO::getId).toList();
        List<Integer> finishSkillIds = wikiCard.getFinishSkills().stream().map(WikiSkillDTO::getId).toList();
        List<Long> specialIds = wikiCard.getSpecials().stream().map(WikiSpecialAttackDTO::getId).toList();
        attribute.setCategoryId(categoryIds);
        attribute.setLinkId(linkIds);
        attribute.setStandBySkillIds(standBySkillIds);
        attribute.setFinishSkillIds(finishSkillIds);
        attribute.setSpecialSkillIds(specialIds);
        if (!CollectionUtils.isEmpty(wikiCard.getTransformations())) {
            attribute.setNextCards(wikiCard.getTransformations());
        }
        return JSON.toJSONString(attribute);
    }

}
