package com.hb.dokkan.infrastructure.mysql.links.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hb.dokkan.common.domain.BasePO;
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
    private String level_1_description;
    /**
     * 满级描述
     */
    private String level_10_description;

}
