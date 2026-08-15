package com.hb.dokkan.service.client;

import com.hb.dokkan.common.constants.CardSyncConstants;
import com.hb.dokkan.common.enums.SyncCardUrlEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Objects;

/**
 * @Description Wiki接口客户端
 * @Author stargazer
 * @Date 2026/8/15 21:20
 **/
@Slf4j
@Component
public class WikiClient {

    @Resource(name = "dokkanInfoWebClient")
    private WebClient infoClient;

    /**
     * 获取 Wiki 首页 HTML，异常时返回空字符串由上层按空内容处理。
     *
     * @return Wiki 首页 HTML
     */
    public String getWikiHtml() {
        String url = SyncCardUrlEnum.WIKI_INFO.getUrl();
        try {
            String html = infoClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(CardSyncConstants.DOKKAN_DB_REQUEST_TIMEOUT_SECONDS));
            return Objects.isNull(html) ? CardSyncConstants.EMPTY_TEXT : html;
        } catch (Exception e) {
            log.warn("Wiki html request failed, url={}", url, e);
            return CardSyncConstants.EMPTY_TEXT;
        }
    }
}
