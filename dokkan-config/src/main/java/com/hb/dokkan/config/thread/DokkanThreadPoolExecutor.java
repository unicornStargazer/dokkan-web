package com.hb.dokkan.config.thread;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Description 线程池
 * @Author stargazer
 * @Date 2025/6/8 20:02
 **/
@Component
public class DokkanThreadPoolExecutor extends ThreadPoolTaskExecutor {

    @Value("${thread.pool.corePoolSize}")
    private int corePoolSize;

    @Value("${thread.pool.maxPoolSize}")
    private int maxPoolSize;

    @Value("${thread.pool.keepAliveSeconds}")
    private int keepAliveSeconds;

    @Value("${thread.pool.queueCapacity}")
    private int queueCapacity;


    @PostConstruct
    public void customInitialize() {
        this.setCorePoolSize(corePoolSize);
        this.setMaxPoolSize(maxPoolSize);
        this.setKeepAliveSeconds(keepAliveSeconds);
        this.setThreadNamePrefix("Dokkan-");
        this.setQueueCapacity(queueCapacity);
        this.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        this.initialize();
        logger.info("DokkanThreadPoolExecutor has been initialized.");
    }

    @Override
    public void shutdown() {
        logger.info("DokkanThreadPoolExecutor is shutting down...");
        super.shutdown();
    }
}
