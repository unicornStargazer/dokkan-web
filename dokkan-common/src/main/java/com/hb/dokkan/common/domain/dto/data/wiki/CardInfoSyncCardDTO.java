package com.hb.dokkan.common.domain.dto.data.wiki;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

/**
 * @Description card-info 网站模型
 * @Author stargazer
 * @Date 2025/6/2 1:06
 **/
@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardInfoSyncCardDTO {

    @JsonProperty("id")
    private long id; // ID 通常用 long 更安全

    @JsonProperty("name")
    private String name;

    @JsonProperty("rarity")
    private int rarity;

    @JsonProperty("lv_max")
    private int lvMax;

    @JsonProperty("optimal_awakening_grow_type")
    private String optimalAwakeningGrowType; // 类型未知，假设为String，若为数字则用Integer

    @JsonProperty("optimal_awakening_step")
    private Integer optimalAwakeningStep; // 类型未知，假设为Integer，允许null

    @JsonProperty("hp_init")
    private int hpInit;

    @JsonProperty("hp_max")
    private int hpMax;

    @JsonProperty("hp_hipo")
    private int hpHipo;

    @JsonProperty("atk_init")
    private int atkInit;

    @JsonProperty("atk_max")
    private int atkMax;

    @JsonProperty("atk_hipo")
    private int atkHipo;

    @JsonProperty("def_init")
    private int defInit;

    @JsonProperty("def_max")
    private int defMax;

    @JsonProperty("def_hipo")
    private int defHipo;

    @JsonProperty("avg_init")
    private int avgInit;

    @JsonProperty("avg_max")
    private int avgMax;

    @JsonProperty("avg_hipo")
    private int avgHipo;

    @JsonProperty("element")
    private String element;

    @JsonProperty("skill_lv_max")
    private int skillLvMax;

    @JsonProperty("resource_id")
    private Integer resourceId; // 允许null

    @JsonProperty("bg_effect_id")
    private Integer bgEffectId; // 允许null

    @JsonProperty("open_at")
    private long openAt; // Unix timestamp

    @JsonProperty("eza")
    private int eza; // 或者 boolean，如果 0 代表 false, 非0 代表 true

    @JsonProperty("eza_open_at")
    private Long ezaOpenAt; // Unix timestamp, 允许null

    @JsonProperty("awoken_max")
    private int awokenMax;

    @JsonProperty("bg_element")
    private String bgElement;

    @JsonProperty("icon_id")
    private int iconId;

    @JsonProperty("raw_attributes")
    private List<Integer> rawAttributes;






}
