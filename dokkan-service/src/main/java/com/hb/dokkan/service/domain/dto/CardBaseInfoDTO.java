package com.hb.dokkan.service.domain.dto;

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

    private Long cardId;

    private String cardName;

    private String title;

    private Integer type;

    private String propType;

    private Integer cost;

    private Integer rarity;

    private Long hpValue;

    private Long defValue;

    private Long atkValue;

    private Date publishTime;

    private String attributes;

    private CardBaseInfoAttribute cardBaseInfoAttribute;
}
