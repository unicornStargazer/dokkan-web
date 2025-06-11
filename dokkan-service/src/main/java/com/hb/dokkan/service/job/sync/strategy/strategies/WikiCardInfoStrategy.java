package com.hb.dokkan.service.job.sync.strategy.strategies;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.hb.dokkan.common.utils.JsonUtils;
import com.hb.dokkan.common.utils.TranslationUtils;
import com.hb.dokkan.service.domain.CardBaseInfoDTO;
import com.hb.dokkan.service.domain.sync.AwakeningInfoDTO;
import com.hb.dokkan.service.domain.sync.SyncCardDTO;
import com.hb.dokkan.service.domain.sync.WikiCardDTO;
import com.hb.dokkan.service.enums.CardRarityEnum;
import com.hb.dokkan.service.facade.WikiFacade;
import com.hb.dokkan.service.job.sync.strategy.WikiInfoStrategy;
import com.hb.dokkan.service.job.sync.strategy.context.WikiContext;
import com.hb.dokkan.service.job.sync.strategy.enums.WikiInfoTypeEnum;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;

/**
 * @Description xxxxx
 * @Author stargazer
 * @Date 2025/6/2 21:05
 **/
@Component
@Slf4j
public class WikiCardInfoStrategy implements WikiInfoStrategy {

    @Resource
    private WikiFacade wikiFacade;

    private File htmlFile;

    @Resource
    private VirtualThreadTaskExecutor dokkanVirtualThreadExecutor;

    private static final String filePath = "E:/IDEA Project/dokkan-web/dokkan-starter/src/main/resources/card/cards.json";

    @PostConstruct
    public void preLoad() {
        htmlFile = new File("C:/Users/lenovo/Desktop/html/cards.html");
    }


    @Override
    public boolean isMatched(WikiInfoTypeEnum type) {
        return WikiInfoTypeEnum.CARD.equals(type);
    }

    @SneakyThrows
    @Override
    public void execute(WikiContext context) {
        URL url = this.getClass().getResource("card/cards.json");
        if (Objects.nonNull(url)) {
            String json = (String) url.getContent();
            List<WikiCardDTO> wikiCardDTOS = JsonUtils.json2List(json, WikiCardDTO.class);
            context.setWikiCards(wikiCardDTOS);
            return;
        }
        List<WikiCardDTO> wikiCards = getWikiCards();
        context.setWikiCards(wikiCards);
        List<Map<String, Object>> jsonList = JsonUtils.json2ListMap(JSON.toJSONString(wikiCards), String.class, Object.class);
        JsonUtils.writeJson2File(jsonList,filePath);
    }

    private List<WikiCardDTO> getWikiCards() {
        if (!htmlFile.exists()) {
            throw new RuntimeException("html file is null");
        }
        try {
            Document doc = Jsoup.parse(htmlFile, "utf-8");
            Element cardsElement = doc.selectFirst("cards");
            if (Objects.isNull(cardsElement)) {
                return Lists.newArrayList();
            }
            String cardsJson = cardsElement.attr("v-bind:cardsjson");
            List<SyncCardDTO> syncCardsJobs = JsonUtils.json2List(cardsJson, SyncCardDTO.class);
            List<Long> cardIds = syncCardsJobs.stream()
                    .filter(card -> card.getId() < 5000021 && card.getRarity() > 3)
                    .map(SyncCardDTO::getId)
                    .toList();
            List<CompletableFuture<WikiCardDTO>> completableFutures = cardIds.stream()
                    // CompletableFuture进行并行编排指定自定义的线程池
                    .map(cardId -> CompletableFuture
                            .supplyAsync(() -> {
                                final Semaphore semaphore = new Semaphore(50);
                                try {
                                    semaphore.acquire();
                                    WikiCardDTO wikiCardDTO = wikiFacade.getWikiCard(String.valueOf(cardId));
                                    if (filterCard(wikiCardDTO)) {
                                        WikiCardDTO insertCard = TranslationUtils.toSimpleChinese(wikiCardDTO);
                                        // 日志可以保留，但要注意日志本身也可能成为瓶颈
                                        log.info("，card:{}", JSON.toJSONString(Objects.requireNonNull(insertCard).getCard()));
                                        return insertCard;
                                    }
                                    return null;
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }finally {
                                    semaphore.release();
                                }
                            }, dokkanVirtualThreadExecutor)
                            .handle((result, ex) -> {
                                if (ex != null) {
                                    // 如果任务执行过程中发生任何异常（如网络超时）
                                    log.error("获取卡片信息时发生异常, cardId: {}", cardId, ex);
                                    return null; // 将异常转换为null结果，避免中断整个流程
                                }
                                if (result != null) {
                                    log.info("请求成功, cardId: {}", cardId);
                                }
                                return result;
                            }))
                    .toList();
            return CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> completableFutures.stream().map(CompletableFuture::join).filter(Objects::nonNull).toList())
                    .join();
        } catch (Exception e) {
            log.error("WikiCardInfoStrategy#getWikiCards error");
            return Lists.newArrayList();
        }
    }

    public boolean filterCard(WikiCardDTO card) {
        if (Objects.isNull(card) || Objects.isNull(card.getCard()) || CollectionUtils.isEmpty(card.getAwakeningRoutes())) {
            return false;
        }

        CardBaseInfoDTO cardDetail = card.getCard();
        if (specialCardUnAccess(cardDetail)) {
            return false;
        }
        if (specialCardAccess(cardDetail)) {
            return true;
        }
        boolean rarityFlag = Boolean.TRUE.equals(cardDetail.getDokkanFesFlag()) || Boolean.TRUE.equals(cardDetail.getCarnivalFlag())
                || Boolean.TRUE.equals(CardRarityEnum.isLrCard(cardDetail.getRarity())) && Boolean.FALSE.equals(cardDetail.getFreeCardFlag()) ;
        AwakeningInfoDTO lastAwaken = card.getAwakeningRoutes().getLast();
        boolean awakenFlag = cardDetail.getId().equals(lastAwaken.getAwakedCardId())
                && Objects.nonNull(cardDetail.getCost()) && cardDetail.getCost() >= 40;
        return awakenFlag && rarityFlag;
    }

    private boolean specialCardAccess(CardBaseInfoDTO cardDetail) {
        return cardDetail.getId() == 1003310;
    }

    private boolean specialCardUnAccess(CardBaseInfoDTO cardDetail) {
        return cardDetail.getId() == 4017791 || cardDetail.getId() == 4030811;
    }
}
