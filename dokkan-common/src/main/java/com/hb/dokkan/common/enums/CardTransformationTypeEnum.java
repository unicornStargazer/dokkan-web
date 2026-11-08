package com.hb.dokkan.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import com.hb.dokkan.common.constants.CardSyncConstants;

/**
 * 卡牌变身类型枚举
 *
 * @author huangbiao
 * @date 2026/1/5 14:52
 **/
@AllArgsConstructor
@Getter
@Slf4j
public enum CardTransformationTypeEnum {

    INIT(0,"变身前状态"),

    RESERVE(131,"双向交替"),

    TRANSFORMATION_TYPE_ENUM(103,"变身"),

    HUGE(79,"巨大化"),

    ;



    /**
     * 兼容旧 ES 的形态变化类型编码。
     **/
    private final int type;

    /**
     * 类型的中文说明。
     **/
    private final String desc;

    /**
     * 将 KX Wiki form_kind 映射为兼容旧索引的变身类型编码。
     *
     * @param formKind KX Wiki 形态类型
     * @return 兼容旧系统的类型编码
     **/
    public static int fromKxFormKind(String formKind) {
        if (CardSyncConstants.FORM_KIND_EXCHANGE.equalsIgnoreCase(formKind)) {
            return RESERVE.type;
        }
        if (CardSyncConstants.FORM_KIND_GIANT_RAGE.equalsIgnoreCase(formKind)) {
            return HUGE.type;
        }
        if (!CardSyncConstants.FORM_KIND_TRANSFORMATION.equalsIgnoreCase(formKind)) {
            log.warn("Unknown KX Wiki form kind, defaulting to transformation, formKind={}", formKind);
        }
        return TRANSFORMATION_TYPE_ENUM.type;
    }
}
