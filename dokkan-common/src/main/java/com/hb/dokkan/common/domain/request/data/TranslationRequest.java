package com.hb.dokkan.common.domain.request.data;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description 翻译请求
 * @Author stargazer
 * @Date 2026/8/15 19:00
 **/
@Data
public class TranslationRequest implements Serializable {

    /** 序列化版本号，保证请求对象跨版本反序列化兼容。 */
    private static final long serialVersionUID = 1L;

    /** 待翻译文本，不能为空且长度不能超过翻译接口限制。 */
    private String text;
}
