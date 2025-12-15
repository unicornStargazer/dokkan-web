package com.hb.dokkan.common.domain.po.mysql.link;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hb.dokkan.common.domain.po.mysql.base.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Description 链接数据库实体类
 * @Author stargazer
 * @Date 2025/11/9 14:04
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("link")
public class DokkanLinkPO extends BasePO {

    /**
     * 链接id
     */
    private Long linkId;

    /**
     * 链接名称
     */
    private String linkName;
    /**
     * 一级描述
     */
    @TableField("level_1_description")
    private String level1Description;
    /**
     * 满级描述
     */
    @TableField("level_10_description")
    private String level10Description;

}
