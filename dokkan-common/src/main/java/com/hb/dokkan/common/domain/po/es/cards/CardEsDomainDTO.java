package com.hb.dokkan.common.domain.po.es.cards;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description 详情页展示的领域及其与当前技能的关系。
 * @Author Codex
 * @Date 2026/11/8
 **/
@Data
public class CardEsDomainDTO implements Serializable {
    /**
     * 序列化版本标识。
     **/
    private static final long serialVersionUID = 1L;
    /**
     * 领域业务 ID。
     **/
    private Long id;
    /**
     * 领域名称。
     **/
    private String name;
    /**
     * 领域效果描述，优先使用源站模板。
     **/
    private String description;
    /**
     * 领域图像资源 ID。
     **/
    private Long resourceId;
    /**
     * 领域持续回合数，未知时为空。
     **/
    private Integer turns;
    /**
     * 当前技能是否展开该领域。
     **/
    private Boolean creates;
    /**
     * 当前技能是否需要该领域。
     **/
    private Boolean needed;
    /**
     * 领域缩略图 URL。
     **/
    private String thumbnail;
}
