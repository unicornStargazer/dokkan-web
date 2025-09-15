package com.hb.dokkan.service.helper;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.hb.dokkan.service.convert.DokkanSyncConvert;
import com.hb.dokkan.service.domain.bo.WikiCardBO;
import com.hb.dokkan.service.domain.dto.base.CardBaseInfoAttribute;
import com.hb.dokkan.service.domain.dto.base.CardBaseInfoDTO;
import com.hb.dokkan.service.domain.wiki.WikiCardBaseInfoDTO;
import com.hb.dokkan.service.domain.wiki.WikiCardDTO;
import com.hb.dokkan.service.job.sync.strategy.context.WikiContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

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

    public void buildData(WikiCardBO cardBO, WikiContext context) {
        // 基础信息构造
        buildCardBaseInfo(cardBO, context);
        // eza信息

        //
    }

    /**
     * 基础信息构造
     */
    private void buildCardBaseInfo(WikiCardBO cardBO, WikiContext context) {
        List<WikiCardDTO> wikiCards = context.getWikiCards();
        List<CardBaseInfoDTO> cardBaseInfos = Lists.newArrayList();
        if (CollectionUtils.isEmpty(wikiCards)) {
            return;
        }
        wikiCards.forEach(wikiCard -> {
            WikiCardBaseInfoDTO wikiCardBaseInfo = wikiCard.getCard();
            CardBaseInfoDTO cardBaseInfoDTO = convert.wikiCard2Dto(wikiCardBaseInfo);
            cardBaseInfoDTO.setAttributes(buildAttributes(wikiCard));
            cardBaseInfos.add(cardBaseInfoDTO);
        });
        cardBO.setCardBaseData(cardBaseInfos);
    }


    private String buildAttributes(WikiCardDTO wikiCard) {
        WikiCardBaseInfoDTO card = wikiCard.getCard();
        CardBaseInfoAttribute attribute = convert.wikiCard2Attribute(card);
        return JSON.toJSONString(attribute);
    }

}
