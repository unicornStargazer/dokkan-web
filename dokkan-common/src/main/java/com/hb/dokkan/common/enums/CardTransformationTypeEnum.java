package com.hb.dokkan.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 卡牌变身类型枚举
 *
 * @author huangbiao
 * @date 2026/1/5 14:52
 **/
@AllArgsConstructor
@Getter
public enum CardTransformationTypeEnum {

    INIT(0,"变身前状态"),

    RESERVE(131,"双向交替"),

    TRANSFORMATION_TYPE_ENUM(103,"变身"),

    HUGE(79,"巨大化"),

    ;



    private final int type;

    private final String desc;
}
