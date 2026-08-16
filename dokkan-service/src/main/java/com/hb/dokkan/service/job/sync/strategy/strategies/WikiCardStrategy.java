package com.hb.dokkan.service.job.sync.strategy.strategies;

import com.google.common.collect.Lists;
import com.hb.dokkan.common.constants.TranslationConstants;
import com.hb.dokkan.common.domain.bo.data.WikiCardBO;
import com.hb.dokkan.common.utils.DateUtils;
import com.hb.dokkan.common.domain.dto.data.dokkandb.DokkanDbCardDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiCardDTO;
import com.hb.dokkan.common.domain.po.mysql.cards.CardPO;
import com.hb.dokkan.common.domain.po.mysql.cards.EzaCardPO;
import com.hb.dokkan.common.exception.domain.DokkanBizException;
import com.hb.dokkan.config.data.WikiCardFilter;
import com.hb.dokkan.config.http.HttpPoolProperties;
import com.hb.dokkan.config.thread.DokkanThreadPoolExecutor;
import com.hb.dokkan.service.facade.DokkanDbFacade;
import com.hb.dokkan.service.helper.WikiCardHelper;
import com.hb.dokkan.infrastructure.mysql.cards.DokkanCardRepository;
import com.hb.dokkan.infrastructure.mysql.cards.DokkanEzaCardRepository;
import com.hb.dokkan.service.job.sync.SyncProgressContext;
import com.hb.dokkan.service.job.sync.strategy.WikiInfoStrategy;
import com.hb.dokkan.service.job.sync.strategy.context.WikiContext;
import com.hb.dokkan.service.job.sync.strategy.enums.WikiInfoTypeEnum;
import com.hb.dokkan.service.translation.DokkanTranslationService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Date;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
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
    private DokkanDbFacade dokkanDbFacade;

    @Resource
    private DokkanCardRepository cardRepository;

    @Resource
    private DokkanEzaCardRepository ezaCardRepository;

    @Resource
    private HttpPoolProperties httpPoolProperties;

    @Resource
    private DokkanThreadPoolExecutor dokkanThreadPoolExecutor;

    private Semaphore semaphore;

    @Resource
    private WikiCardHelper wikiCardHelper;

    @Resource
    private WikiCardFilter cardFilter;

    @Resource
    private DokkanTranslationService translationService;


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
            List<DokkanDbCardDTO> catalog = dokkanDbFacade.getRecentCatalog(500);
            if (CollectionUtils.isEmpty(catalog)) {
                throw new DokkanBizException("DokkanDB catalog is empty");
            }
            List<DokkanDbCardDTO> filteredCatalog = catalog.stream()
                    .filter(card -> card.getId() != null && card.getId() < 5000021)
                    .filter(card -> card.getRarity() != null && card.getRarity() > 3)
                    .filter(card -> !cardFilter.getWikiCardBlackList().contains(card.getId()))
                    .toList();
            Map<Long, CardPO> existingCards = cardRepository.batchQueryByCardIds(
                            filteredCatalog.stream().map(DokkanDbCardDTO::getId).toList())
                    .stream().collect(Collectors.toMap(CardPO::getCardId, Function.identity(), (left, right) -> left));
            Map<Long, List<EzaCardPO>> existingEzaCards = ezaCardRepository.batchQueryByCardIds(
                            filteredCatalog.stream().map(DokkanDbCardDTO::getId).toList())
                    .stream().collect(Collectors.groupingBy(EzaCardPO::getCardId));
            filteredCatalog.forEach(source -> {
                CardPO existing = existingCards.get(source.getId());
                if (existing == null) return;
                translationService.registerTrustedTerm(source.getName(), existing.getCardName());
                translationService.registerTrustedTerm(source.getTitle(), existing.getTitle());
            });
            List<Long> cardIds = filteredCatalog.stream()
                    .filter(card -> isNewOrUpdated(card, existingCards.get(card.getId()), existingEzaCards.get(card.getId())))
                    .map(DokkanDbCardDTO::getId)
                    .distinct()
                    .toList();
            SyncProgressContext.update(12, "抓取外部数据", "发现 " + cardIds.size() + " 个候选 cardId");
            log.info("DokkanDB incremental catalog checked, catalogSize:{}, syncCandidates:{}",
                    filteredCatalog.size(), cardIds.size());
            if (cardIds.isEmpty()) return Lists.newArrayList();

            HttpPoolProperties.Concurrency concurrencyConfig = httpPoolProperties.getConcurrency();
            int batchSize = Math.min(concurrencyConfig.getBatchSize(), 50);
            List<WikiCardDTO> allResult = Lists.newArrayList();
            List<Long> errorIds = new CopyOnWriteArrayList<>();
            for (int i = 0; i < cardIds.size(); i+= batchSize) {
                int endIndex = Math.min(i + batchSize, cardIds.size());

                List<Long> batchCardIds = cardIds.subList(i, endIndex);

                log.info("处理器:{} 批, 数量:{}", (i / batchSize + 1), batchCardIds.size());

                    List<CompletableFuture<WikiCardDTO>> completableFutures = batchCardIds.stream()
                        // CompletableFuture进行并行编排指定自定义的线程池
                        .map(cardId -> CompletableFuture
                                .supplyAsync(() -> {
                                    boolean acquired = false;
                                    try{
                                        semaphore.acquire();
                                        acquired = true;
                                        return dokkanDbFacade.getCard(cardId);
                                    }catch (Exception e) {
                                        log.error("获取卡片异常 cardId:{}", cardId, e);
                                        errorIds.add(cardId);
                                        return null;
                                    }finally {
                                        if (acquired) {
                                            semaphore.release();
                                        }
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
                int processed = endIndex;
                int percent = 12 + Math.round(processed * 48f / Math.max(cardIds.size(), 1));
                SyncProgressContext.update(percent, "抓取外部数据",
                        "已处理 " + processed + "/" + cardIds.size() + "，有效卡片 " + allResult.size());
                log.info("第 {} 批处理完成 获取到{}个有效卡片", (i / batchSize + 1), batchCardIds.size());

            }
            if (!errorIds.isEmpty()) log.error("DokkanDB card fetch failed, count:{}, cardIds:{}", errorIds.size(), errorIds);
            log.info("DokkanDB incremental fetch completed, cards:{}", allResult.size());
            return allResult;


        } catch (Exception e) {
            log.error("WikiCardInfoStrategy#getWikiCards error",e);
            return Lists.newArrayList();
        }
    }

    /**
     * 判断目录卡片是否需要增量同步。
     *
     * @param source       DokkanDB 目录卡片
     * @param existing     已有基础卡片
     * @param existingEzas 已有极限卡片列表
     * @return 是否需要同步
     */
    private boolean isNewOrUpdated(DokkanDbCardDTO source, CardPO existing, List<EzaCardPO> existingEzas) {
        if (existing == null) return true;
        if (hasMissingEzaData(source, existingEzas)) return true;
        Date sourceUpdate = DateUtils.parseDokkanDbDate(source.getOpenAtUpdate());
        return sourceUpdate != null && (existing.getUpdateTime() == null || sourceUpdate.after(existing.getUpdateTime()));
    }

    /**
     * 判断已有数据是否缺少当前目录暴露的极限或超极限阶段。
     *
     * @param source       DokkanDB 目录卡片
     * @param existingEzas 已有极限卡片列表
     * @return 是否缺少极限数据
     */
    private boolean hasMissingEzaData(DokkanDbCardDTO source, List<EzaCardPO> existingEzas) {
        if (source.getStep() == null || source.getStep() <= 0) {
            return false;
        }
        if (CollectionUtils.isEmpty(existingEzas)) {
            return true;
        }
        List<Integer> existingSteps = existingEzas.stream()
                .map(EzaCardPO::getStep)
                .filter(Objects::nonNull)
                .toList();
        Integer maxExistingStep = existingSteps.stream().max(Integer::compareTo).orElse(null);
        if (Objects.isNull(maxExistingStep) || source.getStep() > maxExistingStep
                || !existingSteps.contains(source.getStep())) {
            return true;
        }
        if (source.getStep() >= TranslationConstants.EZA_PRE_STEP_THRESHOLD
                && source.getStepPre() != null
                && !existingSteps.contains(source.getStepPre())) {
            return true;
        }
        Date sourceEzaPublishTime = latestSourceEzaPublishTime(source);
        Date existingEzaPublishTime = existingEzas.stream()
                .map(EzaCardPO::getPublishTime)
                .filter(Objects::nonNull)
                .max(Date::compareTo)
                .orElse(null);
        return sourceEzaPublishTime != null && (existingEzaPublishTime == null
                || sourceEzaPublishTime.after(existingEzaPublishTime));
    }

    /**
     * 获取目录卡片暴露的最新 EZA 发布时间。
     *
     * @param source DokkanDB 目录卡片
     * @return 最新 EZA 发布时间，不存在时返回 null
     */
    private Date latestSourceEzaPublishTime(DokkanDbCardDTO source) {
        Date awakeningEzaTime = CollectionUtils.isEmpty(source.getAwakeningData()) ? null
                : source.getAwakeningData().stream()
                .map(DokkanDbCardDTO.AwakeningDTO::getOpenAtEza)
                .map(DateUtils::parseDokkanDbDate)
                .filter(Objects::nonNull)
                .max(Date::compareTo)
                .orElse(null);
        return DateUtils.latestDate(awakeningEzaTime, DateUtils.parseDokkanDbDate(source.getOpenAtUpdate()));
    }

    @PostConstruct
    public void preLoad() {
        semaphore = new Semaphore(httpPoolProperties.getConcurrency().getSemaphorePermits());
    }

}
