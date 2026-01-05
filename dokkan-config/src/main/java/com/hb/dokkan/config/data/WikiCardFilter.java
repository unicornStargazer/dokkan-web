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
     */
    private String blackCardId;


    /**
     * wiki卡片白名单名单
     */
    private String whiteCardId;

    public List<Long> getWikiCardBlackList() {
        return Lists.newArrayList(blackCardId.split(",")).stream().map(Long::valueOf).toList();
    }

    public List<Long> getWikiCardWhiteList() {
        return Lists.newArrayList(whiteCardId.split(",")).stream().map(Long::valueOf).toList();
    }
}
