package com.hb.dokkan.common.domain.dto.cards;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hb.dokkan.common.domain.dto.data.cards.CardTransformationDTO;
import com.hb.dokkan.common.domain.dto.data.cards.PotentialDTO;
import lombok.Data;

import java.util.List;

/**
 * 卡片扩展属性
 *
 * @author huangbiao
 * @date 2026/1/5 15:46
 **/
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardAttributeDTO {
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
    private Boolean freeCardFlag;
    /**
     * 限定标志
     */
    private Boolean dokkanFesFlag;
    /**
     * 祭限定
     */
    private Boolean carnivalFlag;

    /**
     * 分类id
     */
    private List<Long> categoryId;

    /**
     * 链接id
     */
    private List<Long> linkId;

    /**
     * 待机技能id
     */
    private List<Integer> standBySkillIds;

    /**
     * 下拉完成技能id
     */
    private List<Integer> finishSkillIds;

    /**
     * 三围潜力值
     */
    private List<PotentialDTO> potential;

    /**
     * 变身后信息
     */
    private List<CardTransformationDTO> nextCards;

    /**
     * 必杀id
     */
    private List<Long> specialSkillIds;
}
