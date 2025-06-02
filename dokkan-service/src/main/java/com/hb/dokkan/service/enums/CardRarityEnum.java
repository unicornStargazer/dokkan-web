package com.hb.dokkan.service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * @Description xxxxx
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

    public static Boolean isLrCard(Number rarity) {
        if (Objects.isNull(rarity)) {
            return false;
        }
        return CardRarityEnum.LR.rarity == rarity.intValue();
    }

}
