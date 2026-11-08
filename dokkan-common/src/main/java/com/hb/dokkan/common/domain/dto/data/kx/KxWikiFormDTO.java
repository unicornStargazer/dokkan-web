package com.hb.dokkan.common.domain.dto.data.kx;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import java.util.*;
import java.io.Serializable;

/**
 * @Description KX Wiki KxWikiFormDTO 响应模型
 * @Author Codex
 * @Date 2026/11/8
 **/
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KxWikiFormDTO implements Serializable {
    /**
     * 卡片业务 ID，本体卡和形态卡均使用源站 ID。
     **/
    private Long cardId;
    /**
     * 该形态变化前的卡片 ID。
     **/
    private Long fromCardId;
    /**
     * 源站形态类型，如 transformation、exchange 或 giant_rage。
     **/
    private String kind;
    /**
     * 显示名称。
     **/
    private String name;
    /**
     * 含形态补充说明的卡片全名。
     **/
    private String unitName;
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
     * 源站卡片资源 ID。
     **/
    private Long resourceId;
    /**
     * 源站头像 URL。
     **/
    private String thumbnail;
    /**
     * 按 base、eza、seza 区分的卡片状态数据。
     **/
    private List<KxWikiStateDTO> states;
}
