package com.hb.dokkan.common.domain.dto.data.wiki;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("condition_description")
    private String conditionDescription;
    /**
     * 效果描述
     */
    @JsonProperty("effect_description")
    private String effectDescription;
    /**
     * 标签
     */
    private String label;
    /**
     * 提升倍率
     */
    @JsonProperty("increase_rate")
    private Integer increaseRate;
    /**
     * 类别id
     */
    @JsonProperty("special_category_id")
    private Integer specialCategoryId;
    /**
     * 类别名称
     */
    @JsonProperty("special_category_name")
    private String specialCategoryName;
}
