package com.hb.dokkan.service.helper;

import com.hb.dokkan.common.utils.IdGeneratorUtil;
import com.hb.dokkan.common.utils.JsonUtils;
import com.hb.dokkan.infrastructure.es.card.domain.CardEsPO;
import com.hb.dokkan.infrastructure.mysql.cards.domain.CardPO;
import com.hb.dokkan.infrastructure.mysql.categories.domain.DokkanCategoryPO;
import com.hb.dokkan.infrastructure.mysql.links.domain.DokkanLinkPO;
import com.hb.dokkan.service.domain.card.dto.CardBaseInfoAttribute;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.hb.dokkan.common.constants.DokkanConstants.*;

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
        List<CardPO> cards = (List<CardPO>) dataMap.get(CARD);
        List<DokkanCategoryPO> categories = (List<DokkanCategoryPO>) dataMap.get(CATEGORY);
        List<DokkanLinkPO> links = (List<DokkanLinkPO>) dataMap.get(LINK);
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
            buildCardBaseAttr(esCardPO, attribute, links, categories);
        }

        return null;
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
