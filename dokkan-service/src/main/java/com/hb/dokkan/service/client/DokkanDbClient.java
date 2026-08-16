package com.hb.dokkan.service.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hb.dokkan.common.constants.CardSyncConstants;
import com.hb.dokkan.common.domain.dto.data.dokkandb.DokkanDbCardDTO;
import com.hb.dokkan.common.domain.dto.data.dokkandb.DokkanDbCardStatsDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiCategoryDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiLinkDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Description DokkanDB接口客户端
 * @Author stargazer
 * @Date 2026/8/15 21:20
 **/
@Slf4j
@Component
public class DokkanDbClient {

    @Resource
    @Qualifier("dokkanDbWebClient")
    private WebClient jpClient;

    @Resource
    @Qualifier("dokkanDbGlobalWebClient")
    private WebClient globalClient;

    /**
     * 查询 Global 站最近卡片目录，异常时返回空列表。
     *
     * @param size 查询数量
     * @return 卡片目录列表
     */
    public List<DokkanDbCardDTO> listRecentCatalogFromGlobal(int size) {
        return listRecentCatalogFromGlobal(CardSyncConstants.DOKKAN_DB_DEFAULT_CHUNK, size);
    }

    /**
     * 查询 Global 站指定分片卡片目录，异常时返回空列表。
     *
     * @param chunk 目录分片序号
     * @param size  查询数量
     * @return 卡片目录列表
     */
    public List<DokkanDbCardDTO> listRecentCatalogFromGlobal(int chunk, int size) {
        return fetchCatalog(Source.GLOBAL, chunk, size);
    }

    /**
     * 查询 JP 站最近卡片目录，异常时返回空列表。
     *
     * @param size 查询数量
     * @return 卡片目录列表
     */
    public List<DokkanDbCardDTO> listRecentCatalogFromJp(int size) {
        return fetchCatalog(Source.JP, CardSyncConstants.DOKKAN_DB_DEFAULT_CHUNK, size);
    }

    /**
     * 查询 Global 站单卡详情，异常时返回空列表。
     *
     * @param cardId 卡片 ID
     * @return 卡片详情列表
     */
    public List<DokkanDbCardDTO> listCardsFromGlobal(Long cardId) {
        return fetchCard(Source.GLOBAL, cardId);
    }

    /**
     * 查询 JP 站单卡详情，异常时返回空列表。
     *
     * @param cardId 卡片 ID
     * @return 卡片详情列表
     */
    public List<DokkanDbCardDTO> listCardsFromJp(Long cardId) {
        return fetchCard(Source.JP, cardId);
    }

    /**
     * 查询指定数据源的卡片数值，异常时返回空列表。
     *
     * @param source 数据源
     * @param cardId 卡片 ID
     * @return 卡片数值列表
     */
    public List<DokkanDbCardStatsDTO> listCardStats(Source source, Long cardId) {
        return fetchList(source, CardSyncConstants.DOKKAN_DB_CARD_STATS_PATH, DokkanDbCardStatsDTO.class,
                uri -> uri.queryParam(CardSyncConstants.DOKKAN_DB_QUERY_CARD_ID, cardId));
    }

    /**
     * 查询 Global 站分类列表，异常时返回空列表。
     *
     * @return 分类列表
     */
    public List<WikiCategoryDTO> listCategoriesFromGlobal() {
        return fetchList(Source.GLOBAL, CardSyncConstants.DOKKAN_DB_CATEGORIES_PATH, WikiCategoryDTO.class);
    }

    /**
     * 查询 JP 站分类列表，异常时返回空列表。
     *
     * @return 分类列表
     */
    public List<WikiCategoryDTO> listCategoriesFromJp() {
        return fetchList(Source.JP, CardSyncConstants.DOKKAN_DB_CATEGORIES_PATH, WikiCategoryDTO.class);
    }

    /**
     * 查询 Global 站链接列表，异常时返回空列表。
     *
     * @return 链接列表
     */
    public List<WikiLinkDTO> listLinksFromGlobal() {
        return fetchList(Source.GLOBAL, CardSyncConstants.DOKKAN_DB_LINKS_PATH, WikiLinkDTO.class);
    }

