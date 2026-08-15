package com.hb.dokkan.common.domain.request.translation;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description 翻译映射状态变更请求
 * @Author stargazer
 * @Date 2026/8/15 23:30
 **/
@Data
public class TranslationMappingStatusRequest implements Serializable {

    /** 序列化版本号，保证请求对象跨版本反序列化兼容。 */
    private static final long serialVersionUID = 1L;

    /** 翻译映射 ID。 */
    private String id;

    /** 是否启用。 */
    private Boolean enabled;
}
