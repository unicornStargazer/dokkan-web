package com.hb.dokkan.service.job.sync;

import com.hb.dokkan.common.constants.CardSyncConstants;
import com.hb.dokkan.common.constants.ExceptionErrorCode;
import com.hb.dokkan.common.domain.bo.data.KxWikiCardChangesBO;
import com.hb.dokkan.common.domain.dto.data.kx.*;
import com.hb.dokkan.common.exception.domain.DokkanBizException;
import com.hb.dokkan.common.utils.JsonUtils;
import com.hb.dokkan.config.kx.KxWikiProperties;
import com.hb.dokkan.infrastructure.mysql.sync.KxWikiSyncRepository;
import com.hb.dokkan.service.client.KxWikiClient;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @Description 合并最新批次与开放时间线，生成卡片增量候选和待确认水位
 * @Author Codex
 * @Date 2026/11/10
 **/
@Service
public class KxWikiCardChangeService {
    /**
     * 统一限流、重试并校验响应的源站客户端。
     **/
    private final KxWikiClient client;
    /**
     * 按地区和语言隔离的同步范围配置。
     **/
    private final KxWikiProperties properties;
    /**
     * 复用已有同步状态表，读取上次成功指纹和日期。
     **/
    private final KxWikiSyncRepository repository;

    /**
     * 注入外部数据入口与持久化水位仓储。
     **/
    public KxWikiCardChangeService(KxWikiClient client, KxWikiProperties properties, KxWikiSyncRepository repository) {
        this.client = client;
        this.properties = properties;
        this.repository = repository;
    }

    /**
     * 先读取两个更新入口，再决定是否允许源 status 短路；失败不返回可确认水位。
     *
     * @param syncKey 当前卡片同步范围
     * @return 去重候选和整轮成功后才能提交的检查点
     **/
    public KxWikiCardChangesBO scan(String syncKey) {
        KxWikiCardChangesBO changes = new KxWikiCardChangesBO();
        // 最新批次来自国际服游戏库，不能用它推进其他地区的同步。
        if (CardSyncConstants.KX_WIKI_DEFAULT_REGION.equals(properties.getRegion())) {
            KxWikiLatestDdlDTO latest = client.getLatestDdl();
            changes.setDdlFingerprint(latest.getFingerprint());
            String previous = repository.findLastUpdatedAt(syncKey + CardSyncConstants.KX_WIKI_DDL_CHECKPOINT_SUFFIX);
            if (!Objects.equals(previous, latest.getFingerprint())) {
                changes.setChanged(true);
                addCardIds(changes.getCardIds(), latest.getNewUnits());
                addCardIds(changes.getCardIds(), latest.getNewEzas());
            }
        }
        scanTimeline(syncKey, changes);
        return changes;
    }

    /**
     * 首次接入只回填首页；有成功水位后追溯所有新日期，包含水位当天以捕获同日新增。
     **/
    private void scanTimeline(String syncKey, KxWikiCardChangesBO changes) {
        String previousJson = repository.findLastUpdatedAt(syncKey + CardSyncConstants.KX_WIKI_TIMELINE_CHECKPOINT_SUFFIX);
        KxWikiTimelineCheckpointDTO previous = previousJson == null ? new KxWikiTimelineCheckpointDTO()
                : JsonUtils.json2Object(previousJson, KxWikiTimelineCheckpointDTO.class);
        KxWikiTimelineDTO firstPage = client.getCardTimeline(1, null);
        KxWikiTimelineCheckpointDTO checkpoint = new KxWikiTimelineCheckpointDTO();
        if (!firstPage.getGroups().isEmpty()) {
            KxWikiTimelineGroupDTO latest = client.completeTimelineGroup(firstPage.getGroups().getFirst());
            firstPage.setGroups(new ArrayList<>(firstPage.getGroups()));
            firstPage.getGroups().set(0, latest);
            checkpoint.setDate(latest.getDate());
            checkpoint.setHash(groupHash(latest));
        }
        String checkpointJson = JsonUtils.object2Json(checkpoint);
        changes.setTimelineCheckpoint(checkpointJson);
        if (Objects.equals(previousJson, checkpointJson)) {
            return;
        }
        // 源站历史暂时回退或空响应时不能覆盖已有成功水位。
        if (isTimelineRegression(previous, checkpoint)) {
            throw new DokkanBizException(ExceptionErrorCode.GET_WIKI_INFO_ERROR);
        }
        changes.setChanged(true);
        String cutoff = previous.getDate();
        KxWikiTimelineDTO pageResponse = firstPage;
        String previousDate = null;
        for (int page = 1; page <= CardSyncConstants.KX_WIKI_MAX_PAGES; page++) {
            for (KxWikiTimelineGroupDTO group : pageResponse.getGroups()) {
                // 按天倒序是水位截断的前提，异常顺序直接失败而不是跳过未知区间。
                if (previousDate != null && group.getDate().compareTo(previousDate) > 0) {
                    throw new DokkanBizException(ExceptionErrorCode.GET_WIKI_INFO_ERROR);
                }
                previousDate = group.getDate();
                if (cutoff != null && group.getDate().compareTo(cutoff) < 0) {
                    return;
                }
                addCardIds(changes.getCardIds(), client.completeTimelineGroup(group).getItems());
                if (Objects.equals(cutoff, group.getDate())) {
                    return;
                }
            }
            if (cutoff == null || page >= pageResponse.getPages()) {
                return;
            }
            if (page == CardSyncConstants.KX_WIKI_MAX_PAGES) {
                throw new DokkanBizException(ExceptionErrorCode.GET_WIKI_INFO_ERROR);
            }
            pageResponse = client.getCardTimeline(page + 1, null);
        }
        throw new DokkanBizException(ExceptionErrorCode.GET_WIKI_INFO_ERROR);
    }

    /**
     * 保持已经成功的时间线日期单调递增。
     **/
    private boolean isTimelineRegression(KxWikiTimelineCheckpointDTO previous, KxWikiTimelineCheckpointDTO current) {
        if (previous.getDate() == null) {
            return false;
        } else if (current.getDate() == null) {
            return true;
        } else {
            return current.getDate().compareTo(previous.getDate()) < 0;
        }
    }

    /**
     * 对当天事件按卡片、类型、开放时间排序后计算指纹，忽略响应排列与抓取时间。
     **/
    private String groupHash(KxWikiTimelineGroupDTO group) {
        List<KxWikiCardChangeDTO> sorted = group.getItems().stream()
                .sorted(Comparator.comparing(KxWikiCardChangeDTO::getId)
                        .thenComparing(KxWikiCardChangeDTO::getReleaseKind, Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing(KxWikiCardChangeDTO::getReleaseAt, Comparator.nullsFirst(Comparator.naturalOrder())))
                .toList();
        return JsonUtils.sourceHash(JsonUtils.object2Json(sorted));
    }

    /**
     * 合并更新入口候选，缺失卡片 ID 视为源数据错误，避免确认不完整批次。
     **/
    private void addCardIds(Set<Long> target, List<KxWikiCardChangeDTO> items) {
        for (KxWikiCardChangeDTO item : items) {
            if (item == null || item.getId() == null) {
                throw new DokkanBizException(ExceptionErrorCode.GET_WIKI_INFO_ERROR);
            }
            target.add(item.getId());
        }
    }
}
