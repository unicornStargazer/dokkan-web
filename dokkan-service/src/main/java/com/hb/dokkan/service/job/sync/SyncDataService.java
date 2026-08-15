package com.hb.dokkan.service.job.sync;

import com.baomidou.mybatisplus.extension.service.IService;
import com.google.common.collect.Maps;
import com.hb.dokkan.common.constants.CardSyncConstants;
import com.hb.dokkan.common.constants.ExceptionErrorCode;
import com.hb.dokkan.common.domain.bo.data.WikiCardBO;
import com.hb.dokkan.common.domain.dto.data.cards.CardBaseInfoAttribute;
import com.hb.dokkan.common.domain.dto.data.cards.CardBaseInfoDTO;
import com.hb.dokkan.common.domain.dto.data.cards.EzaCardInfoDTO;
import com.hb.dokkan.common.domain.dto.data.cards.SkillDTO;
import com.hb.dokkan.common.domain.dto.data.cards.SpecialAttackDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiCardDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiCategoryDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiLinkDTO;
import com.hb.dokkan.common.domain.po.es.cards.CardEsPO;
import com.hb.dokkan.common.domain.po.mysql.base.BasePO;
import com.hb.dokkan.common.domain.po.mysql.cards.CardPO;
import com.hb.dokkan.common.domain.po.mysql.cards.EzaCardPO;
import com.hb.dokkan.common.domain.po.mysql.cards.SkillPO;
import com.hb.dokkan.common.domain.po.mysql.cards.SpecialPO;
import com.hb.dokkan.common.domain.po.mysql.category.DokkanCategoryPO;
import com.hb.dokkan.common.domain.po.mysql.link.DokkanLinkPO;
import com.hb.dokkan.common.exception.domain.DokkanBizException;
import com.hb.dokkan.common.utils.JsonUtils;
import com.hb.dokkan.config.thread.DokkanThreadPoolExecutor;
import com.hb.dokkan.infrastructure.es.card.mapper.DokkanEsCardMapper;
import com.hb.dokkan.infrastructure.mysql.cards.DokkanCardRepository;
import com.hb.dokkan.infrastructure.mysql.cards.DokkanEzaCardRepository;
import com.hb.dokkan.infrastructure.mysql.cards.DokkanSkillRepository;
import com.hb.dokkan.infrastructure.mysql.cards.DokkanSpecialRepository;
import com.hb.dokkan.infrastructure.mysql.categories.DokkanCategoryRepository;
import com.hb.dokkan.infrastructure.mysql.links.DokkanLinkRepository;
import com.hb.dokkan.service.convert.DokkanSyncConvert;
import com.hb.dokkan.service.facade.DokkanDbFacade;
import com.hb.dokkan.service.helper.EsCardSyncHelper;
import com.hb.dokkan.service.helper.WikiCardHelper;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.hb.dokkan.common.constants.DokkanConstants.CARD;
import static com.hb.dokkan.common.constants.DokkanConstants.CATEGORY;
import static com.hb.dokkan.common.constants.DokkanConstants.EZA_CARD;
import static com.hb.dokkan.common.constants.DokkanConstants.LINK;
import static com.hb.dokkan.common.constants.DokkanConstants.SKILL;
import static com.hb.dokkan.common.constants.DokkanConstants.SPECIAL;

/**
 * @Description 同步数据服务
 * @Author stargazer
 * @Date 2025/6/2 0:13
 **/
@Slf4j
@Component
public class SyncDataService {

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
    private DokkanDbFacade dokkanDbFacade;

    @Resource
    private WikiCardHelper wikiCardHelper;

