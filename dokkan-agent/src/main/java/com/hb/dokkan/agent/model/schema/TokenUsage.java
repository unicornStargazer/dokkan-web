package com.hb.dokkan.agent.model.schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token 消耗统计
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenUsage {
    /**
     * 输入（提示词）消耗的 Token 数量
     */
    private Integer promptTokens;
    
    /**
     * 输出（生成内容）消耗的 Token 数量
     */
    private Integer completionTokens;
    
    /**
     * 总计消耗的 Token 数量
     */
    private Integer totalTokens;
}