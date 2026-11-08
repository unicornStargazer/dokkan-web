package com.hb.dokkan.service.client;

import com.google.common.util.concurrent.RateLimiter;
import com.hb.dokkan.common.constants.CardSyncConstants;
import com.hb.dokkan.common.constants.ExceptionErrorCode;
import com.hb.dokkan.common.domain.dto.data.kx.*;
import com.hb.dokkan.common.exception.domain.DokkanBizException;
import com.hb.dokkan.common.utils.DateUtils;
import com.hb.dokkan.common.utils.JsonUtils;
import com.hb.dokkan.config.kx.KxWikiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * @Description KX Wiki 客户端，统一限速、重试、繁转简和失败日志
 * @Author Codex
 * @Date 2026/11/8
 **/
@Component
@Slf4j
public class KxWikiClient {
    /**
     * KX Wiki 专用 HTTP 客户端。
     **/
    private final WebClient webClient;
    /**
     * KX Wiki 地址、区域、语言及限速重试配置。
     **/
    private final KxWikiProperties properties;
    /**
     * 所有 KX Wiki 端点共用的进程内请求限速器。
     **/
    private final RateLimiter rateLimiter;

    /**
     * 初始化进程内共享的限速器；非法配置阻止客户端启动。
     **/
    public KxWikiClient(@Qualifier("kxWikiWebClient") WebClient webClient, KxWikiProperties properties) {
        if (!isValidConfiguration(properties)) {
            throw new IllegalArgumentException(CardSyncConstants.KX_WIKI_INVALID_CONFIG_MESSAGE);
        }
        this.webClient = webClient;
        this.properties = properties;
        this.rateLimiter = RateLimiter.create(properties.getPermitsPerSecond());
    }

    /**
     * 依次校验共享限速、单次超时和重试次数的有效范围。
     **/
    private static boolean isValidConfiguration(KxWikiProperties properties) {
        if (properties.getPermitsPerSecond() <= 0) {
            return false;
        } else if (properties.getTimeoutMills() <= 0) {
            return false;
        } else {
            return properties.getRetryTimes() >= 0;
        }
    }

    /**
     * 查询 status；请求或解析失败时返回空 Optional，不更新同步水位。
     **/
    public Optional<KxWikiStatusDTO> getStatus() {
        return get(CardSyncConstants.KX_WIKI_STATUS_PATH, KxWikiStatusDTO.class);
    }

    /**
     * 读取卡片分页；失败抛业务异常，禁止把失败页当作列表结束。
     **/
    public KxWikiPageResponse<KxWikiCardListItemDTO> listCards(int page) {
        return getPage(CardSyncConstants.KX_WIKI_CARDS_PATH, page, properties.getLang(),
                CardSyncConstants.KX_WIKI_CARDS_FIELD, KxWikiCardListItemDTO.class);
    }

    /**
     * 查询卡片详情，保留原始 JSON 供内容 hash 使用；失败返回空 Optional。
     **/
    public Optional<KxWikiCardDetailDTO> getCardDetail(long cardId) {
        Optional<String> raw = request(String.format(CardSyncConstants.KX_WIKI_CARD_DETAIL_PATH, cardId), null, properties.getLang());
        return parse(raw, KxWikiCardDetailDTO.class).map(detail -> {
            detail.setSourceJson(raw.orElseThrow());
            return detail;
        });
    }

    /**
     * 查询必杀 enrichment；失败返回空 Optional，不写入缺少倍率的卡片。
     **/
    public Optional<KxWikiEnrichmentDTO> getEnrichment(long cardId) {
        Optional<String> raw = request(String.format(CardSyncConstants.KX_WIKI_CARD_ENRICHMENT_PATH, cardId), null, properties.getLang());
        return parse(raw, KxWikiEnrichmentDTO.class).map(enrichment -> {
            enrichment.setSourceJson(raw.orElseThrow());
            return enrichment;
        });
    }

    /**
     * 查询极限开放日期 extras；失败返回空 Optional。
     **/
    public Optional<KxWikiExtrasDTO> getExtras(long cardId) {
        Optional<String> raw = request(String.format(CardSyncConstants.KX_WIKI_CARD_EXTRAS_PATH, cardId), null, properties.getLang());
        return parse(raw, KxWikiExtrasDTO.class).map(extras -> {
            extras.setSourceJson(raw.orElseThrow());
            return extras;
        });
    }

    /**
     * 按语言读取全部分类；失败抛业务异常，已有中文名由持久化层保护。
     **/
    public List<KxWikiReferenceItemDTO> listCategories(String language) {
        return listReference(CardSyncConstants.KX_WIKI_CATEGORIES_PATH, language);
    }

    /**
     * 读取全部链接参考数据；失败抛业务异常。
     **/
    public List<KxWikiReferenceItemDTO> listLinks() {
        return listReference(CardSyncConstants.KX_WIKI_LINKS_PATH, properties.getLang());
    }

    /**
     * 顺序遍历 reference 分页，限定页数防止上游异常造成无限循环。
     **/
    private List<KxWikiReferenceItemDTO> listReference(String path, String language) {
        List<KxWikiReferenceItemDTO> items = new ArrayList<>();
        for (int page = 1; page <= CardSyncConstants.KX_WIKI_MAX_PAGES; page++) {
            KxWikiPageResponse<KxWikiReferenceItemDTO> response = getPage(path, page, language,
                    CardSyncConstants.KX_WIKI_ITEMS_FIELD, KxWikiReferenceItemDTO.class);
            items.addAll(response.getItems());
            if (page >= response.getPages()) {
                return items;
            }
        }
        throw new DokkanBizException(ExceptionErrorCode.GET_WIKI_INFO_ERROR);
    }

