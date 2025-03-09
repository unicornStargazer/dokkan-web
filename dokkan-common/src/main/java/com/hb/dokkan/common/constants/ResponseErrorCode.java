package com.hb.dokkan.common.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Description xxxxx
 * @Author stargazer
 * @Date 2025/3/9 18:42
 **/
@Getter
@AllArgsConstructor
public enum ResponseErrorCode {

    SUCCESS("000000","调用成功"),

    FAILED_DEFAULT("500", "系统错误");

    private final String errorCode;

    private final String errorMsg;
}
