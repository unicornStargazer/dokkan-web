package com.hb.dokkan.common.constants;

/**
 * @Description 翻译映射管理常量
 * @Author stargazer
 * @Date 2026/8/15 23:30
 **/
public final class TranslationMappingConstants {

    /** 翻译映射默认源语言，表示由翻译链路自动判断。 */
    public static final String DEFAULT_SOURCE_LANGUAGE = "auto";

    /** 系统内置映射类型，来源于默认静态词表。 */
    public static final String MAPPING_TYPE_SYSTEM = "SYSTEM";

    /** 用户自定义映射类型，来源于页面或历史自定义词表。 */
    public static final String MAPPING_TYPE_CUSTOM = "CUSTOM";

    /** 通用术语映射类型。 */
    public static final String MAPPING_TYPE_TERM = "TERM";

    /** 角色名称映射类型。 */
    public static final String MAPPING_TYPE_CHARACTER = "CHARACTER";

    /** 技能名称映射类型。 */
    public static final String MAPPING_TYPE_SKILL = "SKILL";

    /** 分类名称映射类型。 */
    public static final String MAPPING_TYPE_CATEGORY = "CATEGORY";

    /** 链接技能名称映射类型。 */
    public static final String MAPPING_TYPE_LINK = "LINK";

    /** 属性、系等类型映射类型。 */
    public static final String MAPPING_TYPE_TYPE = "TYPE";

    /** 翻译映射默认启用状态。 */
    public static final Boolean DEFAULT_ENABLED = Boolean.TRUE;

    /** 翻译映射默认页码。 */
    public static final int DEFAULT_PAGE_NUM = 1;

    /** 翻译映射默认每页数量。 */
    public static final int DEFAULT_PAGE_SIZE = 15;

    /** 翻译映射最大每页数量，避免管理接口一次返回过多数据。 */
    public static final int MAX_PAGE_SIZE = 100;

    /** 原文最大长度，对应 translation_mapping.source_text 字段。 */
    public static final int SOURCE_TEXT_MAX_LENGTH = 512;

    /** 译文最大长度，对应 translation_mapping.target_text 字段。 */
    public static final int TARGET_TEXT_MAX_LENGTH = 512;

    /** 源语言最大长度，对应 translation_mapping.source_language 字段。 */
    public static final int SOURCE_LANGUAGE_MAX_LENGTH = 16;

    /** 映射类型最大长度，对应 translation_mapping.mapping_type 字段。 */
    public static final int MAPPING_TYPE_MAX_LENGTH = 32;

    /** 备注最大长度，对应 translation_mapping.remark 字段。 */
    public static final int REMARK_MAX_LENGTH = 512;

    /** 翻译映射参数错误提示。 */
    public static final String PARAM_ERROR_MESSAGE = "翻译映射参数错误";

    /** 翻译映射 ID 为空错误提示。 */
    public static final String ID_EMPTY_ERROR_MESSAGE = "翻译映射 ID 不能为空";

    /** 翻译映射删除 ID 列表为空错误提示。 */
    public static final String IDS_EMPTY_ERROR_MESSAGE = "翻译映射删除 ID 列表不能为空";

    /** 翻译映射原文为空错误提示。 */
    public static final String SOURCE_TEXT_EMPTY_ERROR_MESSAGE = "翻译映射原文不能为空";

    /** 翻译映射译文为空错误提示。 */
    public static final String TARGET_TEXT_EMPTY_ERROR_MESSAGE = "翻译映射译文不能为空";

    /** 翻译映射原文过长错误提示。 */
    public static final String SOURCE_TEXT_TOO_LONG_ERROR_MESSAGE = "翻译映射原文不能超过 512 个字符";

    /** 翻译映射译文过长错误提示。 */
    public static final String TARGET_TEXT_TOO_LONG_ERROR_MESSAGE = "翻译映射译文不能超过 512 个字符";

    /** 翻译映射重复错误提示。 */
    public static final String SOURCE_TEXT_DUPLICATE_ERROR_MESSAGE = "翻译映射原文已存在";

    /** 翻译映射不存在错误提示。 */
    public static final String MAPPING_NOT_FOUND_ERROR_MESSAGE = "翻译映射不存在";

    /** 初始化映射备注：静态默认词表。 */
    public static final String SYSTEM_MAPPING_REMARK = "由系统默认翻译词表初始化";

    /** 初始化映射备注：历史自定义 JSON 词表。 */
    public static final String CUSTOM_MAPPING_REMARK = "由历史自定义翻译词表初始化";

    /** 翻译映射初始化配置开关。 */
    public static final String INIT_ENABLED_PROPERTY = "${dokkan.translation.mapping.init-enabled:true}";

    /** 翻译映射初始化词表配置。 */
    public static final String GLOSSARY_FILE_PROPERTY = "${dokkan.translation.glossary-file:classpath:dokkan-translation-glossary.json}";

    /** 工具类禁止实例化。 */
    private TranslationMappingConstants() {
    }
}
