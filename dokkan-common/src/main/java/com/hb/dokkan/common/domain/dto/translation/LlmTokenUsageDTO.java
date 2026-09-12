package com.hb.dokkan.common.domain.dto.translation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description LLM token用量DTO
 * @Author stargazer
 * @Date 2026/8/16 00:00
 **/
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LlmTokenUsageDTO implements Serializable {

    /** 序列化版本号，保证 DTO 跨版本反序列化兼容。 */
    private static final long serialVersionUID = 1L;

    /** 输入 prompt 消耗的 token 数。 */
    @JsonProperty("prompt_tokens")
    @JsonAlias("input_tokens")
    private long promptTokens;

    /** 输出 completion 消耗的 token 数。 */
    @JsonProperty("completion_tokens")
    @JsonAlias("output_tokens")
    private long completionTokens;

    /** 本次请求总消耗 token 数。 */
    @JsonProperty("total_tokens")
    private long totalTokens;
}
