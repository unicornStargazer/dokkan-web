package com.hb.dokkan.service.job.sync;

import com.hb.dokkan.common.constants.CardSyncConstants;
import com.hb.dokkan.common.domain.request.data.SyncStartRequest;
import com.hb.dokkan.common.domain.response.data.SyncProgressResponse;
import com.hb.dokkan.common.exception.domain.DokkanBizException;
import com.hb.dokkan.config.thread.DokkanThreadPoolExecutor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description 同步进度服务
 * @Author stargazer
 * @Date 2026/8/15 21:20
 **/
@Slf4j
@Component
public class SyncProgressService {

    /** 同步任务快照缓存，key 为 jobId。 */
    private final Map<String, SyncProgressResponse> jobs = new ConcurrentHashMap<>();

    @Resource
    private SyncDataService syncDataService;

    @Resource
    private DokkanThreadPoolExecutor executor;

    /**
     * 创建并启动异步同步任务。
     *
     * @param request 同步启动请求
     * @return 同步任务 ID
     */
    public String start(SyncStartRequest request) {
        String type = normalizeSyncType(request);
        validateSyncRequest(type, request);
        String jobId = UUID.randomUUID().toString();
        SyncProgressResponse progress = buildInitialProgress(jobId, type, request);
        jobs.put(jobId, progress);

        // 将实际同步提交到业务线程池，接口立即返回 jobId 供前端轮询。
        executor.execute(() -> run(jobId, type, request == null ? null : request.getCardIds()));
        log.info("[sync-job:{}] accepted, type={}, total={}", jobId, type, progress.getTotal());
        return jobId;
    }

    /**
     * 查询同步任务进度快照。
     *
     * @param jobId 同步任务 ID
     * @return 进度快照，不存在时返回 null
     */
    public SyncProgressResponse get(String jobId) {
        return jobs.get(jobId);
    }

