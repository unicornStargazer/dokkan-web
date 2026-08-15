package com.hb.dokkan.common.domain.request.translation;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description 翻译映射删除请求
 * @Author stargazer
 * @Date 2026/8/15 23:30
 **/
@Data
public class TranslationMappingDeleteRequest implements Serializable {

    /** 序列化版本号，保证请求对象跨版本反序列化兼容。 */
    private static final long serialVersionUID = 1L;

    /** 待删除翻译映射 ID 列表。 */
    private List<String> ids;
}
