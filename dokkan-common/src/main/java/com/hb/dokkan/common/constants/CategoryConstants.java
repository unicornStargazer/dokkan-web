package com.hb.dokkan.common.constants;

/**
 * @Description 分类管理常量
 * @Author stargazer
 * @Date 2026/8/16 00:30
 **/
public final class CategoryConstants {

    /** 分类管理默认页码。 */
    public static final int DEFAULT_PAGE_NUM = 1;

    /** 分类管理默认每页数量。 */
    public static final int DEFAULT_PAGE_SIZE = 15;

    /** 分类管理最大每页数量，避免一次查询过多数据。 */
    public static final int MAX_PAGE_SIZE = 100;

    /** 分类 ID 最小有效值。 */
    public static final long MIN_VALID_CATEGORY_ID = 0L;

    /** 卡片 ID 最小有效值。 */
    public static final long MIN_VALID_CARD_ID = 0L;

    /** 分类中文名称最大长度，与数据库字段长度保持一致。 */
    public static final int CATEGORY_NAME_MAX_LENGTH = 255;

    /** 分类英文名称最大长度，与数据库字段长度保持一致。 */
    public static final int CATEGORY_NAME_EN_MAX_LENGTH = 255;

    /** 分类查询参数错误提示。 */
    public static final String CATEGORY_PARAM_ERROR_MESSAGE = "分类查询参数错误";

    /** 分类保存参数错误提示。 */
    public static final String CATEGORY_SAVE_PARAM_ERROR_MESSAGE = "分类保存参数错误";

    /** 分类 ID 重复错误提示。 */
    public static final String CATEGORY_ID_DUPLICATE_ERROR_MESSAGE = "分类 ID 已存在";

    /** 分类不存在错误提示。 */
    public static final String CATEGORY_NOT_FOUND_ERROR_MESSAGE = "分类不存在";

    /** 卡片重新翻译参数错误提示。 */
    public static final String CARD_RETRANSLATE_PARAM_ERROR_MESSAGE = "卡片重新翻译参数错误";

    /** 工具类禁止实例化。 */
    private CategoryConstants() {
    }
}
