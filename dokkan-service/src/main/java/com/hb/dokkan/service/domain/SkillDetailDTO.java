package com.hb.dokkan.service.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * @Description xxxxx
 * @Author stargazer
 * @Date 2025/6/2 16:22
 **/
@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SkillDetailDTO {

    private Long id;

    private String name;

    private String description;

    @JsonProperty("increase_rate")
    private Number increaseRate;

    @JsonProperty("lv_bonus")
    private Number lvBonus;

    private String style;

    @JsonProperty("special_bonus_1")
    private String specialBonus1;

    @JsonProperty("special_bonus_2")
    private String specialBonus2;
}
