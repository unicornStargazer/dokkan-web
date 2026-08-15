package com.hb.dokkan.service.storage.client;

import com.hb.dokkan.common.constants.CardSyncConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @Description 卡片头像下载客户端
 * @Author stargazer
 * @Date 2026/8/15 21:20
 **/
@Slf4j
@Component
public class CardIconDownloadClient {

    @Resource
    private RestTemplate restTemplate;

    /**
     * 下载卡片头像，失败时记录日志并返回 null，由上层决定是否降级。
     *
     * @param cardId    卡片 ID
     * @param sourceUrl 头像源站 URL
     * @return 头像字节数组，失败或空响应时返回 null
     */
    public byte[] download(Long cardId, String sourceUrl) {
        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    sourceUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(buildHeaders()),
                    byte[].class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("download card icon failed, cardId={}, status={}", cardId, response.getStatusCode());
                return null;
            }
            return response.getBody();
        } catch (RestClientResponseException e) {
            log.warn("download card icon failed, cardId={}, status={}", cardId, e.getStatusCode());
            return null;
        } catch (Exception e) {
            log.warn("download card icon failed, cardId={}, sourceUrl={}", cardId, sourceUrl, e);
            return null;
        }
    }

    /**
     * 构造头像下载请求头。
     *
     * @return HTTP 请求头
     */
    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, CardSyncConstants.CARD_ICON_USER_AGENT);
        headers.setAccept(List.of(MediaType.IMAGE_PNG, MediaType.APPLICATION_OCTET_STREAM));
        return headers;
    }

    /**
     * 判断下载结果是否为空。
     *
     * @param iconBytes 头像字节数组
     * @return 是否为空
     */
    public boolean isEmpty(byte[] iconBytes) {
        return Objects.isNull(iconBytes) || iconBytes.length == 0;
    }
}