    /**
     * 增量初始化卡片数据。
     */
    public void initCard() {
        log.info("card incremental sync started");
        WikiInfoStrategy strategy = strategyFactory.getWikiStrategy(WikiInfoTypeEnum.CARD);
        WikiContext context = new WikiContext();
        strategy.execute(context);
        WikiCardBO cardData = context.getCardData();
        if (Objects.isNull(cardData) || CollectionUtils.isEmpty(cardData.getCardBaseData())) {
            log.info("DokkanDB incremental sync completed, no new or updated cards");
            return;
        }
        SyncProgressContext.update(CardSyncConstants.SYNC_PROGRESS_ARRANGE_CARD,
                CardSyncConstants.STAGE_ARRANGE_CARD, CardSyncConstants.MESSAGE_FETCH_COMPLETE_ARRANGE);
        persistCardData(cardData, true);
        log.info("card incremental sync completed");
    }

    /**
     * 按指定卡片 ID 同步卡片数据，不使用自动爬虫过滤条件。
     *
     * @param cardIds 卡片 ID 列表
     * @return 成功同步数量
     */
    public int syncCardsByIds(List<Long> cardIds) {
        return syncCardsByIdsInternal(cardIds, true, false);
    }

    /**
     * 按指定卡片 ID 强制重新翻译卡片数据。
     *
     * @param cardId 卡片 ID
     * @return 成功同步数量
     */
    public int retranslateCardById(Long cardId) {
        if (Objects.isNull(cardId) || cardId <= CardSyncConstants.MIN_VALID_CARD_ID) {
            throw new DokkanBizException(ExceptionErrorCode.QUERY_PARAM_ERROR);
        }
        return syncCardsByIdsInternal(List.of(cardId), false, true);
    }

    /**
     * 按指定卡片 ID 同步卡片数据。
     *
     * @param cardIds                 卡片 ID 列表
     * @param preserveLocalizedFields 是否保留已有本地化文案
     * @param forceRetranslate        是否绕过旧翻译缓存重新翻译
     * @return 成功同步数量
     */
    private int syncCardsByIdsInternal(List<Long> cardIds, boolean preserveLocalizedFields, boolean forceRetranslate) {
        if (CollectionUtils.isEmpty(cardIds)) {
            throw new DokkanBizException(ExceptionErrorCode.QUERY_PARAM_ERROR);
        }
        List<Long> distinctCardIds = cardIds.stream()
                .filter(Objects::nonNull)
                .filter(cardId -> cardId > CardSyncConstants.MIN_VALID_CARD_ID)
                .distinct()
                .toList();
        if (CollectionUtils.isEmpty(distinctCardIds)
                || distinctCardIds.size() > CardSyncConstants.MANUAL_SYNC_MAX_CARD_COUNT) {
            throw new DokkanBizException(ExceptionErrorCode.QUERY_PARAM_ERROR);
        }

        log.info("manual card sync started, requestedCardIds:{}, preserveLocalized:{}, forceRetranslate:{}",
                distinctCardIds, preserveLocalizedFields, forceRetranslate);
        SyncProgressContext.update(CardSyncConstants.SYNC_PROGRESS_MANUAL_FETCH_RUNNING,
                CardSyncConstants.STAGE_FETCH_MANUAL_CARD_RUNNING,
                CardSyncConstants.MESSAGE_FETCH_MANUAL_CARD_PREFIX + distinctCardIds.size()
                        + CardSyncConstants.MESSAGE_CARD_ID_SUFFIX);

        // 先按指定 ID 从 DokkanDB 查询原始数据，查不到时直接按业务异常返回。
        List<WikiCardDTO> wikiCards = forceRetranslate ? distinctCardIds.stream()
                .map(cardId -> dokkanDbFacade.getCard(cardId, true))
                .filter(Objects::nonNull)
                .toList() : dokkanDbFacade.getCards(distinctCardIds);
        if (CollectionUtils.isEmpty(wikiCards)) {
            throw new DokkanBizException(ExceptionErrorCode.GET_WIKI_INFO_ERROR);
        }

        SyncProgressContext.update(CardSyncConstants.SYNC_PROGRESS_MANUAL_ARRANGE_CARD,
                CardSyncConstants.STAGE_ARRANGE_CARD,
                CardSyncConstants.MESSAGE_FETCHED_PREFIX + wikiCards.size()
                        + CardSyncConstants.MESSAGE_VALID_CARD_SUFFIX);

        // 将外部数据转换为内部同步 BO 后复用统一持久化链路。
        WikiCardBO cardData = new WikiCardBO();
        wikiCardHelper.buildData(cardData, wikiCards);
        return persistCardData(cardData, preserveLocalizedFields);
    }