    /**
     * 查询 JP 站链接列表，异常时返回空列表。
     *
     * @return 链接列表
     */
    public List<WikiLinkDTO> listLinksFromJp() {
        return fetchList(Source.JP, CardSyncConstants.DOKKAN_DB_LINKS_PATH, WikiLinkDTO.class);
    }

    /**
     * 查询链接效果详情，异常时返回空列表。
     *
     * @param linkIds 链接 ID 列表
     * @return 链接效果列表
     */
    public List<LinkEffect> listLinkEffects(List<Long> linkIds) {
        String ids = linkIds.stream()
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .collect(Collectors.joining(CardSyncConstants.ID_JOIN_SEPARATOR));
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        return fetchList(Source.GLOBAL, CardSyncConstants.DOKKAN_DB_LINK_EFFECTS_PATH, LinkEffect.class,
                uri -> uri.queryParam(CardSyncConstants.DOKKAN_DB_QUERY_IDS, ids));
    }

    /**
     * 查询最近卡片目录。
     *
     * @param source 数据源
     * @param chunk  目录分片序号
     * @param size   查询数量
     * @return 卡片目录列表
     */
    private List<DokkanDbCardDTO> fetchCatalog(Source source, int chunk, int size) {
        return fetchList(source, CardSyncConstants.DOKKAN_DB_CATALOG_PATH, DokkanDbCardDTO.class,
                uri -> uri.queryParam(CardSyncConstants.DOKKAN_DB_QUERY_CHUNK, chunk)
                        .queryParam(CardSyncConstants.DOKKAN_DB_QUERY_CHUNK_SIZE, size));
    }

    /**
     * 查询单卡详情。
     *
     * @param source 数据源
     * @param cardId 卡片 ID
     * @return 卡片详情列表
     */
    private List<DokkanDbCardDTO> fetchCard(Source source, Long cardId) {
        return fetchList(source, CardSyncConstants.DOKKAN_DB_CARD_PATH, DokkanDbCardDTO.class,
                uri -> uri.queryParam(CardSyncConstants.DOKKAN_DB_QUERY_CODE, cardId));
    }

    /**
     * 执行无 query 参数的 DokkanDB 列表请求。
     *
     * @param source 数据源
     * @param path   接口路径
     * @param type   响应元素类型
     * @param <T>    响应元素泛型
     * @return 响应列表
     */
    private <T> List<T> fetchList(Source source, String path, Class<T> type) {
        return fetchList(source, path, type, uri -> uri);
    }

    /**
     * 执行 DokkanDB 列表请求，统一处理异常日志和空列表降级。
     *
     * @param source 数据源
     * @param path   接口路径
     * @param type   响应元素类型
     * @param custom query 参数定制器
     * @param <T>    响应元素泛型
     * @return 响应列表
     */
    private <T> List<T> fetchList(Source source, String path, Class<T> type, UriCustomizer custom) {
        try {
            List<T> rows = client(source).get()
                    .uri(uri -> custom.customize(uri.path(path)).build())
                    .retrieve()
                    .bodyToFlux(type)
                    .collectList()
                    .block(Duration.ofSeconds(CardSyncConstants.DOKKAN_DB_REQUEST_TIMEOUT_SECONDS));
            return Objects.isNull(rows) ? Collections.emptyList() : rows;
        } catch (Exception e) {
            log.warn("DokkanDB request failed, source={}, path={}", source, path, e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取指定数据源的 WebClient。
     *
     * @param source 数据源
     * @return WebClient
     */
    private WebClient client(Source source) {
        return Source.GLOBAL.equals(source) ? globalClient : jpClient;
    }

    /** DokkanDB 数据源。 */
    public enum Source {
        /** Global 英文数据源。 */
        GLOBAL,
        /** JP 日文数据源。 */
        JP
    }

    /**
     * 链接效果条目。
     *
     * @param id            链接 ID
     * @param description   1级效果描述
     * @param description10 10级效果描述
     */
    public record LinkEffect(Long id, String description,
                             @JsonProperty("description_10") String description10) {
    }

    /**
     * URI 定制函数。
     */
    @FunctionalInterface
    private interface UriCustomizer {

        /**
         * 定制 URI 构造器。
         *
         * @param uriBuilder URI 构造器
         * @return 定制后的 URI 构造器
         */
        org.springframework.web.util.UriBuilder customize(org.springframework.web.util.UriBuilder uriBuilder);
    }
}
