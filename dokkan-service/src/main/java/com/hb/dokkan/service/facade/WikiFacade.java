package com.hb.dokkan.service.facade;

import com.google.common.collect.Lists;
import com.hb.dokkan.common.constants.ResponseErrorCode;
import com.hb.dokkan.common.exception.domain.DokkanBizException;
import com.hb.dokkan.common.http.CommonHttpClient;
import com.hb.dokkan.service.domain.sync.WikiCardDTO;
import com.hb.dokkan.service.enums.SyncCardUrlEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

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


    /**
     * 根据cardId获取card
     */
    public WikiCardDTO getWikiCard(String cardId) {
        String url = SyncCardUrlEnum.WIKI_CARDS.getUrl() + cardId;
        try {
            WikiCardDTO wikiCard = httpClient.getForEntity(url, WikiCardDTO.class);
            return wikiCard;
        } catch (Exception e) {
            log.error("WikiFacade#getWikiCard error,cardId:{}", cardId, e);
            return null;

        }
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
