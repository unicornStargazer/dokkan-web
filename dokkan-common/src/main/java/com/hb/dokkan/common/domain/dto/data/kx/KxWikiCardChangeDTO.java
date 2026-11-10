package com.hb.dokkan.common.domain.dto.data.kx;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

/**
 * @Description 卡片更新信号，兼容最新批次和时间线条目
 * @Author Codex
 * @Date 2026/11/10
 **/
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KxWikiCardChangeDTO {
    /**
     * 需要重新核对详情的源卡片 ID。
     **/
    private Long id;
    /**
     * 源站稀有度编码，DDL 候选可独立于已发布目录按 UR/LR 范围筛选。
     **/
    private Integer rarity;
    /**
     * 开放类型，源站使用 unit、eza 或 seza。
     **/
    private String releaseKind;
    /**
     * 开放时间，Unix 秒数；最新批次未提供时为空。
     **/
    private Long releaseAt;
    /**
     * 当前是否已开放极限，用于识别同日状态修订。
     **/
    private Boolean hasEza;
    /**
     * 当前是否已开放超极限，用于识别同日状态修订。
     **/
    private Boolean hasSeza;
}
