package com.hb.dokkan.service.domain.wiki;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
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
     * @see com.hb.dokkan.service.enums.CardRarityEnum
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
    private Long leaderSkillSetId;
    /**
     * 队长技
     */
    private String leaderSkill;

    /**
     * 被动id
     */
    private Long passiveSkillSetId;

    /**
     * 被动名称
     */
    private String passiveSkillName;
    /**
     * 被动
     */
    @JsonProperty("passive_skill_itemized_desc")
    private String passiveSkillDesc;

    /**
     * 主动技id
     */
    private Integer activeSkillId;
    /**
     * 主动技名称
     */
    private String activeSkillName;
    /**
     * 主动技效果
     */
    private String activeSkillEffect;
    /**
     * 主动技条件
     */
    private String activeSkillCondition;

    /**
     * skillLevelMax
     */
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
