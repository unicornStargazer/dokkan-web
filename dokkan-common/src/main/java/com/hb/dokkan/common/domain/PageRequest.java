package com.hb.dokkan.common.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description 分页默认参数
 * @Author stargazer
 * @Date 2025/12/10 22:15
 **/
@Data
public class PageRequest implements Serializable {
    /**
     * 页码
     */
    private int pageNum = 1;

    /**
     * 每页数量
     */
    private int pageSize = 10;

}
