package com.hb.dokkan.service.job.sync.strategy.strategies;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.hb.dokkan.common.domain.bo.data.WikiCardBO;
import com.hb.dokkan.common.domain.dto.data.wiki.AwakeningInfoDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.CardInfoSyncCardDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiCardBaseInfoDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiCardDTO;
import com.hb.dokkan.common.enums.CardAwakeningTypeEnum;
import com.hb.dokkan.common.enums.CardRarityEnum;
import com.hb.dokkan.common.exception.domain.DokkanBizException;
import com.hb.dokkan.common.utils.JsonUtils;
import com.hb.dokkan.common.utils.TranslationUtils;
import com.hb.dokkan.config.data.WikiCardFilter;
import com.hb.dokkan.config.http.HttpPoolProperties;
import com.hb.dokkan.config.thread.DokkanThreadPoolExecutor;
import com.hb.dokkan.service.facade.WikiFacade;
import com.hb.dokkan.service.helper.WikiCardHelper;
import com.hb.dokkan.service.job.sync.strategy.WikiInfoStrategy;
import com.hb.dokkan.service.job.sync.strategy.context.WikiContext;
import com.hb.dokkan.service.job.sync.strategy.enums.WikiInfoTypeEnum;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;

/**
 * @Description 卡片wiki
 * @Author stargazer
 * @Date 2025/9/10 22:06
 **/
@Slf4j
@Component
public class WikiCardStrategy implements WikiInfoStrategy {

    @Resource
    private WikiFacade wikiFacade;

    @Resource
    private HttpPoolProperties httpPoolProperties;

    @Resource
    private DokkanThreadPoolExecutor dokkanThreadPoolExecutor;

    private Semaphore semaphore;

    @Resource
    private WikiCardHelper wikiCardHelper;

    @Resource
    private WikiCardFilter cardFilter;


    /**
     * 是否匹配
     */
    @Override
    public boolean isMatched(WikiInfoTypeEnum type) {
        return WikiInfoTypeEnum.CARD.equals(type);
    }

    @Override
    public void execute(WikiContext context) {
        List<WikiCardDTO> wikiCards = this.getWikiCards();
        WikiCardBO cardBO = new WikiCardBO();
        context.setWikiCards(wikiCards);
        context.setCardData(cardBO);
        wikiCardHelper.buildData(cardBO, context.getWikiCards());
    }