    /**
     * 持久化卡片同步数据，并增量刷新对应 ES 文档。
     *
     * @param cardData                卡片同步业务对象
     * @param preserveLocalizedFields 是否保留已有本地化文案
     * @return 同步卡片数量
     */
    private int persistCardData(WikiCardBO cardData, boolean preserveLocalizedFields) {
        if (Objects.isNull(cardData) || CollectionUtils.isEmpty(cardData.getCardBaseData())) {
            throw new DokkanBizException(ExceptionErrorCode.GET_WIKI_INFO_ERROR);
        }
        log.info("persisting card data, cards:{}, skills:{}, eza:{}, specials:{}",
                cardData.getCardBaseData().size(),
                CollectionUtils.isEmpty(cardData.getDownPullSkills())
                        ? 0 : cardData.getDownPullSkills().size(),
                CollectionUtils.isEmpty(cardData.getEzaCardInfos())
                        ? 0 : cardData.getEzaCardInfos().size(),
                CollectionUtils.isEmpty(cardData.getSpecialAttacks())
                        ? 0 : cardData.getSpecialAttacks().size());
        SyncProgressContext.update(CardSyncConstants.SYNC_PROGRESS_MYSQL_WRITE,
                CardSyncConstants.STAGE_WRITE_MYSQL, CardSyncConstants.MESSAGE_WRITE_CARD_DATA);
        Boolean mysqlSyncSucceeded = transactionTemplate.execute(status -> {
            try {
                // MySQL 数据写入需要放在同一个事务内，避免卡片与关联信息不一致。
                insertCardBaseInfo(cardData.getCardBaseData(), preserveLocalizedFields);
                insertDownPullSkillInfo(cardData.getDownPullSkills(), preserveLocalizedFields);
                insertEzaCardInfo(cardData.getEzaCardInfos(), preserveLocalizedFields);
                insertSpecialInfo(cardData.getSpecialAttacks(), preserveLocalizedFields);
                return true;
            } catch (Exception e) {
                log.error("SyncDataService#persistCardData error :{}", e.getMessage(), e);
                status.setRollbackOnly();
                return false;
            }
        });
        if (!Boolean.TRUE.equals(mysqlSyncSucceeded)) {
            throw new DokkanBizException(ExceptionErrorCode.INSERT_PARAM_ERROR);
        }
        SyncProgressContext.update(CardSyncConstants.SYNC_PROGRESS_ES_SYNC,
                CardSyncConstants.STAGE_SYNC_ES, CardSyncConstants.MESSAGE_UPDATE_ES_DOCUMENT);
        List<Long> syncedCardIds = cardData.getCardBaseData().stream()
                .map(CardBaseInfoDTO::getCardId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (!CollectionUtils.isEmpty(syncedCardIds)) {
            syncEsCardsByCardIds(syncedCardIds);
        }
        SyncProgressContext.update(CardSyncConstants.SYNC_PROGRESS_ICON_SYNC,
                CardSyncConstants.STAGE_SYNC_ICON, CardSyncConstants.MESSAGE_ICON_SYNC_HANDLED);
        return syncedCardIds.size();
    }

    /**
     * 初始化分类数据。
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
                List<DokkanCategoryPO> incoming = convert.convertToCategoryPO(data);
                Map<Long, DokkanCategoryPO> existingCategories = categoryRepository.list().stream()
                        .filter(row -> Objects.nonNull(row.getCategoryId()))
                        .collect(Collectors.toMap(DokkanCategoryPO::getCategoryId, Function.identity(),
                                (left, right) -> left));
                List<DokkanCategoryPO> missing = new ArrayList<>();
                List<DokkanCategoryPO> updates = new ArrayList<>();
                for (DokkanCategoryPO row : incoming) {
                    if (Objects.isNull(row.getCategoryId())) {
                        continue;
                    }
                    DokkanCategoryPO existing = existingCategories.get(row.getCategoryId());
                    if (Objects.isNull(existing)) {
                        missing.add(row);
                        continue;
                    }
                    if (StringUtils.isNotBlank(row.getCategoryNameEn())
                            && !Objects.equals(existing.getCategoryNameEn(), row.getCategoryNameEn())) {
                        existing.setCategoryNameEn(row.getCategoryNameEn());
                        updates.add(existing);
                    }
                }
                if (!missing.isEmpty()) {
                    categoryRepository.saveBatch(missing);
                }
                if (!updates.isEmpty()) {
                    categoryRepository.updateBatchById(updates);
                }
                log.info("DokkanDB category sync completed, fetched:{}, inserted:{}, updated:{}", data.size(),
                        missing.size(), updates.size());
            } catch (Exception e) {
                log.error("SyncDataService#initCategories error :{}", e.getMessage(), e);
                status.setRollbackOnly();
            }
            return null;
        });
    }

    /**
     * 初始化链接数据。
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
                List<DokkanLinkPO> incoming = convert.convertToLinkPO(data);
                Set<Long> existingIds = linkRepository.list().stream()
                        .map(DokkanLinkPO::getLinkId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                List<DokkanLinkPO> missing = incoming.stream()
                        .filter(row -> row.getLinkId() != null && !existingIds.contains(row.getLinkId()))
                        .filter(row -> StringUtils.isNoneBlank(row.getLinkName(), row.getLevel1Description(),
                                row.getLevel10Description()))
                        .toList();
                if (!missing.isEmpty()) {
                    linkRepository.saveBatch(missing);
                }
                log.info("DokkanDB link sync completed, fetched:{}, inserted:{}", data.size(), missing.size());
            } catch (Exception e) {
                log.error("SyncDataService#initLinks error :{}", e.getMessage(), e);
                status.setRollbackOnly();
            }
            return null;
        });
    }

    /**
     * 全量同步 ES 卡片数据。
     */
    public void syncEsCardData() {
        log.info("full ES card sync started");
        SyncProgressContext.update(CardSyncConstants.SYNC_PROGRESS_ES_PREPARE,
                CardSyncConstants.STAGE_PREPARE_ES_INDEX, CardSyncConstants.MESSAGE_PREPARE_ES_INDEX);
        Boolean createdIndex = esCardMapper.createIndex();
        if (!createdIndex) {
            log.error("创建es索引失败 indexName:{}", DokkanEsCardMapper.INDEX_NAME);
            throw new DokkanBizException(ExceptionErrorCode.CREATE_INDEX_ERROR);
        }
        Map<String, List<?>> dataMap = Maps.newHashMap();
        // 并行查询 MySQL 关联数据，减少全量 ES 重建等待时间。
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
            SyncProgressContext.update(CardSyncConstants.SYNC_PROGRESS_ES_READ_COMPLETE,
                    CardSyncConstants.STAGE_READ_MYSQL,
                    CardSyncConstants.MESSAGE_MYSQL_READ_COMPLETE_PREFIX + cardPOS.size()
                            + CardSyncConstants.MESSAGE_CARD_COUNT_UNIT_SUFFIX);
        } catch (Exception e) {
            log.error("query db data error :{}", e.getMessage(), e);
            throw new DokkanBizException(CardSyncConstants.QUERY_DB_DATA_ERROR_MESSAGE + CardSyncConstants.KEY_SEPARATOR
                    + e.getMessage());
        }
        List<CardEsPO> esCards = esCardSyncHelper.buildEsCardPO(dataMap);
        SyncProgressContext.update(CardSyncConstants.SYNC_PROGRESS_ES_DOC_BUILD,
                CardSyncConstants.STAGE_BUILD_ES_DOCUMENT,
                CardSyncConstants.MESSAGE_ES_DOCUMENT_BUILT_PREFIX
                        + (CollectionUtils.isEmpty(esCards) ? 0 : esCards.size())
                        + CardSyncConstants.MESSAGE_ES_DOCUMENT_COUNT_SUFFIX);
        if (CollectionUtils.isEmpty(esCards)) {
            log.error("构建es卡片索引失败");
            return;
        }
        transactionTemplate.execute(status -> {
            try {
                Integer insertCnt = esCardMapper.insertBatch(esCards);
                log.info("同步es卡片索引成功,insetCnt:{}", insertCnt);
            } catch (Exception e) {
                log.error("SyncDataService#syncEsCardData error :{}", e.getMessage(), e);
                status.setRollbackOnly();
            }
            return null;
        });
    }

