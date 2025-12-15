package com.hb.dokkan.common.domain.dto.data.wiki;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

/**
 * @Description 必杀/主动技/下拉
 * @Author stargazer
 * @Date 2025/6/2 16:22
 **/
@Setter
@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class WikiSpecialAttackDTO implements Serializable {
    private static final long serialVersionUID = -749267422876885930L;
    /**
     * 必杀id
     */
    private Long id;
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
    @JsonProperty("increase_rate")
    private Integer increaseRate;
    /**
     * 每个等级提升
     */
    @JsonProperty("lv_bonus")
    private int lvBonus;
    /**
     * 类型
     */
    private String style;
    /**
     * 必杀开始等级
     */
    @JsonProperty("lv_start")
    private Integer lvStart;
    /**
     * 触发气力
     */
    @JsonProperty("eball_num_start")
    private Integer eballNumStart;

    /**
     * 必杀分类id
     */
    @JsonProperty("special_category_id")
    private Integer specialCategoryId;
    /**
     * 必杀分类名称
     */
    @JsonProperty("special_category_name")
    private String specialCategoryName;
    /**
     * 必杀特殊加成1
     */
    @JsonProperty("special_bonus_1")
    private String specialBonus1;
    /**
     * 必杀特殊加成2
     */
    @JsonProperty("special_bonus_2")
    private String specialBonus2;
    /**
     * 必杀特殊加成1触发等级
     */
    @JsonProperty("special_bonus_1_lv")
    private Integer specialBonus1Lv;
    /**
     * 必杀特殊加成2触发等级
     */
    @JsonProperty("special_bonus_2_lv")
    private Integer specialBonus2Lv;

}
