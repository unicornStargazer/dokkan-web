package com.hb.dokkan.common.domain.dto.data.kx;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description 被动及主动等技能关联的 KX Wiki 领域信息。
 * @Author Codex
 * @Date 2026/11/8
 **/
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KxWikiDomainDTO implements Serializable {
    /**
     * 序列化版本标识。
     **/
    private static final long serialVersionUID = 1L;
    /**
     * 领域业务 ID，同一领域可被多个技能引用。
     **/
    private Long id;
    /**
     * 领域名称。
     **/
    private String name;
    /**
     * 领域效果描述。
     **/
    private String description;
    /**
     * 领域描述模板，存在时优先展示。
     **/
    private String descriptionTemplate;
    /**
     * 领域图像资源 ID。
     **/
    private Long resourceId;
    /**
     * 领域持续回合数，未提供时为空。
     **/
    private Integer turns;
    /**
     * 当前技能是否展开该领域。
     **/
    private Boolean creates;
    /**
     * 当前技能是否需要该领域生效。
     **/
    private Boolean needed;
    /**
     * 领域缩略图的源站地址。
     **/
    private String thumbnail;
}
