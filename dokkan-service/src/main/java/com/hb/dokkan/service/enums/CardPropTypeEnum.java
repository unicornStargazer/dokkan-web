package com.hb.dokkan.service.enums;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * @Description 卡片属性枚举
 * @Author stargazer
 * @Date 2025/6/11 21:28
 **/
@Getter
@AllArgsConstructor
public enum CardPropTypeEnum {

    SUPER(100,"超系"),

    EXTREME(200,"极系"),

    UNKNOWN(-1,"未知"),

    AGL(0,"速属性"),

    TEQ(1,"技属性"),

    INT(2,"知属性"),

    STR(3,"力属性"),

    PHY(4,"体属性"),


    SUPER_AGL(10,"超速属性"),

    SUPER_TEQ(11,"超技属性"),

    SUPER_INT(12,"超知属性"),

    SUPER_STR(13,"超力属性"),

    SUPER_PHY(14,"超体属性"),


    EXTREME_AGL(20,"极速属性"),

    EXTREME_TEQ(21,"极技属性"),

    EXTREME_INT(22,"极知属性"),

    EXTREME_STR(23,"极力属性"),

    EXTREME_PHY(24,"极体属性"),

    ;

    private final Integer type;

    private final String description;

    private static List<CardPropTypeEnum> superProp = Lists.newArrayList(
            SUPER_AGL,SUPER_TEQ,SUPER_PHY,SUPER_INT,SUPER_STR
    );


    private static List<CardPropTypeEnum> extremeProp = Lists.newArrayList(
            EXTREME_AGL,EXTREME_TEQ,EXTREME_INT,EXTREME_STR,EXTREME_PHY
    );

    public static CardPropTypeEnum getCardPropEnumByType(int type) {
        for (CardPropTypeEnum typeEnum : CardPropTypeEnum.values()) {
            if (typeEnum.getType() == type) {
                return typeEnum;
            }
        }
        return null;
    }

    public static Integer getProp(int type) {
        CardPropTypeEnum typeEnum = getCardPropEnumByType(type);
        if (typeEnum == null) {
            return null;
        }
        if (superProp.contains(typeEnum)) {
            return SUPER.getType();
        }
        else if (extremeProp.contains(typeEnum)) {
            return EXTREME.getType();
        }
        else {
            return UNKNOWN.getType();
        }
    }
}
