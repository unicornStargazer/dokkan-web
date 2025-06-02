package com.hb.dokkan.common.http;

import com.hb.dokkan.common.utils.JsonUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class CommonHttpClient {
    private RestTemplate restTemplate;
    private HttpHeaders headers;

    public <T> T getForEntity(String url, Class<T> responseType) {
        ResponseEntity<String> response = null;
        try {
            response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(null,headers),
                    String.class
            );
        } catch (RestClientException e) {
            log.error("请求失败，url:{}, error:{}",url,e.getMessage());
            return null;
        }
        if (response.getStatusCode().isError()){
            return null;
        }
        String body = response.getBody();
        if (body == null) {
            return null;
        }
        return JsonUtils.json2Object(body,responseType);
    }


    public <T> List<T> getForList(String url, Class<T> responseType) {
        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = restTemplate.exchange(
                    url, HttpMethod.GET,
                    new HttpEntity<>(null, headers),
                    String.class);
        } catch (RestClientException e) {
            log.error("请求失败，url:{}, error:{}",url,e.getMessage());
            return new ArrayList<>();
        }
        String response = responseEntity.getBody();
        List<T> list = JsonUtils.json2List(response, responseType);
        return list;
    }
    public <T,V> List<T> postForList(String url, Class<T> responseType,V request) {
        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(request,headers),
                    String.class
            );
        } catch (RestClientException e) {
            log.error("请求失败，url:{}, error:{}",url,e.getMessage());
            return new ArrayList<>();
        }
        String response = responseEntity.getBody();
        List<T> list = JsonUtils.json2List(response, responseType);
        return list;
    }

    public <T,V> T postForEntity(String url, Class<T> responseType, V request) {
        ResponseEntity<String> response = null;
        try {
            response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(request,headers),
                    String.class
            );
        } catch (RestClientException e) {
            log.error("请求失败，url:{}, error:{}",url,e.getMessage());
            return null;
        }
        String body = response.getBody();
        return JsonUtils.json2Object(body,responseType);
    }

    @PostConstruct
    public void init() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(100000); // 设置连接超时时间
        factory.setReadTimeout(100000); // 设置读取超时时间
        restTemplate = new RestTemplate();
        headers = getHeaders();
    }

    private static HttpHeaders getHeaders(){
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "PostmanRuntime/7.42.0");
        headers.set("Connection", "keep-alive");
        headers.set("Content-Type","application/json");
        headers.set("Accept","application/json");
        return headers;
    }
}
