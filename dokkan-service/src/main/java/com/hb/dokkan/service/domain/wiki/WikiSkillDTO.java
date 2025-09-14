package com.hb.dokkan.service.domain.wiki;

import lombok.*;

import java.io.Serializable;

/**
 * @Description 技能信息
 * @Author stargazer
 * @Date 2025/9/14 18:14
 **/
@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
public class WikiSkillDTO implements Serializable {
    private static final long serialVersionUID = -1599843874551651390L;
    /**
     * skillId
     */
    private Integer id;
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
