package com.hb.dokkan.common.domain.vo.skill;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description 技能VO类
 * @Author stargazer
 * @Date 2025/12/17 21:57
 **/
@Data
public class SkillVO implements Serializable {
    private static final long serialVersionUID = -4703083016796694475L;

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
}
