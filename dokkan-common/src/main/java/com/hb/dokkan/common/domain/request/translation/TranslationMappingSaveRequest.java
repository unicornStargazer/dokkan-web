package com.hb.dokkan.common.domain.request.translation;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description 翻译映射保存请求
 * @Author stargazer
 * @Date 2026/8/15 23:30
 **/
@Data
public class TranslationMappingSaveRequest implements Serializable {

    /** 序列化版本号，保证请求对象跨版本反序列化兼容。 */
    private static final long serialVersionUID = 1L;

    /** 翻译映射 ID，新增时为空。 */
    private String id;

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

    /** 备注说明。 */
    private String remark;
}
