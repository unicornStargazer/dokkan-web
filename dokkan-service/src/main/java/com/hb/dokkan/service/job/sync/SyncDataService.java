package com.hb.dokkan.service.job.sync;

import com.google.common.collect.Lists;
import com.hb.dokkan.common.constants.CardSyncConstants;
import com.hb.dokkan.common.constants.ExceptionErrorCode;
import com.hb.dokkan.common.domain.bo.data.KxWikiCardBO;
import com.hb.dokkan.common.domain.dto.data.kx.KxWikiReferenceItemDTO;
import com.hb.dokkan.common.domain.po.es.cards.CardEsPO;
import com.hb.dokkan.common.domain.po.mysql.cards.*;
import com.hb.dokkan.common.domain.po.mysql.category.DokkanCategoryPO;
import com.hb.dokkan.common.domain.po.mysql.link.DokkanLinkPO;
import com.hb.dokkan.common.domain.request.cards.CardTagBatchRequest;
import com.hb.dokkan.common.exception.domain.DokkanBizException;
import com.hb.dokkan.infrastructure.es.card.mapper.DokkanEsCardMapper;
import com.hb.dokkan.infrastructure.es.card.DokkanEsCardRepository;
import com.hb.dokkan.infrastructure.mysql.cards.*;
import com.hb.dokkan.infrastructure.mysql.categories.DokkanCategoryRepository;
import com.hb.dokkan.infrastructure.mysql.links.DokkanLinkRepository;
import com.hb.dokkan.service.cards.CardTagRuleService;
import com.hb.dokkan.service.helper.EsCardSyncHelper;
import com.hb.dokkan.service.job.sync.factory.WikiInfoStrategyFactory;
import com.hb.dokkan.service.job.sync.strategy.context.WikiContext;
import com.hb.dokkan.service.job.sync.strategy.enums.WikiInfoTypeEnum;
import com.hb.dokkan.service.job.sync.strategy.strategies.WikiCardStrategy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description KX Wiki 同步编排：事务提交后才刷新 ES，保留逐卡失败记录
 * @Author Codex
 * @Date 2026/11/8
 **/
@Component
@Slf4j
public class SyncDataService {
    /**
     * 按数据类型选择 KX Wiki 同步策略的工厂。
     **/
    @Resource
    private WikiInfoStrategyFactory strategyFactory;
    /**
     * 卡片列表扫描、候选判定及详情抓取策略。
     **/
    @Resource
    private WikiCardStrategy cardStrategy;
    /**
     * 卡片主表访问仓储。
     **/
    @Resource
    private DokkanCardRepository cardRepository;
    /**
     * 卡片状态表访问仓储。
     **/
    @Resource
    private CardStateRepository stateRepository;
    /**
     * 卡片技能表访问仓储。
     **/
    @Resource
    private CardSkillRepository skillRepository;
    /**
     * 卡片分类关系表访问仓储。
     **/
    @Resource
    private CardCategoryRelRepository categoryRelRepository;
    /**
     * 卡片链接关系表访问仓储。
     **/
    @Resource
    private CardLinkRelRepository linkRelRepository;
    /**
     * 分类字典访问仓储。
     **/
    @Resource
    private DokkanCategoryRepository categoryRepository;
    /**
     * 链接技能字典访问仓储。
     **/
    @Resource
    private DokkanLinkRepository linkRepository;
    /**
     * 人工标签保护及自动限定标记规则服务。
     **/
    @Resource
    private CardTagRuleService tagRuleService;
    /**
     * 卡片 ES 索引管理 Mapper。
     **/
    @Resource
    private DokkanEsCardMapper esCardMapper;
    /**
     * 卡片 ES 查询与整文档写入仓储。
     **/
    @Resource
    private DokkanEsCardRepository esCardRepository;
    /**
     * 从卡片聚合表组装兼容 ES 文档的助手。
     **/
    @Resource
    private EsCardSyncHelper esCardSyncHelper;
    /**
     * 卡片及子表写入使用的事务模板。
     **/
    @Resource(name = "defaultTransactionTemplate")
    private TransactionTemplate transactionTemplate;

    /**
     * 增量同步 UR/LR，本轮失败不阻断其他卡入库，也不确认源版本。
     **/
    public void initCard() {
        initCard(false);
    }

