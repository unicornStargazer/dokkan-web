package com.hb.dokkan.agent.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProviderEnum {

    /**
     * 阿里百炼
     */
    BAI_LIAN("bailian","阿里百炼云平台"),

    /**
     * OPEN_ROUTE中转
     */
    OPEN_ROUTE("openRoute","OPEN_ROUTE中转"),;

    /**
     * 适配器名称
     */
    private final String provideName;

    /**
     * 适配描述
     */
    private final String desc;





}