    private List<WikiCardDTO> getWikiCards() {
        try {
            String html = wikiFacade.getWikiHtml();
            if (StringUtils.isBlank(html)) {
                throw new DokkanBizException("html 获取失败");
            }
            Document doc = Jsoup.parse(html, "utf-8");
            Element cardsElement = doc.selectFirst("cards");
            if (Objects.isNull(cardsElement)) {
                return Lists.newArrayList();
            }
            String cardsJson = cardsElement.attr("v-bind:cardsjson");
            List<CardInfoSyncCardDTO> syncCardsJobs = JsonUtils.json2List(cardsJson, CardInfoSyncCardDTO.class);
            List<Long> cardIds = syncCardsJobs.stream()
                    .filter(card -> card.getId() < 5000021 && card.getRarity() > 3)
                    .map(CardInfoSyncCardDTO::getId)
                    .toList();
            log.info("开始获取卡片信息， 总数量:{}", cardIds.size());

            HttpPoolProperties.Concurrency concurrencyConfig = httpPoolProperties.getConcurrency();
            int batchSize = concurrencyConfig.getBatchSize();
            List<WikiCardDTO> allResult = Lists.newArrayList();
            List<Long> errorIds = Lists.newArrayList();
            List<Long> specialCardIds = Lists.newArrayList();
            for (int i = 0; i < cardIds.size(); i+= batchSize) {
                int endIndex = Math.min(i + batchSize, cardIds.size());

                List<Long> batchCardIds = cardIds.subList(i, endIndex);

                log.info("处理器:{} 批, 数量:{}", (i / batchSize + 1), batchCardIds.size());

                    List<CompletableFuture<WikiCardDTO>> completableFutures = cardIds.stream()
                        // CompletableFuture进行并行编排指定自定义的线程池
                        .map(cardId -> CompletableFuture
                                .supplyAsync(() -> {
                                    try{
                                        semaphore.acquire();
                                        WikiCardDTO wikiCardDTO = wikiFacade.getWikiCard(String.valueOf(cardId));
                                        if (filterCard(wikiCardDTO, specialCardIds)) {
                                            WikiCardDTO insertCard = TranslationUtils.toSimpleChinese(wikiCardDTO);
                                            // 日志可以保留，但要注意日志本身也可能成为瓶颈
                                            log.info("card:{}", JSON.toJSONString(Objects.requireNonNull(insertCard).getCard()));
                                            return insertCard;
                                        }
                                        return null;
                                    }catch (Exception e) {
                                        log.error("获取卡片异常 cardId:{}", cardId, e);
                                        errorIds.add(cardId);
                                        return null;
                                    }finally {
                                        semaphore.release();
                                    }
                                }, dokkanThreadPoolExecutor))
                        .toList();
                List<WikiCardDTO> batchResult = CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0]))
                        .thenApply(v -> completableFutures.stream()
                                .map(CompletableFuture::join)
                                .filter(Objects::nonNull)
                                .toList())
                        .join();
                allResult.addAll(batchResult);
                log.info("第 {} 批处理完成 获取到{}个有效卡片", (i / batchSize + 1), batchCardIds.size());

            }
            CompletableFuture.supplyAsync(()  -> {
                JsonUtils.writeJson2File(JSON.toJSONString(errorIds), "E:/IDEA Project/dokkan-web/dokkan-start/src/main/resources/error/error_card.json");
                JsonUtils.writeJson2File(JSON.toJSONString(specialCardIds), "E:/IDEA Project/dokkan-web/dokkan-start/src/main/resources/error/special_card.json");
                return null;
            });
            log.error("获取卡片失败， 失败数量:{}, 失败卡片:{}", errorIds.size(), errorIds);
            log.info("特殊处理卡， 数量:{}， 卡片:{}", specialCardIds.size(), specialCardIds);
            log.info("获取卡片成功， 总共获取{}个卡片", allResult.size());
            return allResult;


        } catch (Exception e) {
            log.error("WikiCardInfoStrategy#getWikiCards error",e);
            return Lists.newArrayList();
        }
    }

    public boolean filterCard(WikiCardDTO card, List<Long> specialCardIds) {
        if (Objects.isNull(card) || Objects.isNull(card.getCard()) || CollectionUtils.isEmpty(card.getAwakeningRoutes())) {
            return false;
        }

        WikiCardBaseInfoDTO cardDetail = card.getCard();
        if (StringUtils.isAnyBlank(cardDetail.getLeaderSkill(),cardDetail.getPassiveSkillDesc()) || CollectionUtils.isEmpty(card.getCardLinks()) ||
        CollectionUtils.isEmpty(card.getCategories())) {
            return false;
        }
        if (cardFilter.getWikiCardWhiteList().contains(cardDetail.getId())) {
            return true;
        }
        if (cardFilter.getWikiCardBlackList().contains(cardDetail.getId())) {
            return false;
        }
        boolean rarityFlag = Objects.nonNull(cardDetail.getRarity()) && cardDetail.getRarity() > CardRarityEnum.SSR.getRarity();

        boolean awakenFlag = filterAwakenCard(card.getAwakeningRoutes(), cardDetail, specialCardIds);
        return awakenFlag && rarityFlag;
    }

    /**
     * 过滤出是dk觉醒后的卡
     */
    private boolean filterAwakenCard(List<AwakeningInfoDTO> awakeningRoutes, WikiCardBaseInfoDTO card, List<Long> specialCardIds) {
        // 变身后的卡
        if (awakeningRoutes.size() == 1 ) {
            if (BooleanUtils.isTrue(card.getFreeCardFlag()) && CardRarityEnum.LR.getRarity() != card.getRarity()) {
                log.warn("特殊卡，记下来，后续处理 cardId:{}", card.getId());
                specialCardIds.add(card.getId());
                return false;
            }
            return true;
        }
        List<AwakeningInfoDTO> sortedList = awakeningRoutes.stream()
                .filter(awakeningInfoDTO -> CardAwakeningTypeEnum.DOKKAN_AWAKENING.getType().equals(
                        awakeningInfoDTO.getAwakenDetail().getType()))
                .sorted((o1, o2) -> {
                            AwakeningInfoDTO.AwakenDetailDTO awakenDetail1 = o1.getAwakenDetail();
                            AwakeningInfoDTO.AwakenDetailDTO awakenDetail2 = o2.getAwakenDetail();
                            int rarityCompare = Integer.compare(awakenDetail1.getRarity(), awakenDetail2.getRarity());
                            if (rarityCompare != 0) {
                                return rarityCompare;
                            }
                            return awakenDetail2.getOpenAt().compareTo(awakenDetail1.getOpenAt());
                        }
                )
                .toList();
        if (CollectionUtils.isEmpty(sortedList)) {
            return false;
        }
        AwakeningInfoDTO lastCard = sortedList.getLast();
        return lastCard.getAwakedCardId().equals(card.getId());
    }

    @PostConstruct
    public void preLoad() {
        semaphore = new Semaphore(httpPoolProperties.getConcurrency().getSemaphorePermits());
    }

}