    /**
     * 增量或全量重新抓取所有纳入同步的本体及形态卡，人工标签仍按原记录合并。
     *
     * @param force true 时忽略源版本、候选变化及相同 hash，强制重写已解析的子表
     **/
    public void initCard(boolean force) {
        WikiContext context = new WikiContext();
        context.setForce(force);
        // 抓取一张就提交一张，失败隔离到本体，不累积整库详情。
        cardStrategy.execute(context, aggregate -> {
            Long rootId = aggregate.getCards().getFirst().getCardId();
            try {
                persistCardData(aggregate, force);
                cardStrategy.markCardComplete(rootId);
            } catch (RuntimeException exception) {
                context.getFailedCardIds().add(rootId);
                log.error("KX Wiki persist or ES refresh failed, cardId={}", rootId, exception);
            }
        });
        if (!context.getFailedCardIds().isEmpty()) {
            log.error("KX Wiki failed cardIds={}", context.getFailedCardIds());
            throw new DokkanBizException(ExceptionErrorCode.GET_WIKI_INFO_ERROR);
        }
        cardStrategy.markVersionComplete(context.getSourceUpdatedAt());
    }

    /**
     * 按指定 ID 强制同步，不应用自动列表黑名单和 hash 跳过，上限 100。
     **/
    public int syncCardsByIds(List<Long> cardIds) {
        List<Long> requested = validateCardIds(cardIds, CardSyncConstants.MANUAL_SYNC_MAX_CARD_COUNT);
        int completed = 0;
        List<Long> failed = new ArrayList<>();
        for (Long cardId : requested) {
            try {
                KxWikiCardBO aggregate = cardStrategy.fetchCard(cardId, true);
                persistCardData(aggregate, true);
                completed++;
            } catch (RuntimeException exception) {
                failed.add(cardId);
                log.error("KX Wiki manual sync failed, cardId={}", cardId, exception);
            }
        }
        if (!failed.isEmpty()) {
            log.error("KX Wiki manual sync partial failure, completed={}, failedIds={}", completed, failed);
            throw new DokkanBizException(ExceptionErrorCode.GET_WIKI_INFO_ERROR);
        }
        return completed;
    }

    /**
     * 单张本体聚合事务写入；子表先删后插，tag_locked 在锁定旧行后合并。
     * 相同 hash 跳过 MySQL，但仍刷新 ES，以恢复上一次提交后 ES 失败的卡片。
     **/
    private void persistCardData(KxWikiCardBO aggregate, boolean force) {
        List<Long> ids = aggregate.getCards().stream().map(CardPO::getCardId).toList();
        try {
            transactionTemplate.executeWithoutResult(status -> {
                Map<Long, CardPO> existing = cardRepository.lockByCardIds(ids).stream()
                        .collect(Collectors.toMap(CardPO::getCardId, Function.identity()));
                Set<Long> changed = new HashSet<>();
                // 主记录和所有子表在同一事务中更新，任何一项写入失败均回滚。
                for (CardPO incoming : aggregate.getCards()) {
                    CardPO old = existing.get(incoming.getCardId());
                    if (shouldSkipCardWrite(force, old, incoming)) {
                        continue;
                    }
                    changed.add(incoming.getCardId());
                    tagRuleService.apply(incoming, old);
                    if (old != null) {
                        incoming.setId(old.getId());
                        incoming.setCreator(old.getCreator());
                        incoming.setCreateTime(old.getCreateTime());
                    }
                    if (!cardRepository.saveOrUpdate(incoming)) {
                        throw new DokkanBizException(ExceptionErrorCode.INSERT_PARAM_ERROR);
                    }
                }
                List<Long> changedIds = ids.stream().filter(changed::contains).toList();
                stateRepository.deleteByCardIds(changedIds);
                skillRepository.deleteByCardIds(changedIds);
                categoryRelRepository.deleteByCardIds(changedIds);
                linkRelRepository.deleteByCardIds(changedIds);
                stateRepository.insertBatch(aggregate.getStates().stream().filter(row -> changed.contains(row.getCardId())).toList());
                skillRepository.insertBatch(aggregate.getSkills().stream().filter(row -> changed.contains(row.getCardId())).toList());
                categoryRelRepository.insertBatch(aggregate.getCategories().stream().filter(row -> changed.contains(row.getCardId())).toList());
                linkRelRepository.insertBatch(aggregate.getLinks().stream().filter(row -> changed.contains(row.getCardId())).toList());
                if (!changedIds.isEmpty()) {
                    upsertLinks(aggregate.getLinkDefinitions(), true);
                }
            });
        } catch (RuntimeException exception) {
            log.error("KX Wiki transaction rolled back, cardIds={}", ids, exception);
            throw new DokkanBizException(ExceptionErrorCode.INSERT_PARAM_ERROR, exception);
        }
        // 逐卡同步进度由调用方按已处理卡数上报，避免每张卡将总进度跳到固定百分比。
        syncEsCardsByCardIds(ids);
    }

