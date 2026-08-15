package com.hb.dokkan.common.domain.request.category;

import com.hb.dokkan.common.domain.request.base.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Description 分类分页查询请求
 * @Author stargazer
 * @Date 2026/8/16 00:30
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class CategoryQueryRequest extends PageRequest implements Serializable {

    /** 序列化版本号，保证请求对象跨版本反序列化兼容。 */
    private static final long serialVersionUID = 1L;

    /** 关键字，匹配分类 ID、中文分类名或英文分类名。 */
    private String keyword;

    /** 分类 ID。 */
    private Long categoryId;

    /** 中文分类名。 */
    private String categoryName;

    /** 英文分类名。 */
    private String categoryNameEn;
}
