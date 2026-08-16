package com.hb.dokkan.common.domain.dto.translation;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description LLM翻译结果DTO
 * @Author stargazer
 * @Date 2026/8/16 00:00
 **/
@Data
public class LlmTranslationResultDTO implements Serializable {

    /** 序列化版本号，保证 DTO 跨版本反序列化兼容。 */
    private static final long serialVersionUID = 1L;

    /** 本次翻译实际使用的模型。 */
    private String model;

    /** LLM 返回的翻译内容。 */
    private String content;

    /** LLM 返回的 token 用量，接口未返回 usage 时为空。 */
    private LlmTokenUsageDTO usage;
}
