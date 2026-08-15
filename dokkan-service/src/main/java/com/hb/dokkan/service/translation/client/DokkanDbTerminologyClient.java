package com.hb.dokkan.service.translation.client;

import com.hb.dokkan.common.constants.TranslationConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @Description DokkanDB术语客户端
 * @Author stargazer
 * @Date 2026/8/15 19:30
 **/
@Slf4j
@Component
public class DokkanDbTerminologyClient {

    @Resource
    @Qualifier("dokkanDbWebClient")
    private WebClient dokkanDbJpClient;

    @Resource
    @Qualifier("dokkanDbGlobalWebClient")
    private WebClient dokkanDbGlobalClient;

    /**
     * 查询 JP 站分类术语，异常时返回空列表。
     *
     * @return 分类术语列表
     */
    public List<NamedEntry> listJpCategories() {
        return fetchNamedEntries(dokkanDbJpClient, TranslationConstants.DOKKAN_DB_CATEGORIES_PATH);
    }

    /**
     * 查询 Global 站分类术语，异常时返回空列表。
     *
     * @return 分类术语列表
     */
    public List<NamedEntry> listGlobalCategories() {
        return fetchNamedEntries(dokkanDbGlobalClient, TranslationConstants.DOKKAN_DB_CATEGORIES_PATH);
    }

    /**
     * 查询 JP 站链接术语，异常时返回空列表。
     *
     * @return 链接术语列表
     */
    public List<NamedEntry> listJpLinks() {
        return fetchNamedEntries(dokkanDbJpClient, TranslationConstants.DOKKAN_DB_LINKS_PATH);
    }

    /**
     * 查询 Global 站链接术语，异常时返回空列表。
     *
     * @return 链接术语列表
     */
    public List<NamedEntry> listGlobalLinks() {
        return fetchNamedEntries(dokkanDbGlobalClient, TranslationConstants.DOKKAN_DB_LINKS_PATH);
    }

    /**
     * 调用 DokkanDB 术语接口，Client 内部处理异常日志和空集合降级。
     *
     * @param sourceClient DokkanDB WebClient
     * @param path         术语接口路径
     * @return 术语条目列表
     */
    private List<NamedEntry> fetchNamedEntries(WebClient sourceClient, String path) {
        try {
            List<NamedEntry> rows = sourceClient.get()
                    .uri(uri -> uri.path(path).build())
                    .retrieve()
                    .bodyToFlux(NamedEntry.class)
                    .collectList()
                    .block(Duration.ofSeconds(TranslationConstants.DOKKAN_DB_TERMINOLOGY_TIMEOUT_SECONDS));
            return Objects.isNull(rows) ? Collections.emptyList() : rows;
        } catch (Exception e) {
            log.warn("DokkanDB terminology request failed, path={}", path, e);
            return Collections.emptyList();
        }
    }

    /**
     * DokkanDB 术语条目。
     *
     * @param id   术语 ID
     * @param name 术语原文名称
     */
    public record NamedEntry(Long id, String name) {
    }
}
