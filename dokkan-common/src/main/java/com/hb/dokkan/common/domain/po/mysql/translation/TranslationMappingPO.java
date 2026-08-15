package com.hb.dokkan.common.domain.po.mysql.translation;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hb.dokkan.common.domain.po.mysql.base.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Description 翻译映射数据库类
 * @Author stargazer
 * @Date 2026/8/15 23:30
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("translation_mapping")
public class TranslationMappingPO extends BasePO {

    /** 原文术语或短语。 */
    private String sourceText;

    /** 中文译文。 */
    private String targetText;

    /** 源语言，默认 auto 表示自动判断。 */
    private String sourceLanguage;

    /** 映射类型，用于区分系统、用户、角色、技能等来源。 */
    private String mappingType;

    /** 是否启用，禁用后不进入翻译词表。 */
    private Boolean enabled;

    /** 备注说明。 */
    private String remark;
}
