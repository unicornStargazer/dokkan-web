package com.hb.dokkan.common.domain.dto.data.kx;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import java.util.*;
import java.io.Serializable;

/**
 * @Description KX Wiki KxWikiEnrichmentDTO 响应模型
 * @Author Codex
 * @Date 2026/11/8
 **/
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KxWikiEnrichmentDTO implements Serializable {
    /**
     * 卡片业务 ID，本体卡和形态卡均使用源站 ID。
     **/
    private Long cardId;
    /**
     * 按 base、eza、seza 区分的卡片状态数据。
     **/
    private Map<String,KxWikiEnrichedStateDTO> states;
    /**
     * 本体卡对应的形态卡列表。
     **/
    private List<KxWikiEnrichedFormDTO> forms;
    /**
     * 客户端保留的原始 JSON，仅用于内容 hash，不进入数据库。
     **/
    @JsonIgnore
    private String sourceJson;
}
