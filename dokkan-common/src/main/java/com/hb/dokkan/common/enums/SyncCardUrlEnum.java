package com.hb.dokkan.common.enums;

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

    WIKI_INFO("/cards",""),

    WIKI_LINK("/api/links","链接url"),

    WIKI_CATEGORY("/api/categories","分类url"),

    WIKI_CARDS("/api/cards/","卡片url"),

    ;



    private final String url;

    private final String desc;
}
