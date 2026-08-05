package com.hb.dokkan.service.job.sync;

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

/** Owns asynchronous sync jobs and exposes thread-safe progress snapshots. */
@Slf4j
@Component
public class SyncProgressService {
    private final Map<String, SyncProgressResponse> jobs = new ConcurrentHashMap<>();

    @Resource
    private SyncDataService syncDataService;

    @Resource
    private DokkanThreadPoolExecutor executor;

    public String start(SyncStartRequest request) {
        String type = request == null || request.getType() == null ? "" : request.getType().trim().toLowerCase();
        if (!("mysql".equals(type) || "es".equals(type) || "manual".equals(type))) {
            throw new DokkanBizException("sync type must be mysql, es or manual");
        }
        List<Long> ids = request == null ? null : request.getCardIds();
        if ("manual".equals(type) && (CollectionUtils.isEmpty(ids) || ids.size() > 100)) {
            throw new DokkanBizException("manual sync requires 1-100 cardIds");
        }
        String jobId = UUID.randomUUID().toString();
        SyncProgressResponse progress = new SyncProgressResponse();
        progress.setJobId(jobId);
        progress.setType(type);
        progress.setStatus("PENDING");
        progress.setStage("等待执行");
        progress.setMessage("任务已创建");
        progress.setTotal("manual".equals(type) ? ids.size() : 100);
        jobs.put(jobId, progress);

        executor.execute(() -> run(jobId, type, ids));
        log.info("[sync-job:{}] accepted, type={}, total={}", jobId, type, progress.getTotal());
        return jobId;
    }

    public SyncProgressResponse get(String jobId) {
        return jobs.get(jobId);
    }

    private void run(String jobId, String type, List<Long> ids) {
        SyncProgressResponse progress = jobs.get(jobId);
        if (progress == null) return;
        progress.setStatus("RUNNING");
        progress.setStartedAt(System.currentTimeMillis());
        SyncProgressContext.bind((percent, stage, message) -> update(progress, percent, 100, stage, message));
        try {
            update(progress, 5, 100, "准备同步", "正在初始化同步任务");
            if ("mysql".equals(type)) {
                update(progress, 10, 100, "抓取外部数据", "正在从 DokkanDB 获取并翻译卡片数据");
                syncDataService.initCard();
                update(progress, 98, 100, "完成校验", "正在确认 MySQL、ES 和头像同步结果");
            } else if ("es".equals(type)) {
                update(progress, 10, 100, "读取 MySQL", "正在读取卡片及关联数据");
                syncDataService.syncEsCardData();
                update(progress, 90, 100, "写入 Elasticsearch", "ES 文档正在提交");
            } else {
                int total = ids.size();
                update(progress, 10, total, "抓取指定卡片", "正在获取 " + total + " 个 cardId");
                int synced = syncDataService.syncCardsByIds(ids);
                update(progress, synced, total, "写入完成", "已同步 " + synced + " 个卡片");
            }
            update(progress, progress.getTotal(), progress.getTotal(), "已完成", "同步成功");
            progress.setStatus("SUCCESS");
            log.info("[sync-job:{}] completed, type={}, current={}/{}", jobId, type, progress.getCurrent(), progress.getTotal());
        } catch (Exception e) {
            progress.setStatus("FAILED");
            progress.setStage("同步失败");
            progress.setError(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
            progress.setMessage("同步失败，请查看服务端日志");
            log.error("[sync-job:{}] failed, type={}, error={}", jobId, type, e.getMessage(), e);
        } finally {
            SyncProgressContext.clear();
            progress.setFinishedAt(System.currentTimeMillis());
        }
    }

    private void update(SyncProgressResponse progress, int current, int total, String stage, String message) {
        progress.setTotal(Math.max(total, 1));
        progress.setCurrent(Math.max(0, Math.min(current, progress.getTotal())));
        progress.setPercent(Math.max(0, Math.min(100, (int) Math.round(progress.getCurrent() * 100f / progress.getTotal()))));
        progress.setStage(stage);
        progress.setMessage(message);
        log.info("[sync-job:{}] {} {}/{}%={}, message={}", progress.getJobId(), stage, progress.getCurrent(), progress.getTotal(), progress.getPercent(), message);
    }
}
