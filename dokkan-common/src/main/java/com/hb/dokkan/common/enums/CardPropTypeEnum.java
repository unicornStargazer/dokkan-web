package com.hb.dokkan.common.enums;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    AGL(0,"速"),

    TEQ(1,"技"),

    INT(2,"知"),

    STR(3,"力"),

    PHY(4,"体"),


    SUPER_AGL(10,"超速"),

    SUPER_TEQ(11,"超技"),

    SUPER_INT(12,"超知"),

    SUPER_STR(13,"超力"),

    SUPER_PHY(14,"超体"),


    EXTREME_AGL(20,"极速"),

    EXTREME_TEQ(21,"极技"),

    EXTREME_INT(22,"极知"),

    EXTREME_STR(23,"极力"),

    EXTREME_PHY(24,"极体"),

    ;

    private final Integer type;

    private final String description;

    static final List<CardPropTypeEnum> superProp;

    static final List<CardPropTypeEnum> extremeProp;

    static final Map<Integer, CardPropTypeEnum> POOL;

    static {
        superProp = Lists.newArrayList(SUPER_AGL,SUPER_TEQ,SUPER_PHY,SUPER_INT,SUPER_STR);
        extremeProp = Lists.newArrayList(EXTREME_AGL,EXTREME_TEQ,EXTREME_INT,EXTREME_STR,EXTREME_PHY);
        POOL = Arrays.stream(values()).collect(Collectors.toMap(CardPropTypeEnum::getType, Function.identity()));
    }

    public static CardPropTypeEnum getCardPropEnumByType(int type) {
        return POOL.get(type);
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
