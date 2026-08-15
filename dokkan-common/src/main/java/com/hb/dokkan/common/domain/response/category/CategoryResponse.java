package com.hb.dokkan.common.domain.response.category;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description 分类管理响应
 * @Author stargazer
 * @Date 2026/8/16 00:30
 **/
@Data
public class CategoryResponse implements Serializable {

    /** 序列化版本号，保证响应对象跨版本反序列化兼容。 */
    private static final long serialVersionUID = 1L;

    /** 分类主键 ID。 */
    private String id;

    /** DokkanDB 分类 ID。 */
    private Long categoryId;

    /** 中文分类名。 */
    private String categoryName;

    /** 英文分类名。 */
    private String categoryNameEn;

    /** 发布时间。 */
    private Date publishTime;

    /** 创建时间。 */
    private Date createTime;

    /** 更新时间。 */
    private Date updateTime;
}
