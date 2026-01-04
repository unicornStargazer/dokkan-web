package com.hb.dokkan.common.domain.dto.data.wiki;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description 卡牌基础信息
 * @Author stargazer
 * @Date 2025/6/2 16:06
 **/
@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
public class WikiCardBaseInfoDTO implements Serializable {
    private static final long serialVersionUID = -6474306338635635775L;

    /**
     * 卡牌id
     */
    private Long id;

    /**
     * 卡牌名称
     */
    private String name;

    /**
     * cost
     */
    private Integer cost;

    /**
     * @see com.hb.dokkan.common.enums.CardRarityEnum
     */
    private Integer rarity;

    /**
     * 属性
     */
    @JsonProperty("element")
    private Integer propType;

    /**
     * 生命值
     */
    @JsonProperty("hp_max")
    private Long hpValue;

    /**
     * 攻击值
     */
    @JsonProperty("atk_max")
    private Long atkValue;
    /**
     * 防御值
     */
    @JsonProperty("def_max")
    private Long defValue;
    /**
     * 发布实际
     */
    @JsonProperty("open_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date openAt;
    /**
     * 描述
     */
    private String title;
    /**
     * 队长技id
     */
    @JsonProperty("leader_skill_set_id")
    private Long leaderSkillSetId;
    /**
     * 队长技
     */
    @JsonProperty("leader_skill")
    private String leaderSkill;

    /**
     * 被动id
     */
    @JsonProperty("passive_skill_set_id")
    private Long passiveSkillSetId;

    /**
     * 被动名称
     */
    @JsonProperty("passive_skill_name")
    private String passiveSkillName;
    /**
     * 被动
     */
    @JsonProperty("passive_skill_itemized_desc")
    private String passiveSkillDesc;

    /**
     * 主动技id
     */
    @JsonProperty("active_skill_id")
    private Integer activeSkillId;
    /**
     * 主动技名称
     */
    @JsonProperty("active_skill_name")
    private String activeSkillName;
    /**
     * 主动技效果
     */
    @JsonProperty("active_skill_effect")
    private String activeSkillEffect;
    /**
     * 主动技条件
     */
    @JsonProperty("active_skill_condition")
    private String activeSkillCondition;

    /**
     * skillLevelMax
     */
    @JsonProperty("skill_level_max")
    private Integer skillLevelMax;
    /**
     * 免费标志
     */
    @JsonProperty("is_f2p")
    private Boolean freeCardFlag;
    /**
     * 限定标志
     */
    @JsonProperty("is_dokkan_fes")
    private Boolean dokkanFesFlag;
    /**
     * 祭限定
     */
    @JsonProperty("is_carnival_only")
    private Boolean carnivalFlag;

}
