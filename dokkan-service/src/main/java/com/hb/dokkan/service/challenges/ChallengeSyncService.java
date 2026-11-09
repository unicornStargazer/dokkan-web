package com.hb.dokkan.service.challenges;

import com.hb.dokkan.common.constants.CardSyncConstants;
import com.hb.dokkan.common.domain.response.challenges.ChallengeSyncResult;
import com.hb.dokkan.config.kx.KxWikiProperties;
import com.hb.dokkan.infrastructure.mysql.challenges.ChallengeRepository;
import com.hb.dokkan.service.client.KxWikiClient;
import com.hb.dokkan.service.job.sync.SyncProgressContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Description 活动挑战目录和副本敌人快照同步服务
 * @Author Codex
 * @Date 2026/11/9
 **/
@Slf4j
@Service
public class ChallengeSyncService {

    /**
     * KX Wiki 挑战数据 HTTP 客户端。
     **/
    private final KxWikiClient kxWikiClient;

    /**
     * 活动挑战目录和敌人树持久化仓储。
     **/
    private final ChallengeRepository challengeRepository;

    /**
     * 当前 KX Wiki 数据范围。
     **/
    private final KxWikiProperties properties;

    /**
     * 注入活动挑战同步所需依赖。
     *
     * @param kxWikiClient KX Wiki HTTP 客户端
     * @param challengeRepository 挑战 MySQL 仓储
     * @param properties KX Wiki 配置
     */
    public ChallengeSyncService(KxWikiClient kxWikiClient, ChallengeRepository challengeRepository,
                                KxWikiProperties properties) {
        this.kxWikiClient = kxWikiClient;
        this.challengeRepository = challengeRepository;
        this.properties = properties;
    }

    /**
     * 全量同步挑战目录并逐地图刷新敌人快照；远端局部失败保留已有数据并继续。
     *
     * @param force 是否强制重抓，挑战同步每次都会检查源地图
     * @return 本轮同步结果计数
     */
    public ChallengeSyncResult sync(boolean force) {
        List<Map<String, Object>> events = fetchCompleteEventCatalog();
        ChallengeSyncResult result = new ChallengeSyncResult();
        result.setEventTotal(events.size());
        int totalSteps = events.size();
        int completedSteps = 0;

        for (Map<String, Object> event : events) {
            long eventId = number(event.get("id"));
            Map<String, Object> eventDetail;
            try {
                eventDetail = kxWikiClient.getChallengeEvent(eventId);
                Map<String, Object> sourceEvent = mapValue(eventDetail.get("event"));
                if (!isMatchingChallengeEvent(sourceEvent, eventId)) {
                    throw new IllegalArgumentException("Challenge event identity mismatch, eventId=" + eventId);
                }
            } catch (RuntimeException exception) {
                result.setFailedCount(result.getFailedCount() + 1);
                log.error("Challenge event synchronization failed, eventId={}", eventId, exception);
                result.setEventProcessed(result.getEventProcessed() + 1);
                updateProgress(++completedSteps, totalSteps, eventId, null);
                continue;
            }

            challengeRepository.saveDirectory(properties.getRegion(), properties.getLang(), event, eventDetail);
            List<Long> mapIds = mapIds(mapValue(eventDetail.get("event")));
            totalSteps += mapIds.size();
            result.setMapTotal(result.getMapTotal() + mapIds.size());
            updateProgress(completedSteps, totalSteps, eventId, null);

            for (Long mapId : mapIds) {
                syncMap(eventId, mapId, result);
                updateProgress(++completedSteps, totalSteps, eventId, mapId);
            }
            result.setEventProcessed(result.getEventProcessed() + 1);
            updateProgress(++completedSteps, totalSteps, eventId, null);
        }

        if (result.getFailedCount() > 0) {
            throw new ChallengeSyncException("挑战同步完成但部分活动或地图失败", result);
        }
        return result;
    }

    /**
     * 读取 next 链直到目录结束；循环页或非法分组会终止任务。
     *
     * @return 完整挑战活动目录
     */
    private List<Map<String, Object>> fetchCompleteEventCatalog() {
        List<Map<String, Object>> events = new ArrayList<>();
        Set<Integer> visitedPages = new HashSet<>();
        int page = 1;
        while (page > 0 && page <= CardSyncConstants.KX_WIKI_MAX_PAGES) {
            if (!visitedPages.add(page)) {
                throw new IllegalStateException("Challenge catalog contains a cyclic next page");
            }
            Map<String, Object> response = kxWikiClient.listChallengeEvents(page);
            if (!isValidChallengeCatalogResponse(response)) {
                throw new IllegalArgumentException("Invalid challenge catalog page=" + page);
            }
            List<?> pageEvents = (List<?>) response.get("events");
            for (Object item : pageEvents) {
                Map<String, Object> event = mapValue(item);
                if (!isChallengeEvent(event)) {
                    throw new IllegalArgumentException("Challenge endpoint returned a non-challenge event");
                }
                number(event.get("id"));
                events.add(event);
            }
            Integer next = integerValue(response.get("next"));
            if (next == null) {
                return events;
            }
            page = next;
        }
        throw new IllegalStateException("Challenge catalog did not terminate within the page limit");
    }