    /**
     * 初始化分类，保护现有中文名，只新增缺失项并更新英文名。
     **/
    public void initCategories() {
        WikiContext context = new WikiContext();
        strategyFactory.getWikiStrategy(WikiInfoTypeEnum.CATEGORY).execute(context);
        Map<Long, String> english = context.getEnglishCategoryData().stream().filter(row -> row.getName() != null)
                .collect(Collectors.toMap(KxWikiReferenceItemDTO::getId, KxWikiReferenceItemDTO::getName));
        transactionTemplate.executeWithoutResult(status -> {
            Map<Long, DokkanCategoryPO> existing = categoryRepository.list().stream()
                    .collect(Collectors.toMap(DokkanCategoryPO::getCategoryId, Function.identity()));
            for (KxWikiReferenceItemDTO source : context.getCategoryData()) {
                DokkanCategoryPO category = existing.get(source.getId());
                if (category == null) {
                    category = new DokkanCategoryPO();
                    category.setCategoryId(source.getId());
                    category.setCategoryName(source.getName());
                }
                category.setCategoryNameEn(english.get(source.getId()));
                if (!categoryRepository.saveOrUpdate(category)) {
                    throw new DokkanBizException(ExceptionErrorCode.INSERT_PARAM_ERROR);
                }
            }
        });
    }

    /**
     * reference 仅补齐已有链接的空名称/1级描述；新链接由详情补齐满级描述后插入。
     **/
    public void initLinks() {
        WikiContext context = new WikiContext();
        strategyFactory.getWikiStrategy(WikiInfoTypeEnum.LINK).execute(context);
        List<DokkanLinkPO> incoming = context.getLinkData().stream().map(source -> {
            DokkanLinkPO link = new DokkanLinkPO();
            link.setLinkId(source.getId());
            link.setLinkName(source.getName());
            link.setLevel1Description(source.getDescription());
            return link;
        }).toList();
        transactionTemplate.executeWithoutResult(status -> upsertLinks(incoming, false));
    }

    /**
     * 事务内链接补齐，已有名称保持人工值；卡片详情允许插入完整的新链接。
     **/
    private void upsertLinks(List<DokkanLinkPO> incoming, boolean allowInsert) {
        Map<Long, DokkanLinkPO> existing = linkRepository.list().stream()
                .collect(Collectors.toMap(DokkanLinkPO::getLinkId, Function.identity()));
        for (DokkanLinkPO source : incoming) {
            DokkanLinkPO old = existing.get(source.getLinkId());
            if (old == null) {
                if (allowInsert && StringUtils.isNoneBlank(source.getLinkName(), source.getLevel1Description(), source.getLevel10Description())) {
                    if (!linkRepository.save(source)) {
                        throw new DokkanBizException(ExceptionErrorCode.INSERT_PARAM_ERROR);
                    }
                    existing.put(source.getLinkId(), source);
                }
                continue;
            }
            boolean changed = false;
            if (shouldFillLinkName(allowInsert, old, source)) {
                old.setLinkName(source.getLinkName());
                changed = true;
            }
            if (StringUtils.isBlank(old.getLevel1Description()) && StringUtils.isNotBlank(source.getLevel1Description())) {
                old.setLevel1Description(source.getLevel1Description());
                changed = true;
            }
            if (StringUtils.isBlank(old.getLevel10Description()) && StringUtils.isNotBlank(source.getLevel10Description())) {
                old.setLevel10Description(source.getLevel10Description());
                changed = true;
            }
            if (changed && !linkRepository.updateById(old)) {
                throw new DokkanBizException(ExceptionErrorCode.INSERT_PARAM_ERROR);
            }
        }
    }

