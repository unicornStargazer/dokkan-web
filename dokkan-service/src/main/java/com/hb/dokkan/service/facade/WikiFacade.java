package com.hb.dokkan.service.facade;

import com.google.common.collect.Lists;
import com.hb.dokkan.common.constants.ResponseErrorCode;
import com.hb.dokkan.common.exception.domain.DokkanBizException;
import com.hb.dokkan.common.http.CommonHttpClient;
import com.hb.dokkan.common.http.RetryTemplate;
import com.hb.dokkan.config.http.HttpPoolProperties;
import com.hb.dokkan.service.domain.sync.WikiCardDTO;
import com.hb.dokkan.service.enums.SyncCardUrlEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @Description xxxxx
 * @Author stargazer
 * @Date 2025/6/3 21:41
 **/
@Service
@Slf4j
public class WikiFacade {
    @Resource
    private CommonHttpClient httpClient;

    @Resource
    private HttpPoolProperties httpPoolProperties;

    @Resource(name = "dokkanInfoWebClient")
    private WebClient infoClient;


    /**
     * 根据cardId获取card
     */
    public WikiCardDTO getWikiCard(String cardId) {
        String url = SyncCardUrlEnum.WIKI_CARDS.getUrl() + cardId;

        return RetryTemplate.executeWithRetrySliently(() -> {
            Optional<WikiCardDTO> optional = httpClient.getForObject(url, WikiCardDTO.class, null);
            return optional.orElse(null);
        }, httpPoolProperties.getRetry(), "getWikiCard-cardId:" + cardId);
    }

    /**
     * 获取最新html
     */
    public String getWikiHtml(){
        String url = SyncCardUrlEnum.WIKI_INFO.getUrl();
        String html = infoClient.get()
                .uri(url) // 目标路径
                .retrieve()   // 获取响应
                .bodyToMono(String.class)
                .block();
        return html;
    }

    /**
     * 根据cardId获取cardList
     */
    public List<WikiCardDTO> getWikiCardList(List<String> cardIds) {
        List<WikiCardDTO> wikiCards = Lists.newArrayList();
        cardIds.parallelStream().forEach(cardId -> {
            WikiCardDTO card = getWikiCard(cardId);
            if (Objects.isNull(card)) {
                throw new DokkanBizException(ResponseErrorCode.GET_WIKI_INFO_ERROR);
            }
            wikiCards.add(card);
        });
        return wikiCards;
    }


}
