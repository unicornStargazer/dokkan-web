package com.hb.dokkan.service.job.sync;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.hb.dokkan.common.constants.ExceptionErrorCode;
import com.hb.dokkan.common.exception.domain.DokkanBizException;
import com.hb.dokkan.config.thread.DokkanThreadPoolExecutor;
import com.hb.dokkan.infrastructure.es.card.mapper.DokkanEsCardMapper;
import com.hb.dokkan.common.domain.po.es.cards.CardEsPO;
import com.hb.dokkan.infrastructure.mysql.cards.DokkanCardRepository;
import com.hb.dokkan.infrastructure.mysql.cards.DokkanEzaCardRepository;
import com.hb.dokkan.infrastructure.mysql.cards.DokkanSkillRepository;
import com.hb.dokkan.infrastructure.mysql.cards.DokkanSpecialRepository;
import com.hb.dokkan.common.domain.po.mysql.cards.CardPO;
import com.hb.dokkan.common.domain.po.mysql.cards.EzaCardPO;
import com.hb.dokkan.common.domain.po.mysql.cards.SkillPO;
import com.hb.dokkan.common.domain.po.mysql.cards.SpecialPO;
import com.hb.dokkan.infrastructure.mysql.categories.DokkanCategoryRepository;
import com.hb.dokkan.common.domain.po.mysql.category.DokkanCategoryPO;
import com.hb.dokkan.infrastructure.mysql.links.DokkanLinkRepository;
import com.hb.dokkan.common.domain.po.mysql.link.DokkanLinkPO;
import com.hb.dokkan.service.convert.DokkanSyncConvert;
import com.hb.dokkan.common.domain.bo.data.WikiCardBO;
import com.hb.dokkan.common.domain.dto.data.cards.CardBaseInfoDTO;
import com.hb.dokkan.common.domain.dto.data.cards.EzaCardInfoDTO;
import com.hb.dokkan.common.domain.dto.data.cards.SkillDTO;
import com.hb.dokkan.common.domain.dto.data.cards.SpecialAttackDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiCategoryDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiLinkDTO;
import com.hb.dokkan.service.helper.EsCardSyncHelper;
import com.hb.dokkan.service.job.sync.factory.WikiInfoStrategyFactory;
import com.hb.dokkan.service.job.sync.strategy.WikiInfoStrategy;
import com.hb.dokkan.service.job.sync.strategy.context.WikiContext;
import com.hb.dokkan.service.job.sync.strategy.enums.WikiInfoTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;

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


    /**
     * 初始化卡片数据
     */
    public void initCard() {
        WikiInfoStrategy strategy = strategyFactory.getWikiStrategy(WikiInfoTypeEnum.CARD);
        WikiContext context = new WikiContext();
        strategy.execute(context);
        WikiCardBO cardData = context.getCardData();
        if (Objects.isNull(cardData)) {
            log.error("初始化失败，获取卡片为空");
            return;
        }
        transactionTemplate.execute(status -> {
            try {
                insertCardBaseInfo(cardData.getCardBaseData());
                insertDownPullSkillInfo(cardData.getDownPullSkills());
                insertEzaCardInfo(cardData.getEzaCardInfos());
                insertSpecialInfo(cardData.getSpecialAttacks());
            } catch (Exception e) {
                log.error("SyncDataService#initCard error :{}", e.getMessage(), e);
                status.setRollbackOnly();
            }
            return null;
        });
        log.info("初始化完成");
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
        } catch (Exception e) {
            log.error("query db data error :{}", e.getMessage(), e);
            throw new DokkanBizException("query db data error :" + e.getMessage());
        }
        List<CardEsPO> esCards = esCardSyncHelper.buildEsCardPO(dataMap);
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

    private void insertSpecialInfo(List<SpecialAttackDTO> specialAttacks) {
        if (CollectionUtils.isEmpty(specialAttacks)) {
            return;
        }
        List<SpecialAttackDTO> distinctList = distinctList(specialAttacks);
        List<SpecialPO> specialPOS = convert.wikiSpecial2POList(distinctList);
        specialRepository.saveBatch(specialPOS, 1000);
    }

    private void insertEzaCardInfo(List<EzaCardInfoDTO> ezaCardInfos) {
        if (CollectionUtils.isEmpty(ezaCardInfos)) {
            return;
        }
        List<EzaCardInfoDTO> distinctList = distinctList(ezaCardInfos);
        List<EzaCardPO> ezaCardPOS = convert.wikiEza2POList(distinctList);
        ezaCardRepository.saveBatch(ezaCardPOS);
    }

    private void insertDownPullSkillInfo(List<SkillDTO> downPullSkills) {
        List<SkillDTO> distinctList = distinctList(downPullSkills);
        List<SkillPO> skillPOS = convert.wikiSkill2POList(distinctList);
        if (CollectionUtils.isEmpty(skillPOS)) {
            return;
        }
        skillRepository.saveBatch(skillPOS);
    }

    private void insertCardBaseInfo(List<CardBaseInfoDTO> cards) {
        List<CardBaseInfoDTO> distinctedList = distinctList(cards);
        List<CardPO> cardModel = convert.wikiCard2POList(distinctedList);
        checkParam(cardModel);
        cardRepository.saveBatch(cardModel, 500);
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
}
