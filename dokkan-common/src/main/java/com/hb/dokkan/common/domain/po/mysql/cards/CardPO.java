package com.hb.dokkan.common.domain.po.mysql.cards;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hb.dokkan.common.domain.po.mysql.base.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @Description 卡牌模型表
 * @Author stargazer
 * @Date 2025/3/9 19:37
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("card")
public class CardPO extends BasePO {
    /**
     * 卡片id
     */
    private Long cardId;
    /**
     * 卡片name
     */
    private String cardName;
    /**
     * 卡片描述
     */
    private String title;
    /**
     * 属性（超 极）
     */
    private Integer type;
    /**
     *  具体属性
     */
    private String propType;
    /**
     * cost
     */
    private Integer cost;
    /**
     * 稀有度
     */
    private Integer rarity;
    /**
     * 生命值
     */
    private Long hpValue;
    /**
     * 防御值
     */
    private Long defValue;
    /**
     * 攻击值
     */
    private Long atkValue;
    /**
     * 发布时间
     */
    private Date publishTime;
    /**
     * 扩展属性
     */
    private String attributes;


}
