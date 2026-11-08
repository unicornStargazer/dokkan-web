package com.hb.dokkan.config.data;

import com.google.common.collect.Lists;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description wiki卡牌黑名单配置
 * @Author stargazer
 * @Date 2026/1/5 21:18
 **/
@Data
@Component
@ConfigurationProperties(prefix = "card.filter")
public class WikiCardFilter {

    /**
     * wiki卡片黑名单
     **/
    private String blackCardId;


    /**
     * wiki卡片白名单名单
     **/
    private String whiteCardId;

    /**
     * 读取黑名单；未配置时返回空集合。
     **/
    public List<Long> getWikiCardBlackList() {
        if (blackCardId == null || blackCardId.isBlank()) {
            return List.of();
        }
        return Lists.newArrayList(blackCardId.split(",")).stream().map(Long::valueOf).toList();
    }

    /**
     * 读取手动白名单，未配置时返回空集合。
     **/
    public List<Long> getWikiCardWhiteList() {
        if (whiteCardId == null || whiteCardId.isBlank()) {
            return List.of();
        }
        return Lists.newArrayList(whiteCardId.split(",")).stream().map(Long::valueOf).toList();
    }
}
