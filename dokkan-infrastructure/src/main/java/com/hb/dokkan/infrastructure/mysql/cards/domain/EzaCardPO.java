package com.hb.dokkan.infrastructure.mysql.cards.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @Description 极限卡牌表
 * @Author stargazer
 * @Date 2025/9/14 18:50
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("eza_card")
public class EzaCardPO extends CardPO {

    private static final long serialVersionUID = 2469335044310282497L;

    /**
     * 卡片id
     */
    private Long cardId;

    /**
     * 卡片名称
     */
    private String cardName;

    /**
     * 满级
     */
    private Integer lvMax;

    /**
     * 极限阶段
     */
    private Integer step;

    /**
     * 卡片描述
     */
    private String title;

    /**
     * 0:超系 1:极系
     */
    private Integer type;

    /**
     * 属性
     */
    private String propType;

    /**
     * cost
     */
    private Integer cost;

    /**
     * 卡片稀有度
     */
    private Integer rarity;

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
     * 发布时间
     */
    private Date publishTime;

    /**
     * 队长技id
     */
    private Long leaderSkillId;

    /**
     * 队长技
     */
    private String leaderSkill;
    /**
     * 被动技id
     */
    private Integer passiveSkillId;

     /**
     * 被动技描述
     */
    private String passiveSkillDesc;

    /**
     * 扩展属性
     */
    private String attributes;
}
