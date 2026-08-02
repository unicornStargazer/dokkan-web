package com.hb.dokkan.service.job.sync;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.IService;
import com.google.common.collect.Maps;
import com.hb.dokkan.common.constants.ExceptionErrorCode;
import com.hb.dokkan.common.domain.bo.data.WikiCardBO;
import com.hb.dokkan.common.domain.dto.data.cards.CardBaseInfoDTO;
import com.hb.dokkan.common.domain.dto.data.cards.EzaCardInfoDTO;
import com.hb.dokkan.common.domain.dto.data.cards.SkillDTO;
import com.hb.dokkan.common.domain.dto.data.cards.SpecialAttackDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiCategoryDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiCardDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiLinkDTO;
import com.hb.dokkan.common.domain.po.es.cards.CardEsPO;
import com.hb.dokkan.common.domain.po.mysql.cards.CardPO;
import com.hb.dokkan.common.domain.po.mysql.cards.EzaCardPO;
import com.hb.dokkan.common.domain.po.mysql.cards.SkillPO;
import com.hb.dokkan.common.domain.po.mysql.cards.SpecialPO;
import com.hb.dokkan.common.domain.po.mysql.base.BasePO;
import com.hb.dokkan.common.domain.po.mysql.category.DokkanCategoryPO;
import com.hb.dokkan.common.domain.po.mysql.link.DokkanLinkPO;
import com.hb.dokkan.common.exception.domain.DokkanBizException;
import com.hb.dokkan.common.utils.TranslationUtils;
import com.hb.dokkan.config.thread.DokkanThreadPoolExecutor;
import com.hb.dokkan.infrastructure.es.card.mapper.DokkanEsCardMapper;
import com.hb.dokkan.infrastructure.mysql.cards.DokkanCardRepository;
import com.hb.dokkan.infrastructure.mysql.cards.DokkanEzaCardRepository;
import com.hb.dokkan.infrastructure.mysql.cards.DokkanSkillRepository;
import com.hb.dokkan.infrastructure.mysql.cards.DokkanSpecialRepository;
import com.hb.dokkan.infrastructure.mysql.categories.DokkanCategoryRepository;
import com.hb.dokkan.infrastructure.mysql.links.DokkanLinkRepository;
import com.hb.dokkan.service.convert.DokkanSyncConvert;
import com.hb.dokkan.service.helper.EsCardSyncHelper;
import com.hb.dokkan.service.helper.WikiCardHelper;
import com.hb.dokkan.service.facade.WikiFacade;
import com.hb.dokkan.service.job.sync.factory.FixDataStrategyFactory;
import com.hb.dokkan.service.job.sync.factory.WikiInfoStrategyFactory;
import com.hb.dokkan.service.job.sync.strategy.FixDataStrategy;
import com.hb.dokkan.service.job.sync.strategy.WikiInfoStrategy;
import com.hb.dokkan.service.job.sync.strategy.context.WikiContext;
import com.hb.dokkan.service.job.sync.strategy.enums.WikiInfoTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.dromara.easyes.core.conditions.select.LambdaEsQueryWrapper;
import org.dromara.easyes.core.kernel.EsWrappers;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.hb.dokkan.common.constants.DokkanConstants.*;

/**
 * @Description 同步数据服务
 * @Author stargazer
 * @Date 2025/6/2 0:13
 **/
@Slf4j
@Component
public class SyncDataService{
    @Resource
    private WikiInfoStrategyFactory strategyFactory;

    @Resource
    private DokkanCardRepository cardRepository;

    @Resource
    private DokkanEzaCardRepository ezaCardRepository;

    @Resource
    private DokkanSkillRepository skillRepository;

    @Resource
    private DokkanSpecialRepository specialRepository;

    @Resource
    private DokkanCategoryRepository categoryRepository;

    @Resource
    private DokkanLinkRepository linkRepository;

    @Resource
    private DokkanSyncConvert convert;

    @Resource(name = "defaultTransactionTemplate")
    private TransactionTemplate transactionTemplate;

    @Resource
    private DokkanThreadPoolExecutor dokkanThreadPoolExecutor;

    @Resource
    private DokkanEsCardMapper esCardMapper;

    @Resource
    private EsCardSyncHelper esCardSyncHelper;

    @Resource
    private FixDataStrategyFactory fixDataStrategyFactory;

    @Resource
    private WikiFacade wikiFacade;

    @Resource
    private WikiCardHelper wikiCardHelper;


