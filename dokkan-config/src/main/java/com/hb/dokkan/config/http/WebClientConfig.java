package com.hb.dokkan.config.http;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
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
        final int bufferSize = 10 * 1024 * 1024; // 10MB

        final ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs
                        .defaultCodecs()
                        .maxInMemorySize(bufferSize))
                .build();
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
                .baseUrl("https://dokkaninfo.com")
                .defaultHeader(HttpHeaders.USER_AGENT, "PostmanRuntime/7.45.0") // 模拟浏览器
                .exchangeStrategies(strategies)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

}
