package com.hb.dokkan.common.domain.po.es.cards;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description es三围数据类
 * @Author stargazer
 * @Date 2025/11/9 15:22
 **/
@Data
public class CardEsPotentialDTO implements Serializable {
    private static final long serialVersionUID = -8331594691142932157L;

    /**
     * 排序
     */
    private Integer order;

    /**
     * 生命值
     */
    private Number hp;
    /**
     * 攻击值
     */
    private Number atk;
    /**
     * 防御值
     */
    private Number def;

}
