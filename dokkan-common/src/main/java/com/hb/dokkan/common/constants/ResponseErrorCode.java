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

    FAILED_DEFAULT("500", "系统错误"),

    HAS_NO_STRATEGY("980810", "找不到对应策略"),

    GET_WIKI_INFO_ERROR("980811", "获取wiki信息失败"),

    INSERT_PARAM_ERROR("980812","插入数据参数错误"),
    ;

    private final String errorCode;

    private final String errorMsg;
}
