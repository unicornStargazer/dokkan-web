package com.hb.dokkan.common.domain.dto.translation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** Responses 非流式响应，只读取完成状态、输出消息和用量，忽略协议扩展字段。 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LlmResponsesDTO(String status, List<Output> output, LlmTokenUsageDTO usage) {

    /** 输出项可能是推理、工具调用或消息，不假定第一项就是译文。 */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Output(String type, String role, List<Content> content) {
    }

    /** 消息内容可能是译文或拒绝，拒绝内容不得作为译文返回。 */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Content(String type, String text) {
    }
}
