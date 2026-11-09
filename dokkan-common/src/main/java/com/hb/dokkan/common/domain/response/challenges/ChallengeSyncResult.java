package com.hb.dokkan.common.domain.response.challenges;

import lombok.Data;

import java.io.Serializable;

/**
 * 活动挑战同步过程的结果计数。
 **/
@Data
public class ChallengeSyncResult implements Serializable {
    /**
     * 序列化版本标识。
     **/
    private static final long serialVersionUID = 1L;

    /**
     * 本轮扫描的活动总数。
     **/
    private int eventTotal;

    /**
     * 已完成目录处理的活动数。
     **/
    private int eventProcessed;

    /**
     * 本轮读取的难度地图总数。
     **/
    private int mapTotal;

    /**
     * 已接受的完整敌人快照数。
     **/
    private int capturedCount;

    /**
     * 源站声明部分采集的地图数。
     **/
    private int partialCount;

    /**
     * 源站尚未采集敌人数据的地图数。
     **/
    private int notCapturedCount;

    /**
     * 请求、响应校验或活动目录处理失败数。
     **/
    private int failedCount;

    /**
     * 内容未变且复用已有快照的地图数。
     **/
    private int unchangedCount;
}
