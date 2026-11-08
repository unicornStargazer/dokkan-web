package com.hb.dokkan.service.job.sync.strategy.strategies;

import com.hb.dokkan.common.constants.CardSyncConstants;
import com.hb.dokkan.common.constants.ExceptionErrorCode;
import com.hb.dokkan.common.domain.bo.data.KxWikiCardBO;
import com.hb.dokkan.common.domain.dto.data.kx.*;
import com.hb.dokkan.common.domain.po.mysql.cards.CardPO;
import com.hb.dokkan.common.exception.domain.DokkanBizException;
import com.hb.dokkan.config.data.WikiCardFilter;
import com.hb.dokkan.infrastructure.mysql.cards.DokkanCardRepository;
import com.hb.dokkan.service.client.KxWikiClient;
import com.hb.dokkan.service.convert.KxWikiCardConvert;
import com.hb.dokkan.service.job.sync.SyncProgressContext;
import com.hb.dokkan.service.job.sync.strategy.WikiInfoStrategy;
import com.hb.dokkan.service.job.sync.strategy.context.WikiContext;
import com.hb.dokkan.service.job.sync.strategy.enums.WikiInfoTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description KX Wiki 卡片候选扫描和逐卡抓取策略
 * @Author Codex
 * @Date 2026/11/8
 **/
@Component
@Slf4j
public class WikiCardStrategy implements WikiInfoStrategy {
    /**
     * KX Wiki 数据访问客户端。
     **/
    @Resource
    private KxWikiClient client;
    /**
     * KX Wiki 响应到卡片事务聚合的转换器。
     **/
    @Resource
    private KxWikiCardConvert convert;
    /**
     * 卡片主表访问仓储。
     **/
    @Resource
    private DokkanCardRepository cardRepository;
    /**
     * 自动同步的卡片黑白名单配置。
     **/
    @Resource
    private WikiCardFilter cardFilter;
    /**
     * 只有 MySQL 和 ES 全部成功后才确认源水位。
     **/
    private volatile String lastUpdatedAt;
    /**
     * ES 或持久化失败的根卡，即使 flags 相同也必须在下一轮重试。
     **/
    private final Set<Long> pendingCardIds = ConcurrentHashMap.newKeySet();

    /**
     * 匹配卡片同步策略。
     **/
    @Override
    public boolean isMatched(WikiInfoTypeEnum type) {
        return WikiInfoTypeEnum.CARD.equals(type);
    }

    /**
     * status 无变化时短路；分页失败抛错；详情失败隔离并记录卡片 ID。
     **/
    @Override
    public void execute(WikiContext context) {
        String updatedAt = client.getStatus().map(KxWikiStatusDTO::getUpdatedAt)
                .filter(value -> !value.isBlank()).orElseThrow(() -> new DokkanBizException(ExceptionErrorCode.GET_WIKI_INFO_ERROR));
        if (Objects.equals(updatedAt, lastUpdatedAt) && pendingCardIds.isEmpty()) {
            return;
        }
        context.setSourceUpdatedAt(updatedAt);
        List<KxWikiCardListItemDTO> catalog = readCatalog();
        Map<Long, CardPO> existing = cardRepository.listSyncFlags().stream()
                .collect(Collectors.toMap(CardPO::getCardId, Function.identity()));
        List<Long> candidates = catalog.stream().filter(item -> isCandidate(item, existing.get(item.getId())))
                .map(KxWikiCardListItemDTO::getId).distinct().toList();
        for (int index = 0; index < candidates.size(); index++) {
            Long cardId = candidates.get(index);
            pendingCardIds.add(cardId);
            try {
                context.getCardData().add(fetchCard(cardId, false));
            } catch (RuntimeException exception) {
                context.getFailedCardIds().add(cardId);
                log.error("KX Wiki card failed, cardId={}", cardId, exception);
            }
            SyncProgressContext.update(CardSyncConstants.SYNC_PROGRESS_EXTERNAL_START
                            + (index + 1) * CardSyncConstants.SYNC_PROGRESS_EXTERNAL_SPAN / Math.max(candidates.size(), 1),
                    CardSyncConstants.STAGE_FETCH_EXTERNAL,
                    CardSyncConstants.MESSAGE_KX_FETCH_PROGRESS + (index + 1) + CardSyncConstants.PATH_SEPARATOR + candidates.size());
        }
    }

