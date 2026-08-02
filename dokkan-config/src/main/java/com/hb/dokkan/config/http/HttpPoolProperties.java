package com.hb.dokkan.config.http;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Description xxxxx
 * @Author stargazer
 * @Date 2025/8/24 21:36
 **/
@Data
@Component
@ConfigurationProperties(prefix = "http.pool")
public class HttpPoolProperties {

    /**
     * RestTemplate配置
     */
    private RestTemplate restTemplate = new RestTemplate();

    /**
     * webClient配置
     */
    private WebClient webClient = new WebClient();

    /**
     * 并发配置
     */
    private Concurrency concurrency = new Concurrency();

    /**
     * 重试配置
     */
    private Retry retry = new Retry();




    @Data
    public static class RestTemplate {

        private int maxTotal = 1000;

        private int defaultMaxPerRoute = 100;

        private int connectTimeout = 5000;

        private int readTimeout = 5000;

        private int connectionRequestTimeout = 5000;

    }

    @Data
    public static class WebClient {

        private int maxConnections = 1000;

        private int connectTimeoutMills = 5000;

        private int readTimeoutSeconds = 5000;

        private int writeTimeoutSeconds = 5000;

        private int maxIdleTimeSeconds = 5000;

        private int maxLifeTimeSeconds = 5000;

        private int pendingAcquireTimeSeconds = 5000;

        private int evictInBackgroundTimeSeconds = 5000;

        /**
         * Maximum response body size buffered by WebClient codecs, in MB.
         */
        private int maxInMemorySizeMb = 64;

    }

    @Data
    public static class Concurrency {

        private int semaphorePermits = 100;

        private int batchDelayMills = 1000;

        private int batchSize = 10000;

    }

    @Data
    public static class Retry implements RetryTemplate.RetryConfig {

        /**
         * 最大重试数
         */
        private int maxAttempts = 5;

        private long initialDelayMs = 2000;

        private double backoffMultiplier = 1.5;

        private long maxDelayMs = 3000;
    }


}