    /**
     * 全量 ES 重建按批读取子表，不再全表加载旧 eza/special/skill。
     **/
    public void syncEsCardData() {
        recreateCardIndex();
        List<CardPO> cards = cardRepository.list();
        int done = 0;
        for (List<CardPO> batch : Lists.partition(cards, CardSyncConstants.CARD_SAVE_BATCH_SIZE)) {
            List<CardEsPO> documents = esCardSyncHelper.buildEsCards(batch);
            esCardRepository.saveDocuments(documents);
            done += batch.size();
            SyncProgressContext.update(CardSyncConstants.SYNC_PROGRESS_ES_READ_COMPLETE,
                    CardSyncConstants.STAGE_WRITE_ES, CardSyncConstants.MESSAGE_SYNCED_PREFIX + done);
        }
    }

    /**
     * 按 ID 构建并写入完整 ES 文档，所有写入结果都必须成功。
     **/
    public int syncEsCardsByCardIds(List<Long> cardIds) {
        int count = 0;
        if (!esCardMapper.existsIndex(DokkanEsCardMapper.INDEX_NAME) && !esCardMapper.createIndex()) {
            throw new DokkanBizException(ExceptionErrorCode.CREATE_INDEX_ERROR);
        }
        for (List<Long> batch : Lists.partition(cardIds, CardSyncConstants.CARD_SAVE_BATCH_SIZE)) {
            List<CardEsPO> documents = esCardSyncHelper.buildEsCards(cardRepository.batchQueryByCardIds(batch));
            if (documents.isEmpty()) {
                continue;
            }
            esCardRepository.saveDocuments(documents);
            count += documents.size();
        }
        return count;
    }

    /**
     * 人工标签事务提交后刷新 ES；ES 失败对外报错但保留已提交标签，可重复请求恢复。
     **/
    public int batchUpdateTags(CardTagBatchRequest request) {
        List<Long> ids = validateCardIds(request.getCardIds(), CardSyncConstants.CARD_TAG_BATCH_MAX_COUNT);
        Integer updated = transactionTemplate.execute(status -> cardRepository.updateTags(ids, request));
        syncEsCardsByCardIds(ids);
        return Objects.requireNonNull(updated);
    }

    /**
     * 统一校验正整数卡片 ID，按去重后数量限制。
     **/
    private List<Long> validateCardIds(List<Long> ids, int limit) {
        if (!areValidCardIds(ids)) {
            throw new DokkanBizException(ExceptionErrorCode.QUERY_PARAM_ERROR);
        }
        List<Long> distinct = ids.stream().distinct().toList();
        if (distinct.size() > limit) {
            throw new DokkanBizException(ExceptionErrorCode.QUERY_PARAM_ERROR);
        }
        return distinct;
    }

    /**
     * 非强制同步时，已有卡片的源哈希相同则跳过 MySQL 写入。
     **/
    private boolean shouldSkipCardWrite(boolean force, CardPO existing, CardPO incoming) {
        if (force) {
            return false;
        } else if (existing == null) {
            return false;
        } else {
            return Objects.equals(existing.getSourceHash(), incoming.getSourceHash());
        }
    }

    /**
     * 字典同步仅在现有链接名称为空且源名称有效时补齐名称。
     **/
    private boolean shouldFillLinkName(boolean allowInsert, DokkanLinkPO existing, DokkanLinkPO source) {
        if (allowInsert) {
            return false;
        } else if (StringUtils.isNotBlank(existing.getLinkName())) {
            return false;
        } else {
            return StringUtils.isNotBlank(source.getLinkName());
        }
    }

    /**
     * 先删除已有索引，再创建新索引；任一步失败立即终止重建。
     **/
    private void recreateCardIndex() {
        if (esCardMapper.existsIndex(DokkanEsCardMapper.INDEX_NAME)) {
            if (!esCardMapper.deleteIndex(DokkanEsCardMapper.INDEX_NAME)) {
                throw new DokkanBizException(ExceptionErrorCode.CREATE_INDEX_ERROR);
            }
        }
        if (!esCardMapper.createIndex()) {
            throw new DokkanBizException(ExceptionErrorCode.CREATE_INDEX_ERROR);
        }
    }

    /**
     * 校验卡片 ID 集合非空，且每个 ID 都是有效正整数。
     **/
    private boolean areValidCardIds(List<Long> ids) {
        if (ids == null) {
            return false;
        } else if (ids.isEmpty()) {
            return false;
        } else {
            return ids.stream().noneMatch(id -> id == null || id <= CardSyncConstants.MIN_VALID_CARD_ID);
        }
    }

}
