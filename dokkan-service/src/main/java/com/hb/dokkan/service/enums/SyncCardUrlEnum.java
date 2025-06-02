package com.hb.dokkan.service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Description xxxxx
 * @Author stargazer
 * @Date 2025/6/2 0:19
 **/
@AllArgsConstructor
@Getter
public enum SyncCardUrlEnum {

    WIKI_LINK("https://zh.dokkan.wiki/api/links","链接url"),

    WIKI_CATEGORY("https://zh.dokkan.wiki/api/categories","分类url"),

    WIKI_CARDS("https://zh.dokkan.wiki/api/cards/","卡片url"),

    ;



    private final String url;

    private final String desc;
}