    /**
     * 根据 cardId 增量同步 ES 卡片数据。
     *
     * @param cardIds cardId 集合
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

        // 先构建新 ES 文档，再删除旧文档，避免构建失败导致旧数据被清空。
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

    /**
     * 写入必杀技数据，已有记录保留本地化字段。
     *
     * @param specialAttacks          必杀技同步数据
     * @param preserveLocalizedFields 是否保留已有本地化文案
     */
    private void insertSpecialInfo(List<SpecialAttackDTO> specialAttacks, boolean preserveLocalizedFields) {
        if (CollectionUtils.isEmpty(specialAttacks)) {
            return;
        }
        List<SpecialAttackDTO> distinctList = distinctByKey(specialAttacks, SpecialAttackDTO::getSpecialId);
        List<SpecialPO> specialPOS = convert.wikiSpecial2POList(distinctList);
        Map<Long, SpecialPO> existingSpecials = specialRepository.batchQueryBySpecialIds(
                        specialPOS.stream().map(SpecialPO::getSpecialId).toList())
                .stream().collect(Collectors.toMap(SpecialPO::getSpecialId, Function.identity(),
                        (oldValue, newValue) -> oldValue));
        specialPOS.forEach(special -> {
            SpecialPO existing = existingSpecials.get(special.getSpecialId());
            if (existing == null) {
                return;
            }
            special.setId(existing.getId());
            if (preserveLocalizedFields) {
                special.setDescription(existing.getDescription());
                special.setSpecialCategoryName(existing.getSpecialCategoryName());
                special.setSpecialBonus1(existing.getSpecialBonus1());
                special.setSpecialBonus2(existing.getSpecialBonus2());
            }
        });
        saveOrUpdateByKnownId(specialRepository, specialPOS, CardSyncConstants.SPECIAL_SAVE_BATCH_SIZE);
    }

