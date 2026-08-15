package com.hb.dokkan.service.helper;

import com.google.common.collect.Lists;
import com.hb.dokkan.common.domain.bo.data.WikiCardBO;
import com.hb.dokkan.common.domain.dto.data.cards.CardBaseInfoAttribute;
import com.hb.dokkan.common.domain.dto.data.cards.CardBaseInfoDTO;
import com.hb.dokkan.common.domain.dto.data.cards.EzaCardInfoDTO;
import com.hb.dokkan.common.domain.dto.data.cards.SkillDTO;
import com.hb.dokkan.common.domain.dto.data.cards.SpecialAttackDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiCardBaseInfoDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiCardCategoryDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiCardDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiCardLinkDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiSkillDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiSpecialAttackDTO;
import com.hb.dokkan.common.enums.CardPropTypeEnum;
import com.hb.dokkan.common.utils.JsonUtils;
import com.hb.dokkan.service.convert.DokkanSyncConvert;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
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
     * 构建卡片同步业务对象。
     *
     * @param cardBO       卡片同步业务对象
     * @param wikiCardDTOS Wiki 卡片数据
     */
    public void buildData(WikiCardBO cardBO, List<WikiCardDTO> wikiCardDTOS) {
        // 依次构建基础、极限、技能和必杀数据，保持原同步写入顺序。
        buildCardBaseInfo(cardBO, wikiCardDTOS);
        buildEzaCardBaseInfo(cardBO, wikiCardDTOS);
        buildCardSkillInfo(cardBO, wikiCardDTOS);
        buildSpecialAttackInfo(cardBO, wikiCardDTOS);
    }

    /**
     * 构建必杀信息。
     *
     * @param cardBO       卡片同步业务对象
     * @param wikiCardDTOS Wiki 卡片数据
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
     * 构建卡片技能信息。
     *
     * @param cardBO       卡片同步业务对象
     * @param wikiCardDTOS Wiki 卡片数据
     */
    private void buildCardSkillInfo(WikiCardBO cardBO, List<WikiCardDTO> wikiCardDTOS) {
        List<SkillDTO> skills = Lists.newArrayList();
        wikiCardDTOS.forEach(wikiCardDTO -> {
            if (!CollectionUtils.isEmpty(wikiCardDTO.getFinishSkills())) {
                List<SkillDTO> finishSkills = buildSkillDetail(wikiCardDTO.getFinishSkills());
                Optional.ofNullable(finishSkills).ifPresent(skills::addAll);
            }
            if (!CollectionUtils.isEmpty(wikiCardDTO.getStandbySkills())) {
                List<SkillDTO> standbySkills = buildSkillDetail(wikiCardDTO.getStandbySkills());
                Optional.ofNullable(standbySkills).ifPresent(skills::addAll);
            }
        });
        cardBO.setDownPullSkills(skills);
    }

    /**
     * 转换技能明细。
     *
     * @param skills Wiki 技能数据
     * @return 技能 DTO 列表
     */
    private List<SkillDTO> buildSkillDetail(List<WikiSkillDTO> skills) {
        return convert.wikiSkill2DtoList(skills);
    }

    /**
     * 构建极限卡片信息。
     *
     * @param cardBO    卡片同步业务对象
     * @param wikiCards Wiki 卡片数据
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

    /**
     * 构建极限属性 JSON。
     *
     * @param curCardEzaDTO 极限卡片数据
     * @return 属性 JSON
     */
    private String buildEzaAttributes(EzaCardInfoDTO curCardEzaDTO) {
        CardBaseInfoAttribute ezaAttr = convert.wikiCard2EzaAttribute(curCardEzaDTO);
        return JsonUtils.object2Json(ezaAttr);
    }

    /**
     * 构建卡片基础信息。
     *
     * @param cardBO    卡片同步业务对象
     * @param wikiCards Wiki 卡片数据
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

    /**
     * 构建卡片扩展属性 JSON。
     *
     * @param wikiCard Wiki 卡片数据
     * @return 属性 JSON
     */
    private String buildAttributes(WikiCardDTO wikiCard) {
        WikiCardBaseInfoDTO card = wikiCard.getCard();
        CardBaseInfoAttribute attribute = convert.wikiCard2Attribute(card);
        attribute.setPotential(wikiCard.getPotential());
        List<Long> categoryIds = safeList(wikiCard.getCategories()).stream().map(WikiCardCategoryDTO::getId).toList();
        List<Long> linkIds = safeList(wikiCard.getCardLinks()).stream().map(WikiCardLinkDTO::getId).toList();
        List<Integer> standBySkillIds = safeList(wikiCard.getStandbySkills()).stream().map(WikiSkillDTO::getId).toList();
        List<Integer> finishSkillIds = safeList(wikiCard.getFinishSkills()).stream().map(WikiSkillDTO::getId).toList();
        List<Long> specialIds = safeList(wikiCard.getSpecials()).stream().map(WikiSpecialAttackDTO::getId).toList();
        attribute.setCategoryId(categoryIds);
        attribute.setLinkId(linkIds);
        attribute.setStandBySkillIds(standBySkillIds);
        attribute.setFinishSkillIds(finishSkillIds);
        attribute.setSpecialSkillIds(specialIds);
        if (!CollectionUtils.isEmpty(wikiCard.getTransformations())) {
            attribute.setNextCards(wikiCard.getTransformations());
        }
        return JsonUtils.object2Json(attribute);
    }

    /**
     * 空安全返回列表。
     *
     * @param values 原始列表
     * @param <T>    元素类型
     * @return 非空列表
     */
    private <T> List<T> safeList(List<T> values) {
        return values == null ? Collections.emptyList() : values;
    }
}
