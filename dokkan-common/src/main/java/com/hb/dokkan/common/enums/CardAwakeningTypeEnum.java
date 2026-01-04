package com.hb.dokkan.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Description 觉醒阶段枚举
 * @Author stargazer
 * @Date 2026/1/4 23:34
 **/
@Getter
@AllArgsConstructor
public enum CardAwakeningTypeEnum {

    Z_AWAKING("CardAwakeningRoute::Zet"),

    DOKKAN_AWAKENING("CardAwakeningRoute::Dokkan");

    private final String type;

}