    /**
     * 写入极限数据，已有记录保留本地化字段。
     *
     * @param ezaCardInfos           极限同步数据
     * @param preserveLocalizedFields 是否保留已有本地化文案
     */
    private void insertEzaCardInfo(List<EzaCardInfoDTO> ezaCardInfos, boolean preserveLocalizedFields) {
        if (CollectionUtils.isEmpty(ezaCardInfos)) {
            return;
        }
        List<EzaCardInfoDTO> distinctList = distinctByKey(ezaCardInfos, this::ezaCardKey);
        List<EzaCardPO> ezaCardPOS = convert.wikiEza2POList(distinctList);
        Map<String, EzaCardPO> existingCards = ezaCardRepository.batchQueryByCardIds(
                        ezaCardPOS.stream().map(EzaCardPO::getCardId).distinct().toList())
                .stream().collect(Collectors.toMap(this::ezaCardKey, Function.identity(),
                        (oldValue, newValue) -> oldValue));
        ezaCardPOS.forEach(ezaCard -> {
            EzaCardPO existing = existingCards.get(ezaCardKey(ezaCard));
            if (existing == null) {
                return;
            }
            ezaCard.setId(existing.getId());
            if (preserveLocalizedFields) {
                ezaCard.setCardName(existing.getCardName());
                ezaCard.setTitle(existing.getTitle());
                ezaCard.setLeaderSkill(existing.getLeaderSkill());
                ezaCard.setPassiveSkillDesc(existing.getPassiveSkillDesc());
            }
            if (ezaCard.getCost() == null) {
                ezaCard.setCost(existing.getCost());
            }
        });
        saveOrUpdateByKnownId(ezaCardRepository, ezaCardPOS, CardSyncConstants.CARD_SAVE_BATCH_SIZE);
    }