    /**
     * 初始化卡片数据
     */
    public void initCard() {
        log.info("card incremental sync started");
        WikiInfoStrategy strategy = strategyFactory.getWikiStrategy(WikiInfoTypeEnum.CARD);
        WikiContext context = new WikiContext();
        strategy.execute(context);
        WikiCardBO cardData = context.getCardData();
        if (Objects.isNull(cardData)) {
            log.error("初始化失败，获取卡片为空");
            return;
        }
        SyncProgressContext.update(62, "整理卡片数据", "外部数据抓取完成，正在去重并转换");
        persistCardData(cardData);
        log.info("初始化完成");
    }

    /**
     * Synchronizes exact card IDs without applying the automatic crawler filters.
     */
    public int syncCardsByIds(List<Long> cardIds) {
        List<Long> distinctCardIds = cardIds.stream()
                .filter(Objects::nonNull)
                .filter(cardId -> cardId > 0)
                .distinct()
                .toList();
        if (CollectionUtils.isEmpty(distinctCardIds) || distinctCardIds.size() > 100) {
            throw new DokkanBizException(ExceptionErrorCode.QUERY_PARAM_ERROR);
        }

        log.info("manual card sync started, requestedCardIds:{}", distinctCardIds);
        SyncProgressContext.update(12, "抓取指定卡片", "正在抓取 " + distinctCardIds.size() + " 个 cardId");

        List<WikiCardDTO> wikiCards = wikiFacade.getWikiCardList(
                        distinctCardIds.stream().map(String::valueOf).toList())
                .stream()
                .map(TranslationUtils::toSimpleChinese)
                .filter(Objects::nonNull)
                .toList();
        if (CollectionUtils.isEmpty(wikiCards)) {
            throw new DokkanBizException(ExceptionErrorCode.GET_WIKI_INFO_ERROR);
        }

        SyncProgressContext.update(48, "整理卡片数据", "已获取 " + wikiCards.size() + " 个有效卡片");

        WikiCardBO cardData = new WikiCardBO();
        wikiCardHelper.buildData(cardData, wikiCards);
        return persistCardData(cardData);
    }

