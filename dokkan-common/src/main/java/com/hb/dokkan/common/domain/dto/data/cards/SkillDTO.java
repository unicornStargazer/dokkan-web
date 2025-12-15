package com.hb.dokkan.common.domain.dto.data.cards;

import com.google.common.base.Objects;
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


    @Override
    public boolean equals(Object object) {
        if (!(object instanceof SkillDTO skillDTO)) return false;
        return Objects.equal(skillId, skillDTO.skillId) && Objects.equal(name, skillDTO.name) && Objects.equal(conditionDescription, skillDTO.conditionDescription) && Objects.equal(effectDescription, skillDTO.effectDescription) && Objects.equal(label, skillDTO.label) && Objects.equal(increaseRate, skillDTO.increaseRate) && Objects.equal(specialCategoryId, skillDTO.specialCategoryId) && Objects.equal(specialCategoryName, skillDTO.specialCategoryName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(skillId, name, conditionDescription, effectDescription, label, increaseRate, specialCategoryId, specialCategoryName);
    }
}
