package com.hb.dokkan.common.domain.dto.data.kx;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

/**
 * @Description 按日期倒序分页的卡片开放时间线
 * @Author Codex
 * @Date 2026/11/10
 **/
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KxWikiTimelineDTO {
    /**
     * 当前地区和语言是否支持时间线。
     **/
    private Boolean available;
    /**
     * 当前查询的总页数；按日期查询通常为一页。
     **/
    private Integer pages;
    /**
     * 按日期倒序排列的开放记录分组。
     **/
    private List<KxWikiTimelineGroupDTO> groups;
}