    private int persistCardData(WikiCardBO cardData) {
        if (Objects.isNull(cardData) || CollectionUtils.isEmpty(cardData.getCardBaseData())) {
            throw new DokkanBizException(ExceptionErrorCode.GET_WIKI_INFO_ERROR);
        }
        log.info("persisting card data, cards:{}, skills:{}, eza:{}, specials:{}",
                cardData.getCardBaseData().size(),
                CollectionUtils.isEmpty(cardData.getDownPullSkills()) ? 0 : cardData.getDownPullSkills().size(),
                CollectionUtils.isEmpty(cardData.getEzaCardInfos()) ? 0 : cardData.getEzaCardInfos().size(),
                CollectionUtils.isEmpty(cardData.getSpecialAttacks()) ? 0 : cardData.getSpecialAttacks().size());
        SyncProgressContext.update(65, "写入 MySQL", "正在写入卡片基础数据及关联技能");
        Boolean mysqlSyncSucceeded = transactionTemplate.execute(status -> {
            try {
                insertCardBaseInfo(cardData.getCardBaseData());
                insertDownPullSkillInfo(cardData.getDownPullSkills());
                insertEzaCardInfo(cardData.getEzaCardInfos());
                insertSpecialInfo(cardData.getSpecialAttacks());
                return true;
            } catch (Exception e) {
                log.error("SyncDataService#initCard error :{}", e.getMessage(), e);
                status.setRollbackOnly();
                return false;
            }
        });
        if (!Boolean.TRUE.equals(mysqlSyncSucceeded)) {
            throw new DokkanBizException(ExceptionErrorCode.INSERT_PARAM_ERROR);
        }
        SyncProgressContext.update(82, "同步 Elasticsearch", "MySQL 写入完成，正在更新 ES 文档");
        List<Long> syncedCardIds = cardData.getCardBaseData().stream()
                .map(CardBaseInfoDTO::getCardId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (!CollectionUtils.isEmpty(syncedCardIds)) {
            syncEsCardsByCardIds(syncedCardIds);
        }
        SyncProgressContext.update(96, "同步头像", "ES 更新完成，头像下载与对象存储已处理");
        return syncedCardIds.size();
    }

    /**
     * 初始化分类数据
     */
    public void initCategories() {
        WikiInfoStrategy strategy = strategyFactory.getWikiStrategy(WikiInfoTypeEnum.CATEGORY);
        WikiContext context = new WikiContext();
        strategy.execute(context);
        List<WikiCategoryDTO> categoryData = context.getCategoryData();
        if (CollectionUtils.isEmpty(categoryData)) {
            log.error("初始化失败，获取分类为空");
            return;
        }
        transactionTemplate.execute(status -> {
            try {
                List<WikiCategoryDTO> data = distinctList(categoryData);
                categoryRepository.saveBatch(convert.convertToCategoryPO(data));
                log.info("初始化完成，分类数据{}条", data.size());
            } catch (Exception e) {
                log.error("SyncDataService#initCategories error :{}", e.getMessage(), e);
                status.setRollbackOnly();
            }
            return null;
        });
    }

    /**
     * 初始化链接数据
     */
    public void initLinks() {
        WikiInfoStrategy strategy = strategyFactory.getWikiStrategy(WikiInfoTypeEnum.LINK);
        WikiContext context = new WikiContext();
        strategy.execute(context);
        List<WikiLinkDTO> linkData = context.getLinkData();
        if (CollectionUtils.isEmpty(linkData)) {
            log.error("初始化失败，获取链接为空");
            return;
        }
        transactionTemplate.execute(status -> {
            try {
                List<WikiLinkDTO> data = distinctList(linkData);
                linkRepository.saveBatch(convert.convertToLinkPO(data));
                log.info("初始化完成，链接数据{}条", data.size());
            } catch (Exception e) {
                log.error("SyncDataService#initLinks error :{}", e.getMessage(), e);
                status.setRollbackOnly();
            }
            return null;
        });
    }

    /**
     * 同步es卡片数据
     */
    public void syncEsCardData() {
        log.info("full ES card sync started");
        SyncProgressContext.update(12, "准备 ES 索引", "正在检查并创建卡片索引");
        Boolean createdIndex = esCardMapper.createIndex();
        if (!createdIndex) {
            log.error("创建es索引失败 indexName:{}", DokkanEsCardMapper.INDEX_NAME);
            throw new DokkanBizException(ExceptionErrorCode.CREATE_INDEX_ERROR);
        }
        Map<String, List<?>> dataMap = Maps.newHashMap();
        // 并行查询card数据
        Future<List<CardPO>> cardFuture = dokkanThreadPoolExecutor.submit(() -> cardRepository.list());
        Future<List<EzaCardPO>> ezaCardFuture = dokkanThreadPoolExecutor.submit(() -> ezaCardRepository.list());
        Future<List<SpecialPO>> specialFuture = dokkanThreadPoolExecutor.submit(() -> specialRepository.list());
        Future<List<SkillPO>> skillFuture = dokkanThreadPoolExecutor.submit(() -> skillRepository.list());
        Future<List<DokkanLinkPO>> linkFuture = dokkanThreadPoolExecutor.submit(() -> linkRepository.list());
        Future<List<DokkanCategoryPO>> categoryFuture = dokkanThreadPoolExecutor.submit(() -> categoryRepository.list());
        try {
            List<CardPO> cardPOS = cardFuture.get();
            List<EzaCardPO> ezaCardPOS = ezaCardFuture.get();
            List<SpecialPO> specialPOS = specialFuture.get();
            List<SkillPO> skillPOS = skillFuture.get();
            List<DokkanLinkPO> linkPOS = linkFuture.get();
            List<DokkanCategoryPO> categoryPOS = categoryFuture.get();
            dataMap.put(CARD, cardPOS);
            dataMap.put(EZA_CARD, ezaCardPOS);
            dataMap.put(SPECIAL, specialPOS);
            dataMap.put(SKILL, skillPOS);
            dataMap.put(LINK, linkPOS);
            dataMap.put(CATEGORY, categoryPOS);
            SyncProgressContext.update(45, "读取 MySQL", "关联数据读取完成，共 " + cardPOS.size() + " 张卡片");
        } catch (Exception e) {
            log.error("query db data error :{}", e.getMessage(), e);
            throw new DokkanBizException("query db data error :" + e.getMessage());
        }
        List<CardEsPO> esCards = esCardSyncHelper.buildEsCardPO(dataMap);
        SyncProgressContext.update(72, "构建 ES 文档", "已构建 "
                + (CollectionUtils.isEmpty(esCards) ? 0 : esCards.size()) + " 个卡片文档");
        if (CollectionUtils.isEmpty(esCards)) {
            log.error("构建es卡片索引失败");
            return;
        }
        transactionTemplate.execute(status -> {
            try {
                Integer insetCnt = esCardMapper.insertBatch(esCards);
                log.info("同步es卡片索引成功,insetCnt:{}", insetCnt);
            } catch (Exception e) {
                log.error("SyncDataService#syncEsCardData error :{}", e.getMessage(), e);
                status.setRollbackOnly();
            }
            return null;
        });
    }


    /**
     *  根据cardId同步es卡片数据
     * @param cardIds cardId集合
     * @return 同步数量
     */
    private int syncEsCardsByCardIds(List<Long> cardIds) {
        List<Long> distinctCardIds = cardIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(distinctCardIds)) {
            throw new DokkanBizException(ExceptionErrorCode.QUERY_PARAM_ERROR);
        }

        if (!esCardMapper.existsIndex(DokkanEsCardMapper.INDEX_NAME) && !esCardMapper.createIndex()) {
            log.error("create ES index failed, indexName:{}", DokkanEsCardMapper.INDEX_NAME);
            throw new DokkanBizException(ExceptionErrorCode.CREATE_INDEX_ERROR);
        }

        Map<String, List<?>> dataMap = Maps.newHashMap();
        try {
            dataMap.put(CARD, cardRepository.batchQueryByCardIds(distinctCardIds));
            dataMap.put(EZA_CARD, ezaCardRepository.list());
            dataMap.put(SPECIAL, specialRepository.list());
            dataMap.put(SKILL, skillRepository.list());
            dataMap.put(LINK, linkRepository.list());
            dataMap.put(CATEGORY, categoryRepository.list());
        } catch (Exception e) {
            log.error("query incremental ES sync data failed", e);
            throw new DokkanBizException(ExceptionErrorCode.QUERY_PARAM_ERROR, e);
        }

        // Build before removing old documents so a build failure does not erase existing ES data.
        List<CardEsPO> esCards = esCardSyncHelper.buildEsCardPO(dataMap);
        LambdaEsQueryWrapper<CardEsPO> deleteWrapper = EsWrappers.lambdaQuery(CardEsPO.class);
        deleteWrapper.in(CardEsPO::getCardId, distinctCardIds);
        esCardMapper.delete(deleteWrapper);

        if (CollectionUtils.isEmpty(esCards)) {
            log.info("incremental ES sync completed with no MySQL cards, deletedCardIds:{}", distinctCardIds);
            return 0;
        }
        Integer insertCount = esCardMapper.insertBatch(esCards);
        int syncedCount = insertCount == null ? 0 : insertCount;
        log.info("incremental ES card sync completed, requestedCardIds:{}, syncedCount:{}", distinctCardIds, syncedCount);
        return syncedCount;
    }

