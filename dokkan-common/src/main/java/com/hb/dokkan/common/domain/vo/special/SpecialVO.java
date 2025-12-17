package com.hb.dokkan.common.domain.vo.special;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description 必杀VO
 * @Author stargazer
 * @Date 2025/12/17 21:56
 **/
@Data
public class SpecialVO implements Serializable {
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
}
