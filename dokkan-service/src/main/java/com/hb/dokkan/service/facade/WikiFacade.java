package com.hb.dokkan.service.facade;

import com.hb.dokkan.common.constants.ExceptionErrorCode;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiCardDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiCategoryDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiLinkDTO;
import com.hb.dokkan.common.enums.SyncCardUrlEnum;
import com.hb.dokkan.common.exception.domain.DokkanBizException;
import com.hb.dokkan.common.http.CommonHttpClient;
import com.hb.dokkan.config.http.HttpPoolProperties;
import com.hb.dokkan.config.http.RetryTemplate;
import com.hb.dokkan.service.client.WikiClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @Description Wiki数据门面
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

    @Resource
    private WikiClient wikiClient;

    /**
     * 根据 cardId 获取 Wiki 卡片详情。
     *
     * @param cardId 卡片 ID
     * @return Wiki 卡片详情，查不到时返回 null
     */
    public WikiCardDTO getWikiCard(String cardId) {
        String url = SyncCardUrlEnum.WIKI_CARDS.getUrl() + cardId;
        return RetryTemplate.executeWithRetrySliently(() -> {
            Optional<WikiCardDTO> optional = httpClient.getForObjectSync(url, WikiCardDTO.class, null);
            return optional.orElse(null);
        }, httpPoolProperties.getRetry(), "getWikiCard-cardId:" + cardId);
    }

    /**
     * 获取全量 Wiki 分类信息。
     *
     * @return Wiki 分类列表
     */
    public List<WikiCategoryDTO> getWikiCategory() {
        String url = SyncCardUrlEnum.WIKI_CATEGORY.getUrl();
        return RetryTemplate.executeWithRetrySliently(() ->
                        httpClient.getForList(url, WikiCategoryDTO.class, null),
                httpPoolProperties.getRetry(),
                "getWikiCategory");
    }

    /**
     * 获取全量 Wiki 链接信息。
     *
     * @return Wiki 链接列表
     */
    public List<WikiLinkDTO> getWikiLink() {
        String url = SyncCardUrlEnum.WIKI_LINK.getUrl();
        return RetryTemplate.executeWithRetrySliently(() ->
                        httpClient.getForList(url, WikiLinkDTO.class, null),
                httpPoolProperties.getRetry(),
                "getWikiLink");
    }

    /**
     * 获取 Wiki 最新 HTML。
     *
     * @return Wiki HTML 文本
     */
    public String getWikiHtml() {
        return wikiClient.getWikiHtml();
    }

    /**
     * 根据 cardId 列表批量获取 Wiki 卡片详情。
     *
     * @param cardIds 卡片 ID 列表
     * @return Wiki 卡片详情列表
     */
    public List<WikiCardDTO> getWikiCardList(List<String> cardIds) {
        return cardIds.parallelStream().map(cardId -> {
            WikiCardDTO card = getWikiCard(cardId);
            if (Objects.isNull(card)) {
                throw new DokkanBizException(ExceptionErrorCode.GET_WIKI_INFO_ERROR);
            }
            return card;
        }).toList();
    }
}