    /**
     * 在线程池中执行同步任务并更新进度。
     *
     * @param jobId 同步任务 ID
     * @param type  同步类型
     * @param ids   手动同步卡片 ID 列表
     */
    private void run(String jobId, String type, List<Long> ids) {
        SyncProgressResponse progress = jobs.get(jobId);
        if (progress == null) {
            return;
        }
        progress.setStatus(CardSyncConstants.SYNC_STATUS_RUNNING);
        progress.setStartedAt(System.currentTimeMillis());
        SyncProgressContext.bind((percent, stage, message) -> update(progress, percent,
                CardSyncConstants.SYNC_PROGRESS_TOTAL_PERCENT, stage, message));
        try {
            // 先进入准备阶段，再根据同步类型分发到具体同步链路。
            update(progress, CardSyncConstants.SYNC_PROGRESS_START, CardSyncConstants.STAGE_PREPARE_SYNC,
                    CardSyncConstants.MESSAGE_INIT_SYNC_TASK);
            if (CardSyncConstants.SYNC_TYPE_MYSQL.equals(type)) {
                runMysqlSync(progress);
            } else if (CardSyncConstants.SYNC_TYPE_ES.equals(type)) {
                runEsSync(progress);
            } else {
                runManualSync(progress, ids);
            }
            update(progress, progress.getTotal(), progress.getTotal(),
                    CardSyncConstants.STAGE_COMPLETED, CardSyncConstants.MESSAGE_SYNC_SUCCESS);
            progress.setStatus(CardSyncConstants.SYNC_STATUS_SUCCESS);
            log.info("[sync-job:{}] completed, type={}, current={}/{}",
                    jobId, type, progress.getCurrent(), progress.getTotal());
        } catch (Exception e) {
            progress.setStatus(CardSyncConstants.SYNC_STATUS_FAILED);
            progress.setStage(CardSyncConstants.STAGE_SYNC_FAILED);
            progress.setError(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
            progress.setMessage(CardSyncConstants.MESSAGE_SYNC_FAILED);
            log.error("[sync-job:{}] failed, type={}, error={}", jobId, type, e.getMessage(), e);
        } finally {
            SyncProgressContext.clear();
            progress.setFinishedAt(System.currentTimeMillis());
        }
    }

    /**
     * 执行 MySQL 增量同步任务。
     *
     * @param progress 进度快照
     */
    private void runMysqlSync(SyncProgressResponse progress) {
        update(progress, CardSyncConstants.SYNC_PROGRESS_FETCH_START, CardSyncConstants.STAGE_FETCH_EXTERNAL,
                CardSyncConstants.MESSAGE_FETCH_DOKKAN_DB);
        syncDataService.initCard();
        update(progress, CardSyncConstants.SYNC_PROGRESS_FINAL_CHECK, CardSyncConstants.STAGE_FINAL_CHECK,
                CardSyncConstants.MESSAGE_FINAL_CHECK);
    }

    /**
     * 执行 ES 全量同步任务。
     *
     * @param progress 进度快照
     */
    private void runEsSync(SyncProgressResponse progress) {
        update(progress, CardSyncConstants.SYNC_PROGRESS_FETCH_START, CardSyncConstants.STAGE_READ_MYSQL,
                CardSyncConstants.MESSAGE_READ_CARD_DATA);
        syncDataService.syncEsCardData();
        update(progress, CardSyncConstants.SYNC_PROGRESS_ES_WRITE, CardSyncConstants.STAGE_WRITE_ES,
                CardSyncConstants.MESSAGE_ES_SUBMITTING);
    }

    /**
     * 执行指定卡片手动同步任务。
     *
     * @param progress 进度快照
     * @param ids      卡片 ID 列表
     */
    private void runManualSync(SyncProgressResponse progress, List<Long> ids) {
        int total = ids.size();
        update(progress, CardSyncConstants.SYNC_PROGRESS_MANUAL_FETCH, total,
                CardSyncConstants.STAGE_FETCH_MANUAL_CARD,
                CardSyncConstants.MESSAGE_FETCH_MANUAL_CARD_PREFIX + total + CardSyncConstants.MESSAGE_CARD_ID_SUFFIX);
        int synced = syncDataService.syncCardsByIds(ids);
        update(progress, synced, total, CardSyncConstants.STAGE_WRITE_COMPLETED,
                CardSyncConstants.MESSAGE_SYNCED_PREFIX + synced + CardSyncConstants.MESSAGE_CARD_COUNT_SUFFIX);
    }

    /**
     * 更新同步进度并裁剪进度边界。
     *
     * @param progress 进度快照
     * @param current  当前进度值
     * @param stage    当前阶段
     * @param message  当前消息
     */
    private void update(SyncProgressResponse progress, int current, String stage, String message) {
        update(progress, current, CardSyncConstants.SYNC_PROGRESS_TOTAL_PERCENT, stage, message);
    }

    /**
     * 更新同步进度并裁剪进度边界。
     *
     * @param progress 进度快照
     * @param current  当前进度值
     * @param total    总进度值
     * @param stage    当前阶段
     * @param message  当前消息
     */
    private void update(SyncProgressResponse progress, int current, int total, String stage, String message) {
        progress.setTotal(Math.max(total, CardSyncConstants.DOKKAN_DB_DEFAULT_CHUNK));
        progress.setCurrent(Math.max(0, Math.min(current, progress.getTotal())));
        progress.setPercent(Math.max(0, Math.min(CardSyncConstants.SYNC_PROGRESS_TOTAL_PERCENT,
                (int) Math.round(progress.getCurrent() * 100f / progress.getTotal()))));
        progress.setStage(stage);
        progress.setMessage(message);
        log.info("[sync-job:{}] {} {}/{}%={}, message={}", progress.getJobId(), stage,
                progress.getCurrent(), progress.getTotal(), progress.getPercent(), message);
    }

    /**
     * 规范化同步类型。
     *
     * @param request 同步启动请求
     * @return 小写同步类型
     */
    private String normalizeSyncType(SyncStartRequest request) {
        return request == null || request.getType() == null
                ? CardSyncConstants.EMPTY_TEXT : request.getType().trim().toLowerCase();
    }

    /**
     * 校验同步启动请求。
     *
     * @param type    同步类型
     * @param request 同步启动请求
     */
    private void validateSyncRequest(String type, SyncStartRequest request) {
        if (!(CardSyncConstants.SYNC_TYPE_MYSQL.equals(type)
                || CardSyncConstants.SYNC_TYPE_ES.equals(type)
                || CardSyncConstants.SYNC_TYPE_MANUAL.equals(type))) {
            throw new DokkanBizException(CardSyncConstants.SYNC_TYPE_ERROR_MESSAGE);
        }
        List<Long> ids = request == null ? null : request.getCardIds();
        if (CardSyncConstants.SYNC_TYPE_MANUAL.equals(type)
                && (CollectionUtils.isEmpty(ids) || ids.size() > CardSyncConstants.MANUAL_SYNC_MAX_CARD_COUNT)) {
            throw new DokkanBizException(CardSyncConstants.MANUAL_SYNC_ERROR_MESSAGE);
        }
    }

    /**
     * 构建初始进度快照。
     *
     * @param jobId   同步任务 ID
     * @param type    同步类型
     * @param request 同步启动请求
     * @return 初始进度快照
     */
    private SyncProgressResponse buildInitialProgress(String jobId, String type, SyncStartRequest request) {
        List<Long> ids = request == null ? null : request.getCardIds();
        SyncProgressResponse progress = new SyncProgressResponse();
        progress.setJobId(jobId);
        progress.setType(type);
        progress.setStatus(CardSyncConstants.SYNC_STATUS_PENDING);
        progress.setStage(CardSyncConstants.STAGE_WAITING);
        progress.setMessage(CardSyncConstants.MESSAGE_TASK_CREATED);
        progress.setTotal(CardSyncConstants.SYNC_TYPE_MANUAL.equals(type)
                ? ids.size() : CardSyncConstants.SYNC_PROGRESS_TOTAL_PERCENT);
        return progress;
    }
}