    /**
     * 写入下拉技能数据，已有记录保留本地化字段。
     *
     * @param downPullSkills          下拉技能同步数据
     * @param preserveLocalizedFields 是否保留已有本地化文案
     */
    private void insertDownPullSkillInfo(List<SkillDTO> downPullSkills, boolean preserveLocalizedFields) {
        List<SkillDTO> distinctList = distinctByKey(downPullSkills, SkillDTO::getSkillId);
        List<SkillPO> skillPOS = convert.wikiSkill2POList(distinctList);
        if (CollectionUtils.isEmpty(skillPOS)) {
            return;
        }
        Map<String, SkillPO> existingSkills = skillRepository.batchQueryBySkillIds(
                        skillPOS.stream().map(SkillPO::getSkillId).toList())
                .stream().collect(Collectors.toMap(SkillPO::getSkillId, Function.identity(),
                        (oldValue, newValue) -> oldValue));
        skillPOS.forEach(skill -> {
            SkillPO existing = existingSkills.get(skill.getSkillId());
            if (existing == null) {
                return;
            }
            skill.setId(existing.getId());
            if (preserveLocalizedFields) {
                skill.setName(existing.getName());
                skill.setConditionDescription(existing.getConditionDescription());
                skill.setEffectDescription(existing.getEffectDescription());
                skill.setSpecialCategoryName(existing.getSpecialCategoryName());
            }
        });
        saveOrUpdateByKnownId(skillRepository, skillPOS, CardSyncConstants.CARD_SAVE_BATCH_SIZE);
    }

    /**
     * 写入卡片基础数据，已有记录保留本地化字段。
     *
     * @param cards                   卡片基础同步数据
     * @param preserveLocalizedFields 是否保留已有本地化文案
     */
    private void insertCardBaseInfo(List<CardBaseInfoDTO> cards, boolean preserveLocalizedFields) {
        List<CardBaseInfoDTO> distinctedList = distinctByKey(cards, CardBaseInfoDTO::getCardId);
        List<CardPO> cardModel = convert.wikiCard2POList(distinctedList);
        checkParam(cardModel);
        Map<Long, CardPO> existingCards = cardRepository.batchQueryByCardIds(
                        cardModel.stream().map(CardPO::getCardId).toList())
                .stream().collect(Collectors.toMap(CardPO::getCardId, Function.identity(),
                        (oldValue, newValue) -> oldValue));
        cardModel.forEach(card -> {
            CardPO existing = existingCards.get(card.getCardId());
            card.setId(existing == null ? null : existing.getId());
            if (preserveLocalizedFields) {
                preserveExistingLocalizedFields(card, existing);
            } else if (existing != null && card.getCost() == null) {
                card.setCost(existing.getCost());
            }
        });
        saveOrUpdateByKnownId(cardRepository, cardModel, CardSyncConstants.CARD_SAVE_BATCH_SIZE);
    }

