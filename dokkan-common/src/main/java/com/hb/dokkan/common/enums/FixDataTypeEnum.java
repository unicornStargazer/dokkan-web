package com.hb.dokkan.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 修复数据策略类型
 *
 * @author huangbiao
 * @date 2026/1/5 13:48
 **/
@AllArgsConstructor
@Getter
public enum FixDataTypeEnum {

    /**
     * 修复数据库eza发布时间
     */
    FIX_DB_EZA_PUBLISH_TIME(0, "修复数据库eza发布时间"),
    ;

    private final Integer code;

    private final String desc;

    /**
     * 根据code获取修复数据策略类型
     */
    public static FixDataTypeEnum valuesOfCode(Integer code) {
        for (FixDataTypeEnum fixDataTypeEnum : FixDataTypeEnum.values()) {
            if (fixDataTypeEnum.getCode().equals(code)) {
                return fixDataTypeEnum;
            }
        }
        return null;
    }
}
