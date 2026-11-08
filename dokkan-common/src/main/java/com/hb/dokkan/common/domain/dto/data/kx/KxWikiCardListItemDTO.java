package com.hb.dokkan.common.domain.dto.data.kx;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import java.util.*;
import java.io.Serializable;

/**
 * @Description KX Wiki KxWikiCardListItemDTO 响应模型
 * @Author Codex
 * @Date 2026/11/8
 **/
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KxWikiCardListItemDTO implements Serializable {
    /**
     * 源站业务 ID。
     **/
    private Long id;
    /**
     * 源站稀有度编码，4 为 UR，5 为 LR。
     **/
    private Integer rarity;
    /**
     * 源站属性编码，十位表示超极系，个位表示具体属性。
     **/
    private Integer element;
    /**
     * 队伍编成所需的 cost。
     **/
    private Integer cost;
    /**
     * 源站是否存在 EZA 状态。
     **/
    private Boolean hasEza;
    /**
     * 源站是否存在 SEZA 状态。
     **/
    private Boolean hasSeza;
    /**
     * 当前地区的卡片开放时间，保留源站日期文本。
     **/
    private String releaseAt;
    /**
     * 源站头像 URL。
     **/
    private String thumbnail;
}
