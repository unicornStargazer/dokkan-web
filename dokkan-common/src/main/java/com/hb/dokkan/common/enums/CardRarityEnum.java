package com.hb.dokkan.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Description 稀有度枚举
 * @Author stargazer
 * @Date 2025/6/2 17:16
 **/
@AllArgsConstructor
@Getter
public enum CardRarityEnum{

    N(0),

    R(1),

    SR(2),

    SSR(3),

    UR(4),

    LR(5);

    private final int rarity;

}
