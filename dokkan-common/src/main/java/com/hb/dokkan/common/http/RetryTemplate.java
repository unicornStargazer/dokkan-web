package com.hb.dokkan.common.http;

import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * @Description xxxxx
 * @Author stargazer
 * @Date 2025/8/24 23:12
 **/
@Slf4j
public class RetryTemplate {

    public static <T> T executeWithRetry(Supplier<T> operation, RetryConfig retryConfig, String operationName) throws Exception {
        Exception lastException = null;

        for (int attempt = 0; attempt <= retryConfig.getMaxAttempts() ; attempt++) {
            try {
                log.debug("执行操作:{}, 第{}次尝试", operationName, attempt);

                T result = operation.get();

                if (Objects.nonNull(result)) {
                    if (attempt > 1) {
                        log.info("重试成功 {}, 第{}次尝试成功", operationName, attempt);
                    }
                    return result;
                } else {
                    log.warn("操作返回空结果:{}, 第{}次尝试", operationName, attempt);
                    continue;
                }
            } catch (Exception e) {
                lastException = e;
                log.warn("操作失败:{},第{}次尝试, error:{}", operationName, attempt, e.getMessage());

                if (attempt >= retryConfig.getMaxAttempts()) {
                    log.error("操作最终失败:{}, 已重试{}次", operationName, retryConfig.getMaxAttempts(), e);
                    throw lastException;
                }
            }
        }

        if (lastException != null) {
            throw lastException;
        }
        return null;
    }

    public static <T> T executeWithRetrySliently(Supplier<T> operation, RetryConfig retryConfig, String operationName) {

        try {
            return executeWithRetry(operation, retryConfig, operationName);
        }catch (Exception e) {
            log.error("重试操作最终失败");
            return null;
        }
    }



    /**
     * 重试配置接口
     */
    public interface RetryConfig {
        int getMaxAttempts();

        long getInitialDelayMs();

        double getBackoffMultiplier();

        long getMaxDelayMs();
    }
}
