package com.hb.dokkan.config.http;

import io.netty.channel.ChannelOption;
import com.hb.dokkan.config.kx.KxWikiProperties;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import org.springframework.util.unit.DataSize;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    /**
     * HTTP 连接池与网络超时配置。
     **/
    @Resource
    private HttpPoolProperties httpPoolProperties;

    /**
     * KX Wiki 地址、语言、请求限速及重试配置。
     **/
    @Resource
    private KxWikiProperties kxWikiProperties;

    /**
     * 创建 KX Wiki 专用 WebClient，避免与历史数据源共享 base URL 和请求头。
     *
     * @return KX Wiki WebClient
     **/
    @Bean("kxWikiWebClient")
    public WebClient kxWikiWebClient() {
        HttpPoolProperties.WebClient config = httpPoolProperties.getWebClient();
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectTimeoutMills())
                .responseTimeout(Duration.ofMillis(kxWikiProperties.getTimeoutMills()))
                .doOnConnected(connection -> connection
                        .addHandlerLast(new ReadTimeoutHandler(config.getReadTimeoutSeconds()))
                        .addHandlerLast(new WriteTimeoutHandler(config.getWriteTimeoutSeconds())));
        return WebClient.builder()
                .baseUrl(kxWikiProperties.getBaseUrl())
                .defaultHeader(HttpHeaders.USER_AGENT, kxWikiProperties.getUserAgent())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .codecs(codecs -> codecs.defaultCodecs()
                        .maxInMemorySize(Math.toIntExact(DataSize.ofMegabytes(config.getMaxInMemorySizeMb()).toBytes())))
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

}
