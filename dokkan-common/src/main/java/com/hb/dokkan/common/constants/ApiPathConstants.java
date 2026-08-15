package com.hb.dokkan.common.constants;

/**
 * @Description API路径常量
 * @Author stargazer
 * @Date 2026/8/15 19:30
 **/
public final class ApiPathConstants {

    /** 数据服务根路径。 */
    public static final String DATA_ROOT = "/data";

    /** 卡片服务根路径。 */
    public static final String CARDS_ROOT = "/cards";

    /** 卡片列表接口路径。 */
    public static final String CARD_LIST = "/list";

    /** 卡片详情接口路径。 */
    public static final String CARD_DETAIL = "/detail";

    /** 初始化卡片数据接口路径。 */
    public static final String INIT_CARD = "/init-card";

    /** 同步 ES 卡片数据接口路径。 */
    public static final String SYNC_ES_CARD = "/sync-es-card";

    /** 按卡片 ID 手动同步接口路径。 */
    public static final String SYNC_CARD_BY_IDS = "/sync-card-by-ids";

    /** 启动后台同步任务接口路径。 */
    public static final String SYNC_START = "/sync-start";

    /** 查询后台同步进度接口路径。 */
    public static final String SYNC_PROGRESS = "/sync-progress/{jobId}";

    /** 普通翻译接口路径。 */
    public static final String TRANSLATE = "/translate";

    /** LLM 翻译接口路径。 */
    public static final String TRANSLATE_LLM = "/translate/llm";

    /** 初始化分类接口路径。 */
    public static final String INIT_CATEGORY = "/init-category";

    /** 初始化链接接口路径。 */
    public static final String INIT_LINK = "/init-link";

    /** 修复数据库数据接口路径。 */
    public static final String FIX_DB_DATA = "/fix-db-data/{fixType}";

    private ApiPathConstants() {
    }
}
