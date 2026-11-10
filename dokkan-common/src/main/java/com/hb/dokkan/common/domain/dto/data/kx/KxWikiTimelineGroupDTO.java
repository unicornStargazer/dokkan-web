package com.hb.dokkan.common.domain.dto.data.kx;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

/**
 * @Description 时间线一天的卡片开放记录
 * @Author Codex
 * @Date 2026/11/10
 **/
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KxWikiTimelineGroupDTO {
    /**
     * 源站分组日期，ISO yyyy-MM-dd。
     **/
    private String date;
    /**
     * 当天的新卡、极限和超极限开放记录。
     **/
    private List<KxWikiCardChangeDTO> items;
    /**
     * 当天记录总数，用于校验补取是否完整。
     **/
    private Integer total;
    /**
     * 列表是否截断；为 true 时必须按日期补取。
     **/
    private Boolean truncated;
}
