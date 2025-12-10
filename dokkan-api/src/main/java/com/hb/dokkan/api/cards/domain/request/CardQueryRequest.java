package com.hb.dokkan.api.cards.domain.request;

import com.hb.dokkan.common.domain.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Description 卡片查询参数
 * @Author stargazer
 * @Date 2025/12/10 22:16
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class CardQueryRequest extends PageRequest implements Serializable {
    private static final long serialVersionUID = -8967595314460731405L;

    /**
     * 卡片名称
     */
    private String cardName;
    /**
     * 卡片id
     */
    private Long cardId;
    /**
     * 属性名称
     * @see com.hb.dokkan.service.enums.CardPropTypeEnum
     */
    private String propType;
    /**
     * 超系或极系
     * @see com.hb.dokkan.service.enums.CardPropTypeEnum
     */
    private String type;

    /**
     * 稀有度
     * @see com.hb.dokkan.service.enums.CardRarityEnum
     */
    private String rarity;
}
