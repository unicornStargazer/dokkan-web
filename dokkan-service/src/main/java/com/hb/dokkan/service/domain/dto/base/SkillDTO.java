package com.hb.dokkan.service.domain.dto.base;

import lombok.Data;

import java.io.Serializable;

/**
 * 技能信息
 */
@Data
public class SkillDTO implements Serializable {
    private static final long serialVersionUID = -1599843874551651390L;
    /**
     * skillId
     */
    private Integer skillId;
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
