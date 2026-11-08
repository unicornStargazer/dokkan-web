package com.hb.dokkan.common.domain.dto.data.kx;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import java.util.*;
import java.io.Serializable;

/**
 * @Description KX Wiki KxWikiCardDetailDTO 响应模型
 * @Author Codex
 * @Date 2026/11/8
 **/
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KxWikiCardDetailDTO implements Serializable {
    /**
     * 源站业务 ID。
     **/
    private Long id;
    /**
     * 显示名称。
     **/
    private String name;
    /**
     * 含形态补充说明的卡片全名。
     **/
    private String unitName;
    /**
     * 卡片标题，缺失时使用 base 状态队长技名称。
     **/
    private String title;
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
     * 源站角色 ID。
     **/
    private Integer characterId;
    /**
     * 源站头像 URL。
     **/
    private String thumbnail;
    /**
     * 卡片潜力加成预设。
     **/
    private KxWikiPotentialDTO potential;
    /**
     * 卡片首次开放的时间信息。
     **/
    private KxWikiReleaseDTO firstReleased;
    /**
     * 按 base、eza、seza 区分的卡片状态数据。
     **/
    private List<KxWikiStateDTO> states;
    /**
     * 本体卡对应的形态卡列表。
     **/
    private List<KxWikiFormDTO> forms;
    /**
     * 客户端保留的原始 JSON，仅用于内容 hash，不进入数据库。
     **/
    @JsonIgnore
    private String sourceJson;
}
