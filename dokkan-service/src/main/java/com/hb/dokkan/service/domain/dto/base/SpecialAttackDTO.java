package com.hb.dokkan.service.domain.dto.base;

import lombok.*;

import java.io.Serializable;

/**
 * @Description 必杀
 * @Author stargazer
 * @Date 2025/6/2 16:22
 **/
@Setter
@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class SpecialAttackDTO implements Serializable {
    private static final long serialVersionUID = -749267422876885930L;
    /**
     * 必杀id
     */
    private Long specialId;
    /**
     * 必杀名称
     */
    private String name;
    /**
     * 必杀效果描述
     */
    private String description;
    /**
     * 初始提升倍率
     */
    private Integer increaseRate;
    /**
     * 每个等级提升
     */
    private int lvBonus;
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
    private Integer specialCategoryId;
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
