package com.hb.dokkan.infrastructure.cards.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hb.dokkan.common.domain.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @description 技能表
 * @author BEJSON
 * @date 2025-09-14
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("skill")
public class SkillPO extends BasePO implements Serializable {

    private static final long serialVersionUID = -3273275273891644085L;

    /**
    * 技能id
    */
    private String skillId;

    /**
    * 名称
    */
    private String name;

    /**
    * 发动条件
    */
    private String conditionDescription;

    /**
    * 效果描述
    */
    private String effectDescription;

    /**
    * 标签
    */
    private String label;

    /**
    * 提升倍率
    */
    private Integer increaseRate;

    /**
    * 类别id
    */
    private Integer specialCategoryId;

    /**
    * 类别名称
    */
    private String specialCategoryName;

    /**
    * 扩展属性
    */
    private String attributes;

}