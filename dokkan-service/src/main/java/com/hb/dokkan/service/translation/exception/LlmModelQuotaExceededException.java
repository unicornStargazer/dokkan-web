package com.hb.dokkan.service.translation.exception;

/**
 * @Description LLM模型额度不足异常
 * @Author stargazer
 * @Date 2026/8/16 00:00
 **/
public class LlmModelQuotaExceededException extends RuntimeException {

    /**
     * 创建 LLM 模型额度不足异常。
     *
     * @param message 错误信息
     */
    public LlmModelQuotaExceededException(String message) {
        super(message);
    }

    /**
     * 创建 LLM 模型额度不足异常。
     *
     * @param message 错误信息
     * @param cause   原始异常
     */
    public LlmModelQuotaExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
