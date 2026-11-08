package com.hb.dokkan.common.domain.dto.data.kx;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * KX Wiki 分页响应。
 **/
@Data
public class KxWikiPageResponse<T> implements Serializable {
    /**
     * 当前页码，从 1 开始。
     **/
    private Integer page;
    /**
     * 源站返回的总页数。
     **/
    private Integer pages;
    /**
     * 符合查询条件的总记录数。
     **/
    private Integer total;
    /**
     * 是否还有下一页，为空表示源站未提供该标记。
     **/
    @JsonProperty("has_more")
    private Boolean hasMore;
    /**
     * 当前页的记录列表。
     **/
    private List<T> items;
}
