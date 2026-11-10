package com.hb.dokkan.common.domain.dto.data.kx;

import lombok.Data;

/**
 * @Description 最近成功处理的时间线日期和当天内容指纹
 * @Author Codex
 * @Date 2026/11/10
 **/
@Data
public class KxWikiTimelineCheckpointDTO {
    /**
     * 已成功扫描的最新开放日期；空时间线时为空。
     **/
    private String date;
    /**
     * 最新日期的完整记录指纹，识别同一天的补充和修订。
     **/
    private String hash;
}
