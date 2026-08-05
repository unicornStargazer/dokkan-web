package com.hb.dokkan.common.domain.dto.data.dokkandb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/** Base, hidden-potential and EZA stats returned by DokkanDB. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DokkanDbCardStatsDTO {
    @JsonProperty("card_id")
    private Long cardId;
    @JsonProperty("base_lv_max")
    private Integer baseLvMax;
    @JsonProperty("hp_max")
    private Long hpMax;
    @JsonProperty("atk_max")
    private Long atkMax;
    @JsonProperty("def_max")
    private Long defMax;
    @JsonProperty("eza_lv_max")
    private Integer ezaLvMax;
    @JsonProperty("hp_max_eza")
    private Long hpMaxEza;
    @JsonProperty("atk_max_eza")
    private Long atkMaxEza;
    @JsonProperty("def_max_eza")
    private Long defMaxEza;
    @JsonProperty("hp_table_max")
    private Map<String, Long> hpTableMax;
    @JsonProperty("atk_table_max")
    private Map<String, Long> atkTableMax;
    @JsonProperty("def_table_max")
    private Map<String, Long> defTableMax;
    @JsonProperty("hp_table_eza")
    private Map<String, Long> hpTableEza;
    @JsonProperty("atk_table_eza")
    private Map<String, Long> atkTableEza;
    @JsonProperty("def_table_eza")
    private Map<String, Long> defTableEza;
}