    private void insertSpecialInfo(List<SpecialAttackDTO> specialAttacks) {
        if (CollectionUtils.isEmpty(specialAttacks)) {
            return;
        }
        List<SpecialAttackDTO> distinctList = distinctByKey(specialAttacks, SpecialAttackDTO::getSpecialId);
        List<SpecialPO> specialPOS = convert.wikiSpecial2POList(distinctList);
        Map<Long, String> existingIds = specialRepository.batchQueryBySpecialIds(
                        specialPOS.stream().map(SpecialPO::getSpecialId).toList())
                .stream().collect(Collectors.toMap(SpecialPO::getSpecialId, SpecialPO::getId, (oldValue, newValue) -> oldValue));
        specialPOS.forEach(special -> special.setId(existingIds.get(special.getSpecialId())));
        saveOrUpdateByKnownId(specialRepository, specialPOS, 1000);
    }

    private void insertEzaCardInfo(List<EzaCardInfoDTO> ezaCardInfos) {
        if (CollectionUtils.isEmpty(ezaCardInfos)) {
            return;
        }
        List<EzaCardInfoDTO> distinctList = distinctByKey(ezaCardInfos, this::ezaCardKey);
        List<EzaCardPO> ezaCardPOS = convert.wikiEza2POList(distinctList);
        Map<String, String> existingIds = ezaCardRepository.batchQueryByCardIds(
                        ezaCardPOS.stream().map(EzaCardPO::getCardId).distinct().toList())
                .stream().collect(Collectors.toMap(this::ezaCardKey, EzaCardPO::getId, (oldValue, newValue) -> oldValue));
        ezaCardPOS.forEach(ezaCard -> ezaCard.setId(existingIds.get(ezaCardKey(ezaCard))));
        saveOrUpdateByKnownId(ezaCardRepository, ezaCardPOS, 500);
    }

