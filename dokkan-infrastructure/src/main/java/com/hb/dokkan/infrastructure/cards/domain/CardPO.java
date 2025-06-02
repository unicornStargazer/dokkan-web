package com.hb.dokkan.infrastructure.cards.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hb.dokkan.common.domain.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @Description xxxxx
 * @Author stargazer
 * @Date 2025/3/9 19:37
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName()
public class CardPO extends BasePO {
    /**
     * 卡片id
     */
    private Long cardId;

    private String cardName;

    private String title;

    private Integer type;

    private String propType;

    private Integer cost;

    private Integer rarity;

    private Number hpValue;

    private Number defValue;

    private Number atkValue;

    private Date publishTime;

    private String attributes;


}
