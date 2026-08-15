package com.hb.dokkan.common.domain.request.category;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description 分类保存请求
 * @Author stargazer
 * @Date 2026/8/16 01:10
 **/
@Data
public class CategorySaveRequest implements Serializable {

    /** 序列化版本号，保证请求对象跨版本反序列化兼容。 */
    private static final long serialVersionUID = 1L;

    /** 分类主键 ID，新增时为空。 */
    private String id;

    /** DokkanDB 分类 ID。 */
    private Long categoryId;

    /** 中文分类名。 */
    private String categoryName;

    /** 英文分类名，用于技能描述翻译术语映射。 */
    private String categoryNameEn;

    /** 发布时间。 */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date publishTime;
}
