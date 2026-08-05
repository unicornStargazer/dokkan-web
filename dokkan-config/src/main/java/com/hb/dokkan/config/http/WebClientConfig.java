package com.hb.dokkan.config.http;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import jakarta.annotation.Resource;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Resource
    private HttpPoolProperties httpPoolProperties;

    @Bean("dokkanWikiWebClient")
    public WebClient wikiWebClient() {

        HttpPoolProperties.WebClient config = httpPoolProperties.getWebClient();
        ConnectionProvider connectionProvider = ConnectionProvider.builder("dokkan")
                .maxConnections(config.getMaxConnections())
                .maxIdleTime(Duration.ofSeconds(config.getMaxIdleTimeSeconds()))
                .maxLifeTime(Duration.ofSeconds(config.getMaxLifeTimeSeconds()))
                .pendingAcquireTimeout(Duration.ofSeconds(config.getPendingAcquireTimeSeconds()))
                .evictInBackground(Duration.ofSeconds(config.getEvictInBackgroundTimeSeconds()))
                .build();

        HttpClient httpClient = HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectTimeoutMills())
                .doOnConnected(connection -> connection
                        .addHandlerLast(new ReadTimeoutHandler(config.getReadTimeoutSeconds()))
                        .addHandlerLast(new WriteTimeoutHandler(config.getWriteTimeoutSeconds()))
                );
        return WebClient.builder()
                .baseUrl("https://zh.dokkan.wiki")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean("dokkanInfoWebClient")
    public WebClient infoWebClient() {
        HttpPoolProperties.WebClient config = httpPoolProperties.getWebClient();
        final int bufferSize = config.getMaxInMemorySizeMb() * 1024 * 1024;

        final ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs
                        .defaultCodecs()
                        .maxInMemorySize(bufferSize))
                .build();
        ConnectionProvider connectionProvider = ConnectionProvider.builder("dokkan")
                .maxConnections(config.getMaxConnections())
                .maxIdleTime(Duration.ofSeconds(config.getMaxIdleTimeSeconds()))
                .maxLifeTime(Duration.ofSeconds(config.getMaxLifeTimeSeconds()))
                .pendingAcquireTimeout(Duration.ofSeconds(config.getPendingAcquireTimeSeconds()))
                .evictInBackground(Duration.ofSeconds(config.getEvictInBackgroundTimeSeconds()))
                .build();

        HttpClient httpClient = HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectTimeoutMills())
                .doOnConnected(connection -> connection
                        .addHandlerLast(new ReadTimeoutHandler(config.getReadTimeoutSeconds()))
                        .addHandlerLast(new WriteTimeoutHandler(config.getWriteTimeoutSeconds()))
                );
        return WebClient.builder()
                .baseUrl("https://dokkaninfo.com")
                .defaultHeader(HttpHeaders.USER_AGENT, "PostmanRuntime/7.45.0") // 模拟浏览器
                .exchangeStrategies(strategies)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean("dokkanDbWebClient")
    public WebClient dokkanDbWebClient() {
        HttpPoolProperties.WebClient config = httpPoolProperties.getWebClient();
        ConnectionProvider connectionProvider = ConnectionProvider.builder("dokkan-db")
                .maxConnections(config.getMaxConnections())
                .maxIdleTime(Duration.ofSeconds(config.getMaxIdleTimeSeconds()))
                .maxLifeTime(Duration.ofSeconds(config.getMaxLifeTimeSeconds()))
                .pendingAcquireTimeout(Duration.ofSeconds(config.getPendingAcquireTimeSeconds()))
                .evictInBackground(Duration.ofSeconds(config.getEvictInBackgroundTimeSeconds()))
                .build();
        HttpClient httpClient = HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectTimeoutMills())
                .doOnConnected(connection -> connection
                        .addHandlerLast(new ReadTimeoutHandler(config.getReadTimeoutSeconds()))
                        .addHandlerLast(new WriteTimeoutHandler(config.getWriteTimeoutSeconds())));
        return WebClient.builder()
                .baseUrl("https://api.dokkandb.com/jp")
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean("googleTranslationWebClient")
    public WebClient googleTranslationWebClient() {
        return WebClient.builder()
                .baseUrl("https://translate.googleapis.com")
                .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0")
                .build();
    }

    @Bean("dokkanDbGlobalWebClient")
    public WebClient dokkanDbGlobalWebClient() {
        HttpPoolProperties.WebClient config = httpPoolProperties.getWebClient();
        ConnectionProvider connectionProvider = ConnectionProvider.builder("dokkan-db-global")
                .maxConnections(config.getMaxConnections())
                .maxIdleTime(Duration.ofSeconds(config.getMaxIdleTimeSeconds()))
                .maxLifeTime(Duration.ofSeconds(config.getMaxLifeTimeSeconds()))
                .pendingAcquireTimeout(Duration.ofSeconds(config.getPendingAcquireTimeSeconds()))
                .evictInBackground(Duration.ofSeconds(config.getEvictInBackgroundTimeSeconds()))
                .build();
        HttpClient httpClient = HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectTimeoutMills())
                .doOnConnected(connection -> connection
                        .addHandlerLast(new ReadTimeoutHandler(config.getReadTimeoutSeconds()))
                        .addHandlerLast(new WriteTimeoutHandler(config.getWriteTimeoutSeconds())));
        return WebClient.builder()
                .baseUrl("https://api.dokkandb.com")
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean("dokkanInfoRestClient")
    public RestClient infoRestClient() {
        return RestClient.builder()
                .baseUrl("https://dokkaninfo.com")
                .defaultHeader(HttpHeaders.USER_AGENT, "PostmanRuntime/7.45.0") // 模拟浏览器
                .requestFactory(apiHttpRequestFactory())
                .build();
    }

    @Bean("dokkanWikiRestClient")
    public RestClient wikiRestClient() {
        return RestClient.builder()
                .baseUrl("https://zh.dokkan.wiki")
                .defaultHeader(HttpHeaders.USER_AGENT, "PostmanRuntime/7.45.0") // 模拟浏览器
                .requestFactory(apiHttpRequestFactory())
                .build();
    }

    @Bean
    public ClientHttpRequestFactory apiHttpRequestFactory() {
        HttpPoolProperties.WebClient config = httpPoolProperties.getWebClient();
        PoolingHttpClientConnectionManager clientConnectionManager = new PoolingHttpClientConnectionManager();
        clientConnectionManager.setMaxTotal(config.getMaxConnections());
        clientConnectionManager.setDefaultMaxPerRoute(config.getMaxConnections());

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(clientConnectionManager)
                .build();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout(config.getConnectTimeoutMills());
        factory.setConnectionRequestTimeout(config.getConnectTimeoutMills());
        return factory;
    }

}