    private void insertDownPullSkillInfo(List<SkillDTO> downPullSkills) {
        List<SkillDTO> distinctList = distinctByKey(downPullSkills, SkillDTO::getSkillId);
        List<SkillPO> skillPOS = convert.wikiSkill2POList(distinctList);
        if (CollectionUtils.isEmpty(skillPOS)) {
            return;
        }
        Map<String, String> existingIds = skillRepository.batchQueryBySkillIds(
                        skillPOS.stream().map(SkillPO::getSkillId).toList())
                .stream().collect(Collectors.toMap(SkillPO::getSkillId, SkillPO::getId, (oldValue, newValue) -> oldValue));
        skillPOS.forEach(skill -> skill.setId(existingIds.get(skill.getSkillId())));
        saveOrUpdateByKnownId(skillRepository, skillPOS, 500);
    }

    private void insertCardBaseInfo(List<CardBaseInfoDTO> cards) {

        List<CardBaseInfoDTO> distinctedList = distinctByKey(cards, CardBaseInfoDTO::getCardId);
        List<CardPO> cardModel = convert.wikiCard2POList(distinctedList);
        checkParam(cardModel);
        Map<Long, String> existingIds = cardRepository.batchQueryByCardIds(
                        cardModel.stream().map(CardPO::getCardId).toList())
                .stream().collect(Collectors.toMap(CardPO::getCardId, CardPO::getId, (oldValue, newValue) -> oldValue));
        cardModel.forEach(card -> card.setId(existingIds.get(card.getCardId())));
        saveOrUpdateByKnownId(cardRepository, cardModel, 500);
    }

    private <T extends BasePO> void saveOrUpdateByKnownId(IService<T> repository, List<T> entities, int batchSize) {
        if (CollectionUtils.isEmpty(entities)) {
            return;
        }
        List<T> insertEntities = entities.stream().filter(entity -> Objects.isNull(entity.getId())).toList();
        List<T> updateEntities = entities.stream().filter(entity -> Objects.nonNull(entity.getId())).toList();
        if (!CollectionUtils.isEmpty(insertEntities)) {
            repository.saveBatch(insertEntities, batchSize);
        }
        if (!CollectionUtils.isEmpty(updateEntities)) {
            repository.updateBatchById(updateEntities, batchSize);
        }
    }

    private String ezaCardKey(EzaCardInfoDTO ezaCard) {
        return ezaCard.getCardId() + ":" + ezaCard.getStep();
    }

    private String ezaCardKey(EzaCardPO ezaCard) {
        return ezaCard.getCardId() + ":" + ezaCard.getStep();
    }

    private <T, K> List<T> distinctByKey(List<T> dataList, Function<T, K> keyExtractor) {
        if (CollectionUtils.isEmpty(dataList)) {
            return Collections.emptyList();
        }
        List<T> distinctData = new ArrayList<>(dataList.stream()
                .filter(Objects::nonNull)
                .filter(data -> keyExtractor.apply(data) != null)
                .collect(Collectors.toMap(keyExtractor, Function.identity(), (oldValue, newValue) -> newValue, LinkedHashMap::new))
                .values());
        if (distinctData.size() < dataList.size()) {
            log.warn("discarded duplicate or invalid sync records, sourceCount:{}, distinctCount:{}", dataList.size(), distinctData.size());
        }
        return distinctData;
    }

    private <T> List<T> distinctList(List<T> dataList) {
        List<T> distinctList = dataList.stream().distinct().toList();
        return distinctList;
    }

    private void checkParam(List<CardPO> cardPOS) {
        cardPOS.forEach(card -> {
            if (StringUtils.isAnyBlank(card.getTitle(), card.getCardName()) || !ObjectUtils.allNotNull(
                    card.getCardId(), card.getHpValue(), card.getAtkValue(), card.getDefValue())) {
                log.error("param error,card:{}", JSON.toJSONString(card));
                throw new DokkanBizException(ExceptionErrorCode.INSERT_PARAM_ERROR);
            }
        });
    }

    /**
     * 修复数据库数据
     */
    public void fixDbData(Integer fixType) {
        FixDataStrategy fixDataStrategy = fixDataStrategyFactory.getFixDataStrategy(fixType);
        if (Objects.isNull(fixDataStrategy)) {
            log.error("fixDataStrategy is null,fixType:{}", fixType);
            throw new DokkanBizException(ExceptionErrorCode.HAS_NO_STRATEGY);
        }
        fixDataStrategy.fixData();
    }
}
