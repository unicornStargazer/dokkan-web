package com.hb.dokkan.service.job.sync.strategy.strategies;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.hb.dokkan.common.http.CommonHttpClient;
import com.hb.dokkan.common.utils.JsonUtils;
import com.hb.dokkan.service.domain.CardBaseInfoDTO;
import com.hb.dokkan.service.domain.sync.AwakeningInfoDTO;
import com.hb.dokkan.service.domain.sync.SyncCardDTO;
import com.hb.dokkan.service.domain.sync.WikiCardDTO;
import com.hb.dokkan.service.enums.CardRarityEnum;
import com.hb.dokkan.service.enums.SyncCardUrlEnum;
import com.hb.dokkan.service.job.sync.strategy.WikiInfoStrategy;
import com.hb.dokkan.service.job.sync.strategy.context.WikiContext;
import com.hb.dokkan.service.job.sync.strategy.enums.WikiInfoTypeEnum;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * @Description xxxxx
 * @Author stargazer
 * @Date 2025/6/2 21:05
 **/
@Component
@Slf4j
public class WikiCardInfoStrategy implements WikiInfoStrategy {

    @Resource
    private CommonHttpClient httpClient;

    private File htmlFile;

    @PostConstruct
    public void preLoad() {
        htmlFile = new File("C:/Users/lenovo/Desktop/html/cards.html");
    }


    @Override
    public boolean isMatched(WikiInfoTypeEnum type) {
        return WikiInfoTypeEnum.CARD.equals(type);
    }

    @Override
    public void execute(WikiContext context) {
        List<WikiCardDTO> wikiCards = getWikiCards();
        context.setWikiCards(wikiCards);
    }

    private List<WikiCardDTO> getWikiCards(){
        if (!htmlFile.exists()){
            throw new RuntimeException("html file is null");
        }
        try {
            Document doc = Jsoup.parse(htmlFile, "utf-8");
            Element cardsElement = doc.selectFirst("cards");
            if (Objects.isNull(cardsElement)){
                return Lists.newArrayList();
            }
            String cardsJson = cardsElement.attr("v-bind:cardsjson");
            List<SyncCardDTO> syncCardsJobs = JsonUtils.json2List(cardsJson, SyncCardDTO.class);
            List<Long> cardIds = syncCardsJobs.stream()
                    .filter(card -> card.getRarity() > 3)
                    .map(SyncCardDTO::getId)
                    .toList();
            List<WikiCardDTO> wikiCardDTOS = Lists.newArrayList();
            cardIds.parallelStream()
                    .forEach(cardId -> {
                        WikiCardDTO wikiCardDTO = httpClient.getForEntity(SyncCardUrlEnum.WIKI_CARDS.getUrl() + cardId, WikiCardDTO.class);
                        if (filterCard(wikiCardDTO)) {
                            log.info("请求成功，card:{}", JSON.toJSONString(Objects.requireNonNull(wikiCardDTO).getCard()));
                            wikiCardDTOS.add(wikiCardDTO);
                        }
                    });
            return wikiCardDTOS;
        } catch (IOException e) {
            log.error("WikiCardInfoStrategy#getWikiCards error");
            return Lists.newArrayList();
        }
    }

    private static boolean filterCard(WikiCardDTO card) {
        if (Objects.isNull(card) || Objects.isNull(card.getCard()) || CollectionUtils.isEmpty(card.getAwakeningRoutes())) {
            return false;
        }
        CardBaseInfoDTO cardDetail = card.getCard();
        boolean rarityFlag = Boolean.TRUE.equals(cardDetail.getDokkanFesFlag()) || Boolean.TRUE.equals(cardDetail.getCarnivalFlag())
                || Boolean.TRUE.equals(CardRarityEnum.isLrCard(cardDetail.getRarity())) && Boolean.FALSE.equals(cardDetail.getFreeCardFlag()) ;
        AwakeningInfoDTO lastAwaken = card.getAwakeningRoutes().getLast();
        boolean awakenFlag = cardDetail.getId().equals(lastAwaken.getAwakedCardId());
        return StringUtils.isNotEmpty(cardDetail.getLeaderSkill()) && awakenFlag && rarityFlag;
    }
}
