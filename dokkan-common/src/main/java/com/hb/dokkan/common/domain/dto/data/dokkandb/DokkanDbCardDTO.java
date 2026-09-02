package com.hb.dokkan.common.domain.dto.data.dokkandb;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/** Card detail/catalog row returned by the DokkanDB JP API. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DokkanDbCardDTO {
    private Long id;
    private Integer rarity;
    private Integer element;
    private String name;
    private String tag;

    @JsonProperty("subname")
    private String title;
    @JsonProperty("open_at")
    private String openAt;
    @JsonProperty("open_at_update")
    private String openAtUpdate;
    @JsonProperty("leader_skill_set_id")
    private Long leaderSkillSetId;
    @JsonProperty("leader_skill")
    private String leaderSkill;
    @JsonProperty("passive_skill_set_id")
    private Long passiveSkillSetId;
    @JsonProperty("passive_skill_name")
    private String passiveSkillName;
    @JsonProperty("passive_skill_description")
    private String passiveSkillDescription;
    @JsonProperty("active_skill_set_id")
    private Integer activeSkillSetId;
    @JsonProperty("active_skill_name")
    private String activeSkillName;
    @JsonProperty("active_skill_effect")
    private String activeSkillEffect;
    @JsonProperty("active_skill_condition")
    private String activeSkillCondition;
    @JsonProperty("skill_lv_max")
    private Integer skillLvMax;

    @JsonProperty("category_ids")
    private List<Long> categoryIds;
    @JsonProperty("link_ids")
    private List<Long> linkIds;
    @JsonProperty("transformation_ids")
    private List<Long> transformationIds;
    @JsonProperty("transformation_names")
    private List<String> transformationNames;
    @JsonProperty("awakening_data")
    private List<AwakeningDTO> awakeningData;

    @JsonProperty("special_ids")
    private List<Long> specialIds;
    @JsonProperty("special_names")
    private List<String> specialNames;
    @JsonProperty("special_descriptions")
    private List<String> specialDescriptions;
    @JsonProperty("special_increase_rates")
    private List<Integer> specialIncreaseRates;
    @JsonProperty("special_lv_bonuses")
    private List<Integer> specialLvBonuses;
    @JsonProperty("special_styles")
    private List<String> specialStyles;
    @JsonProperty("special_kis")
    private List<Integer> specialKis;

    @JsonProperty("standby_skill_set_id")
    private Integer standbySkillSetId;
    @JsonProperty("standby_skill_name")
    private String standbySkillName;
    @JsonProperty("standby_skill_effect")
    private String standbySkillEffect;
    @JsonProperty("standby_skill_condition")
    private String standbySkillCondition;
    @JsonProperty("finish_skill_set_ids")
    private List<Integer> finishSkillSetIds;
    @JsonProperty("finish_skill_names")
    private List<String> finishSkillNames;
    @JsonProperty("finish_skill_effects")
    private List<String> finishSkillEffects;
    @JsonProperty("finish_skill_conditions")
    private List<String> finishSkillConditions;
    @JsonProperty("finish_skill_increase_rates")
    private List<Integer> finishSkillIncreaseRates;

    @JsonAlias("optimal_awakening_step")
    private Integer step;
    @JsonProperty("step_pre")
    @JsonAlias("optimal_awakening_step_pre")
    private Integer stepPre;
    @JsonProperty("eza_skill_lv_max")
    private Integer ezaSkillLvMax;
    @JsonProperty("eza_leader_skill_set_id")
    private Integer ezaLeaderSkillSetId;
    @JsonProperty("eza_leader_skill")
    private String ezaLeaderSkill;
    @JsonProperty("eza_passive_skill_set_id")
    private Integer ezaPassiveSkillSetId;
    @JsonProperty("eza_passive_skill_name")
    private String ezaPassiveSkillName;
    @JsonProperty("eza_passive_skill_description")
    private String ezaPassiveSkillDescription;
    @JsonProperty("eza_leader_skill_set_id_pre")
    private Integer ezaLeaderSkillSetIdPre;
    @JsonProperty("eza_leader_skill_pre")
    private String ezaLeaderSkillPre;
    @JsonProperty("eza_passive_skill_set_id_pre")
    private Integer ezaPassiveSkillSetIdPre;
    @JsonProperty("eza_passive_skill_name_pre")
    private String ezaPassiveSkillNamePre;
    @JsonProperty("eza_passive_skill_description_pre")
    private String ezaPassiveSkillDescriptionPre;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AwakeningDTO {
        private Long id;
        @JsonProperty("open_at_eza")
        private String openAtEza;
    }
}
