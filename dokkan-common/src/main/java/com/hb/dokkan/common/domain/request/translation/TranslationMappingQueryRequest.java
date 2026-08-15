package com.hb.dokkan.common.domain.request.translation;

import com.hb.dokkan.common.domain.request.base.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Description 翻译映射查询请求
 * @Author stargazer
 * @Date 2026/8/15 23:30
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class TranslationMappingQueryRequest extends PageRequest implements Serializable {

    /** 序列化版本号，保证请求对象跨版本反序列化兼容。 */
    private static final long serialVersionUID = 1L;

    /** 关键词，匹配原文、译文或备注。 */
    private String keyword;

    /** 原文术语或短语。 */
    private String sourceText;

    /** 中文译文。 */
    private String targetText;

    /** 源语言。 */
    private String sourceLanguage;

    /** 映射类型。 */
    private String mappingType;

    /** 是否启用。 */
    private Boolean enabled;
}
