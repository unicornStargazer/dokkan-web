package com.hb.dokkan.common.domain;

import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * @Description 分页响应类
 * @Author stargazer
 * @Date 2025/12/10 22:01
 **/
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> implements Serializable {
    private static final long serialVersionUID = 7609050980764595400L;
    /**
     * 数据列表
     */
    private List<T> data;
    /**
     * 当前页码
     */
    private int currentPage;
    /**
     * 每页数量
     */
    private int pageSize;
    /**
     * 总记录数
     */
    private long total;
}