    /**
     * 請求地圖並區分源未採集、有效快照和遠端失敗；資料庫錯誤向上傳播。
     */
    private void syncMap(long eventId, long mapId, ChallengeSyncResult result) {
        Map<String, Object> detail;
        try {
            detail = kxWikiClient.getChallengeMap(mapId);
            validateMapResponse(detail, eventId, mapId);
        } catch (RuntimeException exception) {
            challengeRepository.recordMapFailure(properties.getRegion(), properties.getLang(), mapId,
                    exception.getMessage());
            result.setFailedCount(result.getFailedCount() + 1);
            log.error("Challenge map synchronization failed, eventId={}, mapId={}", eventId, mapId, exception);
            return;
        }

        if (CardSyncConstants.CHALLENGE_CAPTURE_STATUS_NOT_CAPTURED.equals(detail.get("capture_status"))) {
            challengeRepository.recordNotCaptured(properties.getRegion(), properties.getLang(), mapId, detail);
            result.setNotCapturedCount(result.getNotCapturedCount() + 1);
        } else if (Boolean.TRUE.equals(detail.get("available")) && detail.get("battles") instanceof List<?>) {
            boolean changed = challengeRepository.replaceMapSnapshot(
                    properties.getRegion(), properties.getLang(), mapId, detail);
            if (!changed) {
                result.setUnchangedCount(result.getUnchangedCount() + 1);
            }
            if (Boolean.TRUE.equals(detail.get("partial"))) {
                result.setPartialCount(result.getPartialCount() + 1);
            } else {
                result.setCapturedCount(result.getCapturedCount() + 1);
            }
        } else {
            challengeRepository.recordMapFailure(properties.getRegion(), properties.getLang(), mapId,
                    "Map response does not contain an available capture");
            result.setFailedCount(result.getFailedCount() + 1);
        }
    }

    /**
     * 校验活动和地图响应源 ID 关联。
     */
    private void validateMapResponse(Map<String, Object> detail, long eventId, long mapId) {
        Map<String, Object> map = mapValue(detail.get("map"));
        if (number(detail.get("event_id")) != eventId || number(map.get("id")) != mapId) {
            throw new IllegalArgumentException("Challenge map identity mismatch, mapId=" + mapId);
        } else if (!(detail.get("capture_status") instanceof String)) {
            throw new IllegalArgumentException("Challenge map response lacks capture_status, mapId=" + mapId);
        }
    }

    /**
     * 展平活动下所有关卡地图 ID 并保留源顺序。
     */
    private List<Long> mapIds(Map<String, Object> event) {
        List<Long> mapIds = new ArrayList<>();
        for (Object stageItem : listValue(event.get("stages"))) {
            for (Object mapItem : listValue(mapValue(stageItem).get("maps"))) {
                mapIds.add(number(mapValue(mapItem).get("id")));
            }
        }
        return mapIds;
    }

    /**
     * 按源 group 字段判断挑战活动并校验活动 ID。
     */
    private boolean isMatchingChallengeEvent(Map<String, Object> event, long expectedEventId) {
        Integer group = integerValue(event.get("group"));
        if (group == null) {
            return false;
        } else if (group != CardSyncConstants.KX_WIKI_CHALLENGE_GROUP_ID) {
            return false;
        } else if (number(event.get("id")) != expectedEventId) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 逐项验证挑战目录页包含活动数组和合法的 next 字段。
     */
    private boolean isValidChallengeCatalogResponse(Map<String, Object> response) {
        if (!(response.get("events") instanceof List<?>)) {
            return false;
        } else if (!response.containsKey("next")) {
            return false;
        } else if (response.get("next") != null && !(response.get("next") instanceof Number)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 按源 group 字段判断挑战活动。
     */
    private boolean isChallengeEvent(Map<String, Object> event) {
        Integer group = integerValue(event.get("group"));
        if (group == null) {
            return false;
        } else {
            return group == CardSyncConstants.KX_WIKI_CHALLENGE_GROUP_ID;
        }
    }

    /**
     * 上报活动及地图扫描进度。
     */
    private void updateProgress(int current, int total, long eventId, Long mapId) {
        int percent = Math.min(99, total == 0 ? 0 : current * 100 / total);
        String message = mapId == null ? "已处理挑战活动 " + eventId : "正在抓取活动 " + eventId + " 地图 " + mapId;
        SyncProgressContext.update(percent, "SYNC_CHALLENGE", message);
    }

    /**
     * 读取必需的数值型源 ID。
     */
    private long number(Object value) {
        if (!(value instanceof Number number)) {
            throw new IllegalArgumentException("Challenge response missing numeric ID");
        }
        return number.longValue();
    }

    /**
     * 将可选分页值转换为 Integer。
     */
    private Integer integerValue(Object value) {
        return value instanceof Number number ? number.intValue() : null;
    }

    /**
     * 将非对象 JSON 值转换为空字段映射。
     */
    private Map<String, Object> mapValue(Object value) {
        if (!(value instanceof Map<?, ?> source)) {
            return Map.of();
        }
        Map<String, Object> result = new LinkedHashMap<>();
        source.forEach((key, item) -> result.put(String.valueOf(key), item));
        return result;
    }

    /**
     * 将非数组 JSON 值转换为空列表。
     */
    private List<?> listValue(Object value) {
        return value instanceof List<?> values ? values : List.of();
    }

    /**
     * 部分地图或活动失败后的异常，保留失败计数供同步状态展示。
     **/
    public static class ChallengeSyncException extends RuntimeException {
        /**
         * 本轮挑战同步部分结果统计。
         **/
        private final ChallengeSyncResult result;

        /**
         * 创建包含同步计数的部分失败异常。
         */
        public ChallengeSyncException(String message, ChallengeSyncResult result) {
            super(message);
            this.result = result;
        }

        /**
         * 返回部分结果计数。
         */
        public ChallengeSyncResult getResult() {
            return result;
        }
    }
}
