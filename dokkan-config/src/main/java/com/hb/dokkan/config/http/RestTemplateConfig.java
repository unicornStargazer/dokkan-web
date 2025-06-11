package com.hb.dokkan.config.http;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        // 直接在这里创建和配置工厂，减少出错环节
        return new RestTemplate(clientHttpRequestFactory());
    }

    private ClientHttpRequestFactory clientHttpRequestFactory() {
        // 使用 HttpClient 5 的工厂
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();

        // 配置连接池
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(200);
        connectionManager.setDefaultMaxPerRoute(50);

//        // 配置请求设置
//        RequestConfig requestConfig = RequestConfig.custom()
//                .setConnectionRequestTimeout(Timeout.ofSeconds(120)) // 从连接池获取连接的超时
//                .setConnectTimeout(Timeout.ofSeconds(1)) // 建立连接的超时
//                .setResponseTimeout(Timeout.ofSeconds(120)) // 等待响应的超时
//                .build();

        // 创建 HttpClient
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
//                .setDefaultRequestConfig(requestConfig)
                .build();

        factory.setHttpClient(httpClient);
        return factory;
    }
}