    /**
     * 读取明确字段的分页模型，空页返回空 items，失败页抛业务异常。
     **/
    private <T> KxWikiPageResponse<T> getPage(String path, int page, String language, String field, Class<T> itemType) {
        String raw = request(path, page, language).orElseThrow(() -> new DokkanBizException(ExceptionErrorCode.GET_WIKI_INFO_ERROR));
        try {
            Map<String, Object> values = JsonUtils.json2Map(raw, String.class, Object.class);
            if (!isValidPageResponse(values, field)) {
                throw new DokkanBizException(ExceptionErrorCode.GET_WIKI_INFO_ERROR);
            }
            KxWikiPageResponse<T> response = new KxWikiPageResponse<>();
            response.setPage(page);
            response.setPages(((Number) values.get(CardSyncConstants.KX_WIKI_PAGES_FIELD)).intValue());
            response.setItems(JsonUtils.convertList(values.get(field), itemType));
            return response;
        } catch (RuntimeException exception) {
            log.error("KX Wiki page parse failed, path={}, page={}", path, page, exception);
            throw new DokkanBizException(ExceptionErrorCode.GET_WIKI_INFO_ERROR, exception);
        }
    }

    /**
     * 校验分页数组和总页数，防止字段缺失被当作成功空页。
     **/
    private boolean isValidPageResponse(Map<String, Object> values, String itemsField) {
        if (!(values.get(itemsField) instanceof List<?>)) {
            return false;
        } else if (!(values.get(CardSyncConstants.KX_WIKI_PAGES_FIELD) instanceof Number pages)) {
            return false;
        } else {
            return pages.intValue() >= 1;
        }
    }

    /**
     * 单对象 GET 的统一入口。
     **/
    private <T> Optional<T> get(String path, Class<T> type) {
        return parse(request(path, null, properties.getLang()), type);
    }

    /**
     * 在客户端边界转换已解码的中文文本，再按严格 JSON 工具反序列化。
     **/
    private <T> Optional<T> parse(Optional<String> raw, Class<T> type) {
        try {
            return raw.map(value -> JsonUtils.json2Object(value, type));
        } catch (RuntimeException exception) {
            log.error("KX Wiki response parse failed, target={}", type.getSimpleName(), exception);
            return Optional.empty();
        }
    }

    /**
     * 每次尝试都取得共享许可，仅对 429、5xx、超时和网络异常进行退避重试。
     **/
    private Optional<String> request(String path, Integer page, String language) {
        for (int attempt = 0; attempt <= properties.getRetryTimes(); attempt++) {
            try {
                rateLimiter.acquire();
                String raw = webClient.get().uri(builder -> {
                    builder.path(path).queryParam(CardSyncConstants.KX_WIKI_REGION_PARAM, properties.getRegion())
                            .queryParam(CardSyncConstants.KX_WIKI_LANGUAGE_PARAM, language);
                    if (page != null) {
                        builder.queryParam(CardSyncConstants.KX_WIKI_PAGE_PARAM, page);
                    }
                    return builder.build();
                }).retrieve().bodyToMono(String.class).timeout(Duration.ofMillis(properties.getTimeoutMills())).block();
                // 先解码 JSON 转义，再繁转简；URL、模板和字段名保持原值。
                return Optional.ofNullable(raw == null ? null : JsonUtils.simplifyJsonText(raw));
            } catch (RuntimeException exception) {
                boolean retryable = isRetryable(exception);
                if (!retryable || attempt == properties.getRetryTimes()) {
                    log.error("KX Wiki request failed, path={}, page={}, attempt={}", path, page, attempt, exception);
                    return Optional.empty();
                }
                long delay = retryDelay(exception, attempt);
                log.warn("KX Wiki retry, path={}, page={}, delayMs={}", path, page, delay);
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }

    /**
     * 判断异常链是否属于允许重试的瞬时错误。
     **/
    private boolean isRetryable(Throwable exception) {
        if (exception instanceof WebClientResponseException response) {
            return response.getStatusCode().value() == CardSyncConstants.HTTP_TOO_MANY_REQUESTS
                    || response.getStatusCode().is5xxServerError();
        }
        if (exception instanceof WebClientRequestException) {
            return true;
        } else if (exception instanceof TimeoutException) {
            return true;
        } else if (exception.getCause() != null) {
            return isRetryable(exception.getCause());
        } else {
            return false;
        }
    }

    /**
     * 优先使用 429 Retry-After 秒数或 HTTP 日期，否则指数退避。
     **/
    private long retryDelay(RuntimeException exception, int attempt) {
        long backoff = (long) (CardSyncConstants.KX_WIKI_RETRY_DELAY_MILLIS * Math.pow(2, attempt));
        if (exception instanceof WebClientResponseException response
                && response.getStatusCode().value() == CardSyncConstants.HTTP_TOO_MANY_REQUESTS) {
            String retryAfter = response.getHeaders().getFirst(org.springframework.http.HttpHeaders.RETRY_AFTER);
            return DateUtils.retryAfterMillis(retryAfter, backoff);
        }
        return backoff;
    }
}