    /**
     * 获取一张本体卡和全部形态；手动同步强制读取 extras。
     **/
    public KxWikiCardBO fetchCard(Long cardId, boolean manual) {
        KxWikiCardDetailDTO detail = client.getCardDetail(cardId)
                .orElseThrow(() -> new DokkanBizException(ExceptionErrorCode.GET_WIKI_INFO_ERROR));
        if (!Objects.equals(cardId, detail.getId())) {
            throw new DokkanBizException(ExceptionErrorCode.GET_WIKI_INFO_ERROR);
        }
        KxWikiEnrichmentDTO enrichment = client.getEnrichment(cardId)
                .orElseThrow(() -> new DokkanBizException(ExceptionErrorCode.GET_WIKI_INFO_ERROR));
        boolean hasEza = detail.getStates() != null && detail.getStates().stream()
                .anyMatch(state -> !CardSyncConstants.CARD_STATE_BASE.equals(state.getKey()));
        KxWikiExtrasDTO extras = manual || hasEza ? client.getExtras(cardId)
                .orElseThrow(() -> new DokkanBizException(ExceptionErrorCode.GET_WIKI_INFO_ERROR)) : null;
        return convert.toAggregate(detail, enrichment, extras);
    }

    /**
     * 确认已完成的根卡，确保 ES 失败时可重放整个聚合。
     **/
    public void markCardComplete(Long cardId) {
        pendingCardIds.remove(cardId);
    }

    /**
     * 仅成功轮次确认源版本；失败卡仍在 pending 中，后续不会 status 短路。
     **/
    public void markVersionComplete(String updatedAt) {
        if (pendingCardIds.isEmpty() && updatedAt != null) {
            lastUpdatedAt = updatedAt;
        }
    }

    /**
     * 读取全部列表页，保留 UR/LR 和既有黑名单规则。
     **/
    private List<KxWikiCardListItemDTO> readCatalog() {
        List<KxWikiCardListItemDTO> catalog = new ArrayList<>();
        Set<Long> blacklist = new HashSet<>(cardFilter.getWikiCardBlackList());
        for (int page = 1; page <= CardSyncConstants.KX_WIKI_MAX_PAGES; page++) {
            KxWikiPageResponse<KxWikiCardListItemDTO> response = client.listCards(page);
            response.getItems().stream().filter(item -> isIncludedInAutomaticSync(item, blacklist))
                    .forEach(catalog::add);
            if (page >= response.getPages()) {
                return catalog;
            }
        }
        throw new DokkanBizException(ExceptionErrorCode.GET_WIKI_INFO_ERROR);
    }

    /**
     * 新卡、待重试、缺少 hash 或极限标记变化时需要抓取详情。
     **/
    private boolean isCandidate(KxWikiCardListItemDTO source, CardPO existing) {
        if (existing == null) {
            return true;
        } else if (pendingCardIds.contains(source.getId())) {
            return true;
        } else if (existing.getSourceHash() == null) {
            return true;
        } else if (Boolean.TRUE.equals(source.getHasEza()) != Boolean.TRUE.equals(existing.getEzaFlag())) {
            return true;
        } else {
            return Boolean.TRUE.equals(source.getHasSeza()) != Boolean.TRUE.equals(existing.getSezaFlag());
        }
    }

    /**
     * 按 ID、稀有度和黑名单逐项筛选自动同步范围，保持原有短路顺序。
     **/
    private boolean isIncludedInAutomaticSync(KxWikiCardListItemDTO item, Set<Long> blacklist) {
        if (item.getId() == null) {
            return false;
        } else if (item.getRarity() == null) {
            return false;
        } else if (item.getRarity() < CardSyncConstants.LIMITED_RULE_UR_RARITY) {
            return false;
        } else {
            return !blacklist.contains(item.getId());
        }
    }
}
