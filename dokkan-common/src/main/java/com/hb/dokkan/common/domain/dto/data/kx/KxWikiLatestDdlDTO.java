package com.hb.dokkan.common.domain.dto.data.kx;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import java.util.List;

/**
 * @Description 国际服游戏数据库的最新更新批次
 * @Author Codex
 * @Date 2026/11/10
 **/
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KxWikiLatestDdlDTO {
    /**
     * 源批次稳定指纹，整轮成功后才记录。
     **/
    private String fingerprint;
    /**
     * 本批新增的卡片；空数组表示没有新卡。
     **/
    private List<KxWikiCardChangeDTO> newUnits;
    /**
     * 本批新增极限或超极限路线的已有卡片。
     **/
    private List<KxWikiCardChangeDTO> newEzas;
}
