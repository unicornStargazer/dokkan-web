package com.hb.dokkan.common.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Description 异常错误码
 * @Author stargazer
 * @Date 2025/11/9 14:31
 **/
@Getter
@AllArgsConstructor
public enum ExceptionErrorCode {

    HAS_NO_STRATEGY("980810", "找不到对应策略"),

    GET_WIKI_INFO_ERROR("980811", "获取wiki信息失败"),

    INSERT_PARAM_ERROR("980812","插入数据参数错误"),

    CREATE_INDEX_ERROR("980813","创建es索引失败"),

    QUERY_PARAM_ERROR("980814", "查询参数错误"),

    ;

    private final String errorCode;

    private final String errorMsg;
}
