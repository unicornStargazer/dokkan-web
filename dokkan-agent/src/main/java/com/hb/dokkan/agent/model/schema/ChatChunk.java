package com.hb.dokkan.agent.model.schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流式对话的增量响应模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatChunk {
    /**
     * 增量的文本内容
     */
    private String delta;
    
    /**
     * 结束原因，如果未结束则为空
     */
    private String finishReason;
}