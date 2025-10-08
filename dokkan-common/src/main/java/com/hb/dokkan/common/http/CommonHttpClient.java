package com.hb.dokkan.common.http;

import com.hb.dokkan.common.exception.domain.DokkanSysException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.*;

@Slf4j
@Component
public class CommonHttpClient {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(15);

    @Resource(name = "dokkanWikiWebClient")
    private WebClient wikiClient;

    @Resource(name = "dokkanInfoWebClient")
    private WebClient infoClient;

    @Resource(name = "dokkanWikiRestClient")
    private RestClient restClient;

    /**
     * get请求 返回单个对象
     */
    public <T> Optional<T> getForObject(String uri, Class<T> responseType, Map<String, String> uriVariables) {

        try {
            T result = execute(wikiClient.get()
                    .uri(uriBuilder -> buildUri(uri, uriVariables, uriBuilder))
                    .retrieve(), responseType);
            return Optional.ofNullable(result);

        }catch (Exception e) {
            log.error("getForObject error! uri:{} exception:{}",uri, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * get请求 返回单个对象
     */
    public <T> Optional<T> getForObjectSync(String uri, Class<T> responseType, Map<String, String> uriVariables) {

        try {
            T result = restClient.get()
                    .uri(uriBuilder -> buildUri(uri, uriVariables, uriBuilder))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(responseType);
            return result == null ? Optional.empty() : Optional.of(result);
        }catch (Exception e) {
            log.error("getForObject error! uri:{} exception:{}",uri, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * get请求 返回列表
     */
    public <T> List<T> getForList(String uri, Class<T> elementType, Map<String, String> uriVariables) {

        try {
            List<T> list = execute(wikiClient.get()
                    .uri(uriBuilder -> buildUri(uri, uriVariables, uriBuilder))
                    .retrieve()
                    .bodyToFlux(elementType)
                    .collectList());
            return Objects.nonNull(list) ? list : Collections.emptyList();
        }catch (Exception e) {
            log.error("getForList error! uri:{} exception:{}",uri, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * post请求 返回单个对象
     */
    public <T, R> Optional<T> postForObject(String uri, R requestBody, Class<T> responseType, Map<String, String> uriVariables) {

        try {
            T result = execute(wikiClient.post()
                    .uri(uriBuilder -> buildUri(uri, uriVariables, uriBuilder))
                    .bodyValue(requestBody)
                    .retrieve(), responseType);
            return Optional.ofNullable(result);
        }catch (Exception e) {
            log.error("postForObject error! uri:{} exception:{}",uri, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 构建URI
     */
    private URI buildUri(String uri, Map<String, String> uriVariables, UriBuilder uriBuilder) {
        if (ObjectUtils.isNotEmpty(uriVariables)) {
            return uriBuilder.path(uri).build(uriVariables);
        }else {
            return uriBuilder.path(uri).build();
        }
    }


    /**
     * 实际执行方法
     */
    private <T> T execute(WebClient.ResponseSpec responseSpec, Class<T> responseType) {
        return responseSpec
                .onStatus(HttpStatusCode::is4xxClientError, resp ->
                        Mono.error(new DokkanSysException("客户端错误: " + resp.statusCode() )))
                .onStatus(HttpStatusCode::is5xxServerError, resp ->
                        Mono.error(new DokkanSysException("服务器错误: " + resp.statusCode() )))
                .bodyToMono(responseType)
                .doOnError(ex -> log.error("Http请求失败:{}", ex.getMessage()))
                .block(DEFAULT_TIMEOUT);
    }

    /**
     * 实际执行方法
     */
    private <T> T execute(Mono<T> mono) {
        return mono
                .doOnError(ex -> log.error("Http请求失败:{}", ex.getMessage()))
                .block(DEFAULT_TIMEOUT);
    }

}
