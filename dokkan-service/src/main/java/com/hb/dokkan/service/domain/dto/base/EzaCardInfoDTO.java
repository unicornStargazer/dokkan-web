package com.hb.dokkan.service.domain.dto.base;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description 极限卡牌信息
 * @Author stargazer
 * @Date 2025/9/15 21:53
 **/
@Data
public class EzaCardInfoDTO implements Serializable {
    private static final long serialVersionUID = 7576665782553499878L;

    /**
     * 卡牌id
     */
    private Long cardId;

    /**
     * 极限阶段
     */
    private Integer step;
    /**
     * 最大等级
     */
    private Integer lvMax;

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
    private Integer propType;

    /**
     * 生命值
     */
    private Long hpValue;

    /**
     * 攻击值
     */
    private Long atkValue;
    /**
     * 防御值
     */
    private Long defValue;
    /**
     * 发布实际
     */
    private Date openAt;
    /**
     * 描述
     */
    private String title;
    /**
     * 队长技id
     */
    private Integer leaderSkillId;
    /**
     * 队长技name
     */
    private String leaderSkillName;
    /**
     * 队长技
     */
    private String leaderSkill;
    /**
     * 被动id
     */
    private Integer passiveSkillId;

    /**
     * 被动名称
     */
    private String passiveSkillName;
    /**
     * 被动
     */
    private String passiveSkillDesc;
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
}
