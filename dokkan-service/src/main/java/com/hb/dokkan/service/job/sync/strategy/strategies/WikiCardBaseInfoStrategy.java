package com.hb.dokkan.service.job.sync.strategy.strategies;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.hb.dokkan.service.convert.DokkanSyncConvert;
import com.hb.dokkan.service.domain.dto.CardBaseInfoAttribute;
import com.hb.dokkan.service.domain.dto.CardBaseInfoDTO;
import com.hb.dokkan.service.domain.wiki.WikiCardBaseInfoDTO;
import com.hb.dokkan.service.domain.wiki.WikiCardDTO;
import com.hb.dokkan.service.enums.CardPropTypeEnum;
import com.hb.dokkan.service.job.sync.strategy.AbstractWikiCardStrategy;
import com.hb.dokkan.service.job.sync.strategy.context.WikiContext;
import com.hb.dokkan.service.job.sync.strategy.enums.WikiInfoTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

/**
 * @Description 卡片基础信息
 * @Author stargazer
 * @Date 2025/6/2 21:05
 **/
@Component
@Slf4j
public class WikiCardBaseInfoStrategy extends AbstractWikiCardStrategy<CardBaseInfoDTO> {

    @Resource
    private DokkanSyncConvert convert;

    @Override
    public boolean isMatched(WikiInfoTypeEnum type) {
        return WikiInfoTypeEnum.CARD.equals(type);
    }

    @Override
    protected List<CardBaseInfoDTO> buildData(WikiContext context) {
        List<WikiCardDTO> wikiCards = context.getWikiCards();
        List<CardBaseInfoDTO> cardBaseInfos = Lists.newArrayList();
        convertCardBaseInfo(wikiCards, cardBaseInfos);
        return cardBaseInfos;
    }

    private void convertCardBaseInfo(List<WikiCardDTO> wikiCards, List<CardBaseInfoDTO> cardBaseInfos) {
        if (CollectionUtils.isEmpty(wikiCards)) {
            return;
        }
        wikiCards.forEach(wikiCard -> {
            WikiCardBaseInfoDTO wikiCardBaseInfo = wikiCard.getCard();
            CardBaseInfoDTO cardBaseInfoDTO = convert.wikiCard2Dto(wikiCardBaseInfo);
            cardBaseInfoDTO.setAttributes(buildAttributes(wikiCardBaseInfo));
            cardBaseInfoDTO.setType(getType(wikiCardBaseInfo));
            cardBaseInfoDTO.setPropType(getPropType(wikiCardBaseInfo));
            cardBaseInfos.add(cardBaseInfoDTO);
        });
    }

    private String buildAttributes(WikiCardBaseInfoDTO dto) {
        CardBaseInfoAttribute attribute = CardBaseInfoAttribute.builder()
                .carnivalFlag(dto.getCarnivalFlag())
                .freeCardFlag(dto.getFreeCardFlag())
                .dokkanFesFlag(dto.getDokkanFesFlag())
                .leaderSkill(dto.getLeaderSkill())
                .passiveSkillDesc(dto.getPassiveSkillDesc())
                .build();
        return JSON.toJSONString(attribute);
    }

    private Integer getType(WikiCardBaseInfoDTO cardBaseInfoDTO) {
        Integer type = cardBaseInfoDTO.getPropType();
        return CardPropTypeEnum.getProp(type);
    }

    private String getPropType(WikiCardBaseInfoDTO cardBaseInfoDTO) {
        Integer type = cardBaseInfoDTO.getPropType();
        return Objects.requireNonNull(CardPropTypeEnum.getCardPropEnumByType(type)).getDescription();
    }



}
