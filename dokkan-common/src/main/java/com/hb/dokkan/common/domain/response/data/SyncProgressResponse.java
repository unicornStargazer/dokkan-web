package com.hb.dokkan.common.domain.response.data;

import lombok.Data;

import java.io.Serializable;

/** Snapshot returned to the admin UI while a sync job is running. */
@Data
public class SyncProgressResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private volatile String jobId;
    private volatile String type;
    private volatile String status;
    private volatile String stage;
    private volatile String message;
    private volatile String error;
    private volatile int current;
    private volatile int total;
    private volatile int percent;
    private volatile long startedAt;
    private volatile long finishedAt;

    /**
     * 挑战同步已处理的活动数量。
     **/
    private volatile int eventProcessed;

    /**
     * 挑战同步的活动总数。
     **/
    private volatile int eventTotal;

    /**
     * 挑战同步已处理的地图数量。
     **/
    private volatile int mapProcessed;

    /**
     * 挑战同步的地图总数。
     **/
    private volatile int mapTotal;

    /**
     * 已写入的完整敌人快照数量。
     **/
    private volatile int capturedCount;

    /**
     * 源站部分采集的地图数量。
     **/
    private volatile int partialCount;

    /**
     * 源站未采集敌人数据的地图数量。
     **/
    private volatile int notCapturedCount;

    /**
     * 地图或活动处理失败数量。
     **/
    private volatile int failedCount;

    /**
     * 内容未变化并复用已有快照的地图数量。
     **/
    private volatile int unchangedCount;
}
