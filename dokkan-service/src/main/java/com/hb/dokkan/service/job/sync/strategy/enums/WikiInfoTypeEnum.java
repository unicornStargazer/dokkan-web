package com.hb.dokkan.service.job.sync.strategy.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Description xxxxx
 * @Author stargazer
 * @Date 2025/6/2 21:01
 **/
@AllArgsConstructor
@Getter
public enum WikiInfoTypeEnum {

    CARD("卡片信息"),

    CATEGORY("分类信息"),

    LINK("链接"),

    ;
    private final String desc;
}
