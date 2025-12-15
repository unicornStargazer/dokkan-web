package com.hb.dokkan.common.domain.po.mysql.category;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hb.dokkan.common.domain.po.mysql.base.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @Description 分类数据库类
 * @Author stargazer
 * @Date 2025/11/8 22:06
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("category")
public class DokkanCategoryPO extends BasePO {

    /**
     * 分类id
     */
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 发布时间
     */
    private Date publishTime;

}
