package com.hb.dokkan.config.kx;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * KX Wiki HTTP 客户端配置。
 **/
@Data
@Component
@ConfigurationProperties(prefix = "kx.wiki")
public class KxWikiProperties {
    /**
     * KX Wiki 服务地址。
     **/
    private String baseUrl = "https://wiki.kx-dokkan.com";
    /**
     * 数据区域。
     **/
    private String region = "global";
    /**
     * 数据语言。
     **/
    private String lang = "zh";
    /**
     * 每秒允许的请求数。
     **/
    private double permitsPerSecond = 2D;
    /**
     * 单次请求超时时间，单位毫秒。
     **/
    private long timeoutMills = 30_000L;
    /**
     * 可重试次数。
     **/
    private int retryTimes = 2;
    /**
     * 请求标识，便于站方识别。
     **/
    private String userAgent = "dokkan-web/1.0 (authorized KX Wiki client)";
}
