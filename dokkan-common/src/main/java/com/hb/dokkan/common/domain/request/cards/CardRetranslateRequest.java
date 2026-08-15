package com.hb.dokkan.common.domain.request.cards;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description 单卡重新翻译请求
 * @Author stargazer
 * @Date 2026/8/16 00:30
 **/
@Data
public class CardRetranslateRequest implements Serializable {

    /** 序列化版本号，保证请求对象跨版本反序列化兼容。 */
    private static final long serialVersionUID = 1L;

    /** 卡片 ID。 */
    private Long cardId;
}
