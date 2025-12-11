package com.hb.dokkan.service.domain.cards.dto;

import com.google.common.base.Objects;
import com.hb.dokkan.common.utils.JsonUtils;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description 卡片基础信息
 * @Author stargazer
 * @Date 2025/9/10 23:29
 **/
@Data
public class CardBaseInfoDTO implements Serializable {
    private static final long serialVersionUID = 7679712350800335814L;

    /**
     * 卡牌id
     */
    private Long cardId;
    /**
     * 卡牌name
     */
    private String cardName;
    /**
     * 卡牌描述
     */
    private String title;
    /**
     * 属性(超 极)
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
     * 发布事件
     */
    private Date publishTime;
    /**
     * 扩展属性
     */
    private String attributes;
    /**
     * 扩展属性
     */
    private CardBaseInfoAttribute cardBaseInfoAttribute;

    public CardBaseInfoAttribute getCardBaseInfoAttribute() {
        return JsonUtils.json2Object(this.attributes, CardBaseInfoAttribute.class);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof CardBaseInfoDTO that)) return false;
        return Objects.equal(cardId, that.cardId) && Objects.equal(cardName, that.cardName) && Objects.equal(title, that.title) && Objects.equal(type, that.type) && Objects.equal(propType, that.propType) && Objects.equal(cost, that.cost) && Objects.equal(rarity, that.rarity) && Objects.equal(hpValue, that.hpValue) && Objects.equal(defValue, that.defValue) && Objects.equal(atkValue, that.atkValue) && Objects.equal(publishTime, that.publishTime) && Objects.equal(attributes, that.attributes) && Objects.equal(cardBaseInfoAttribute, that.cardBaseInfoAttribute);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(cardId, cardName, title, type, propType, cost, rarity, hpValue, defValue, atkValue, publishTime, attributes, cardBaseInfoAttribute);
    }
}
