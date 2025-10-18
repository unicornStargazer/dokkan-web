package com.hb.dokkan.service.helper;

import com.hb.dokkan.common.utils.IdGeneratorUtil;
import com.hb.dokkan.common.utils.JsonUtils;
import com.hb.dokkan.infrastructure.es.card.domain.CardEsPO;
import com.hb.dokkan.infrastructure.mysql.cards.domain.CardPO;
import com.hb.dokkan.service.domain.card.dto.CardBaseInfoAttribute;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Description es卡片同步助手
 * @Author stargazer
 * @Date 2025/10/18 17:42
 **/
@Component
@Slf4j
public class EsCardSyncHelper {

    /**
     * 构建es卡片po
     */
    public List<CardEsPO> buildEsCardPO(Map<String, List<?>> dataMap) {
        List<CardEsPO> esCards = new ArrayList<>();
        List<CardPO> cards = (List<CardPO>) dataMap.get("card");
        if (CollectionUtils.isEmpty(cards)) {
            return esCards;
        }
        for (CardPO card : cards) {
            CardEsPO esCardPO = new CardEsPO();
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
            esCards.add(esCardPO);
            CardBaseInfoAttribute attribute = JsonUtils.json2Object(card.getAttributes(), CardBaseInfoAttribute.class);
            buildCardBaseAttr(esCardPO, attribute);
        }

        return null;
    }

    /**
     * 构建卡片基础属性
     */
    private void buildCardBaseAttr(CardEsPO esCardPO, CardBaseInfoAttribute attributes) {
        if (Objects.isNull(attributes)) {
            return;
        }
        esCardPO.setLeaderSkill(attributes.getLeaderSkill());
        esCardPO.setPassiveSkill(attributes.getPassiveSkillDesc());
        esCardPO.setActiveSkill(attributes.getActiveSkillEffect());
        esCardPO.setFreeCardFlag(attributes.getFreeCardFlag());
        esCardPO.setDokkanFesFlag(attributes.getDokkanFesFlag());
        esCardPO.setCarnivalFlag(attributes.getCarnivalFlag());
//        esCardPO.setCategories(attributes.getCategoryId());
//        esCardPO.setLinkId(attributes.getLinkId());
    }
}