    /**
     * EZA-only 更新时保留已有中文基础文案，避免机器翻译覆盖人工修正内容。
     *
     * @param incoming 即将写入的卡片记录
     * @param existing 数据库已有卡片记录
     */
    private void preserveExistingLocalizedFields(CardPO incoming, CardPO existing) {
        if (existing == null) {
            return;
        }
        incoming.setCardName(existing.getCardName());
        incoming.setTitle(existing.getTitle());
        if (incoming.getCost() == null) {
            incoming.setCost(existing.getCost());
        }
        try {
            CardBaseInfoAttribute oldAttributes = JsonUtils.json2Object(
                    existing.getAttributes(), CardBaseInfoAttribute.class);
            CardBaseInfoAttribute newAttributes = JsonUtils.json2Object(
                    incoming.getAttributes(), CardBaseInfoAttribute.class);
            if (oldAttributes == null || newAttributes == null) {
                return;
            }
            newAttributes.setLeaderSkill(oldAttributes.getLeaderSkill());
            newAttributes.setPassiveSkillName(oldAttributes.getPassiveSkillName());
            newAttributes.setPassiveSkillDesc(oldAttributes.getPassiveSkillDesc());
            newAttributes.setActiveSkillName(oldAttributes.getActiveSkillName());
            newAttributes.setActiveSkillEffect(oldAttributes.getActiveSkillEffect());
            newAttributes.setActiveSkillCondition(oldAttributes.getActiveSkillCondition());
            incoming.setAttributes(JsonUtils.object2Json(newAttributes));
        } catch (Exception e) {
            log.warn("preserve localized card fields failed, cardId:{}", incoming.getCardId(), e);
        }
    }

    /**
     * 根据是否已有主键分别执行批量新增或批量更新。
     *
     * @param repository 数据仓储
     * @param entities   待保存实体
     * @param batchSize  批量大小
     * @param <T>        实体类型
     */
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

    /**
     * 构建 EZA DTO 去重 key。
     *
     * @param ezaCard EZA DTO
     * @return 去重 key
     */
    private String ezaCardKey(EzaCardInfoDTO ezaCard) {
        return ezaCard.getCardId() + CardSyncConstants.KEY_SEPARATOR + ezaCard.getStep();
    }

    /**
     * 构建 EZA PO 去重 key。
     *
     * @param ezaCard EZA PO
     * @return 去重 key
     */
    private String ezaCardKey(EzaCardPO ezaCard) {
        return ezaCard.getCardId() + CardSyncConstants.KEY_SEPARATOR + ezaCard.getStep();
    }

    /**
     * 按业务 key 去重并过滤空 key 数据。
     *
     * @param dataList     原始数据列表
     * @param keyExtractor key 提取器
     * @param <T>          数据类型
     * @param <K>          key 类型
     * @return 去重后的数据列表
     */
    private <T, K> List<T> distinctByKey(List<T> dataList, Function<T, K> keyExtractor) {
        if (CollectionUtils.isEmpty(dataList)) {
            return Collections.emptyList();
        }
        List<T> distinctData = new ArrayList<>(dataList.stream()
                .filter(Objects::nonNull)
                .filter(data -> keyExtractor.apply(data) != null)
                .collect(Collectors.toMap(keyExtractor, Function.identity(),
                        (oldValue, newValue) -> newValue, LinkedHashMap::new))
                .values());
        if (distinctData.size() < dataList.size()) {
            log.warn("discarded duplicate or invalid sync records, sourceCount:{}, distinctCount:{}",
                    dataList.size(), distinctData.size());
        }
        return distinctData;
    }

    /**
     * 按对象自身 equals 语义去重。
     *
     * @param dataList 原始数据列表
     * @param <T>      数据类型
     * @return 去重后的数据列表
     */
    private <T> List<T> distinctList(List<T> dataList) {
        return dataList.stream().distinct().toList();
    }

    /**
     * 校验卡片基础数据必要字段。
     *
     * @param cardPOS 卡片持久化数据
     */
    private void checkParam(List<CardPO> cardPOS) {
        cardPOS.forEach(card -> {
            if (StringUtils.isAnyBlank(card.getTitle(), card.getCardName()) || !ObjectUtils.allNotNull(
                    card.getCardId(), card.getHpValue(), card.getAtkValue(), card.getDefValue())) {
                log.error("param error,card:{}", JsonUtils.object2Json(card));
                throw new DokkanBizException(ExceptionErrorCode.INSERT_PARAM_ERROR);
            }
        });
    }

    /**
     * 修复数据库数据。
     *
     * @param fixType 修复类型
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
