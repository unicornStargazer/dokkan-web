package com.hb.dokkan.common.domain.dto.data.kx;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import java.util.*;
import java.io.Serializable;

/**
 * @Description KX Wiki KxWikiTextDTO 响应模型
 * @Author Codex
 * @Date 2026/11/8
 **/
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KxWikiTextDTO implements Serializable {
    /**
     * 源站业务 ID。
     **/
    private Long id;
    /**
     * 显示名称。
     **/
    private String name;
    /**
     * 展示描述文本。
     **/
    private String description;
    /**
     * 保留源站占位符的描述模板，展示时优先使用。
     **/
    private String descriptionTemplate;
    /**
     * 满级链接技能描述。
     **/
    private String maxLevelDescription;
    /**
     * 保留源站占位符的满级链接描述模板。
     **/
    private String maxLevelDescriptionTemplate;
}
