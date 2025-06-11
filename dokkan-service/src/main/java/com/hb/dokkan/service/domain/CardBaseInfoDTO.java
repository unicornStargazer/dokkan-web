package com.hb.dokkan.service.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Date;

/**
 * @Description xxxxx
 * @Author stargazer
 * @Date 2025/6/2 16:06
 **/
@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardBaseInfoDTO {

    private Long id;

    private String name;

    private Integer cost;

    /**
     * @see
     */
    private Integer rarity;

    @JsonProperty("element")
    private Integer propType;

    @JsonProperty("hp_max")
    private Long hpValue;

    @JsonProperty("atk_max")
    private Long atkValue;

    @JsonProperty("def_max")
    private Long defValue;

    @JsonProperty("open_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date openAt;

    private String title;

    @JsonProperty("leader_skill")
    private String leaderSkill;

    @JsonProperty("passive_skill_itemized_desc")
    private String passiveSkillDesc;

    @JsonProperty("is_f2p")
    private Boolean freeCardFlag;

    @JsonProperty("is_dokkan_fes")
    private Boolean dokkanFesFlag;

    @JsonProperty("is_carnival_only")
    private Boolean carnivalFlag;

}
