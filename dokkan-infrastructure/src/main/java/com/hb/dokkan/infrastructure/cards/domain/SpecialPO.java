package com.hb.dokkan.infrastructure.cards.domain;

import com.hb.dokkan.common.domain.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @description 必杀表
 * @author BEJSON
 * @date 2025-09-14
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SpecialPO extends BasePO implements Serializable {

    private static final long serialVersionUID = 499775237392294039L;

    /**
    * 必杀id
    */
    private Long specialId;

    /**
    * 必杀描述
    */
    private String description;

    /**
    * 提升倍率
    */
    private Integer increaseRate;

    /**
    * 每级提升
    */
    private Integer lvBonus;

    /**
    * 类型
    */
    private String style;

    /**
    * 必杀开始等级
    */
    private Integer lvStart;

    /**
    * 触发气力
    */
    private Integer eballNumStart;

    /**
    * 必杀分类id
    */
    private Long specialCategoryId;

    /**
    * 必杀分类名称
    */
    private String specialCategoryName;

    /**
    * 必杀特殊加成1
    */
    private String specialBonus1;

    /**
    * 必杀特殊加成2
    */
    private String specialBonus2;

    /**
    * 必杀特殊加成1触发等级
    */
    private Integer specialBonus1Lv;

    /**
    * 必杀特殊加成2触发等级
    */
    private Integer specialBonus2Lv;

    /**
    * 扩展属性
    */
    private String attributes;
}