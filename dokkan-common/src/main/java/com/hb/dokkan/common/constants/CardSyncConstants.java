package com.hb.dokkan.common.constants;

/**
 * @Description 卡片同步常量
 * @Author stargazer
 * @Date 2026/8/15 21:10
 **/
public final class CardSyncConstants {

    /** 手动同步最多允许的卡片数量，避免一次请求触发过大同步任务。 */
    public static final int MANUAL_SYNC_MAX_CARD_COUNT = 100;

    /** 同步进度默认总量，用于百分比型同步任务。 */
    public static final int SYNC_PROGRESS_TOTAL_PERCENT = 100;

    /** 同步进度：任务刚开始。 */
    public static final int SYNC_PROGRESS_START = 5;

    /** 同步进度：准备抓取外部数据。 */
    public static final int SYNC_PROGRESS_FETCH_START = 10;

    /** 同步进度：手动同步准备阶段。 */
    public static final int SYNC_PROGRESS_MANUAL_FETCH = 10;

    /** 同步进度：正在抓取指定卡片。 */
    public static final int SYNC_PROGRESS_MANUAL_FETCH_RUNNING = 12;

    /** 同步进度：指定卡片数据整理阶段。 */
    public static final int SYNC_PROGRESS_MANUAL_ARRANGE_CARD = 48;

    /** 同步进度：ES 索引准备阶段。 */
    public static final int SYNC_PROGRESS_ES_PREPARE = 12;

    /** 同步进度：ES 同步读取 MySQL 关联数据完成。 */
    public static final int SYNC_PROGRESS_ES_READ_COMPLETE = 45;

    /** 同步进度：ES 文档构建完成。 */
    public static final int SYNC_PROGRESS_ES_DOC_BUILD = 72;

    /** 同步进度：异步同步准备 ES 写入阶段。 */
    public static final int SYNC_PROGRESS_ES_WRITE = 90;

    /** 同步进度：外部卡片抓取完成后的整理阶段。 */
    public static final int SYNC_PROGRESS_ARRANGE_CARD = 62;

    /** 同步进度：MySQL 写入阶段。 */
    public static final int SYNC_PROGRESS_MYSQL_WRITE = 65;

    /** 同步进度：ES 同步阶段。 */
    public static final int SYNC_PROGRESS_ES_SYNC = 82;

    /** 同步进度：头像同步阶段。 */
    public static final int SYNC_PROGRESS_ICON_SYNC = 96;

    /** 同步进度：完成前校验阶段。 */
    public static final int SYNC_PROGRESS_FINAL_CHECK = 98;

    /** 同步类型：MySQL 增量同步。 */
    public static final String SYNC_TYPE_MYSQL = "mysql";

    /** 同步类型：ES 全量同步。 */
    public static final String SYNC_TYPE_ES = "es";

    /** 同步类型：指定卡片手动同步。 */
    public static final String SYNC_TYPE_MANUAL = "manual";

    /** 同步任务状态：等待执行。 */
    public static final String SYNC_STATUS_PENDING = "PENDING";

    /** 同步任务状态：执行中。 */
    public static final String SYNC_STATUS_RUNNING = "RUNNING";

    /** 同步任务状态：执行成功。 */
    public static final String SYNC_STATUS_SUCCESS = "SUCCESS";

    /** 同步任务状态：执行失败。 */
    public static final String SYNC_STATUS_FAILED = "FAILED";

    /** 同步类型错误提示。 */
    public static final String SYNC_TYPE_ERROR_MESSAGE = "sync type must be mysql, es or manual";

    /** 手动同步参数错误提示。 */
    public static final String MANUAL_SYNC_ERROR_MESSAGE = "manual sync requires 1-100 cardIds";

    /** 查询数据库数据失败提示。 */
    public static final String QUERY_DB_DATA_ERROR_MESSAGE = "query db data error";

    /** 保存卡片头像失败提示。 */
    public static final String SAVE_CARD_ICON_ERROR_MESSAGE = "save card icon failed";

    /** 卡片同步批量保存大小，适用于基础卡片数据。 */
    public static final int CARD_SAVE_BATCH_SIZE = 500;

    /** 卡片同步批量保存大小，适用于必杀技数据。 */
    public static final int SPECIAL_SAVE_BATCH_SIZE = 1_000;

    /** 卡片 ID 必须大于该值。 */
    public static final long MIN_VALID_CARD_ID = 0L;

    /** DokkanDB 请求超时时间，单位：秒。 */
    public static final int DOKKAN_DB_REQUEST_TIMEOUT_SECONDS = 30;

    /** DokkanDB 最近卡片目录接口路径。 */
    public static final String DOKKAN_DB_CATALOG_PATH = "/api/cards-catalog-with-transformations";

    /** DokkanDB 单卡接口路径。 */
    public static final String DOKKAN_DB_CARD_PATH = "/api/card";

    /** DokkanDB 卡片数值接口路径。 */
    public static final String DOKKAN_DB_CARD_STATS_PATH = "/api/card-stats-with-hp";

    /** DokkanDB 分类接口路径。 */
    public static final String DOKKAN_DB_CATEGORIES_PATH = "/api/categories";

    /** DokkanDB 链接接口路径。 */
    public static final String DOKKAN_DB_LINKS_PATH = "/api/links";

    /** DokkanDB 链接效果接口路径。 */
    public static final String DOKKAN_DB_LINK_EFFECTS_PATH = "/api/link-skill-effects-by-ids";

    /** DokkanDB 目录分页参数名。 */
    public static final String DOKKAN_DB_QUERY_CHUNK = "chunk";

    /** DokkanDB 目录分页大小参数名。 */
    public static final String DOKKAN_DB_QUERY_CHUNK_SIZE = "chunk_size";

    /** DokkanDB 单卡 ID 参数名。 */
    public static final String DOKKAN_DB_QUERY_CODE = "code";

    /** DokkanDB 卡片数值 ID 参数名。 */
    public static final String DOKKAN_DB_QUERY_CARD_ID = "p_card_id";

    /** DokkanDB 多 ID 查询参数名。 */
    public static final String DOKKAN_DB_QUERY_IDS = "ids";

    /** DokkanDB 目录默认首个分片。 */
    public static final int DOKKAN_DB_DEFAULT_CHUNK = 1;

    /** DokkanDB 卡片目录同步分片大小。 */
    public static final int DOKKAN_DB_CATALOG_CHUNK_SIZE = 500;

    /** DokkanDB 卡片目录最多扫描分片数，避免外部接口异常导致无限轮询。 */
    public static final int DOKKAN_DB_CATALOG_MAX_CHUNK = 20;

    /** 多 ID 查询分隔符。 */
    public static final String ID_JOIN_SEPARATOR = ",";

    /** 每个链接需要翻译的文本数量：名称、1级描述、10级描述。 */
    public static final int LINK_TRANSLATION_TEXT_COUNT = 3;

    /** 链接名称在翻译文本组中的偏移。 */
    public static final int LINK_NAME_TEXT_OFFSET = 0;

    /** 链接1级描述在翻译文本组中的偏移。 */
    public static final int LINK_LEVEL_1_TEXT_OFFSET = 1;

    /** 链接10级描述在翻译文本组中的偏移。 */
    public static final int LINK_LEVEL_10_TEXT_OFFSET = 2;

    /** Wiki 链接、分类展示名称分隔符。 */
    public static final String DISPLAY_NAME_SEPARATOR = ",";

    /** 同步阶段：等待执行。 */
    public static final String STAGE_WAITING = "等待执行";

    /** 同步阶段：准备同步。 */
    public static final String STAGE_PREPARE_SYNC = "准备同步";

    /** 同步阶段：抓取外部数据。 */
    public static final String STAGE_FETCH_EXTERNAL = "抓取外部数据";

    /** 同步阶段：读取 MySQL。 */
    public static final String STAGE_READ_MYSQL = "读取 MySQL";

    /** 同步阶段：写入 ES。 */
    public static final String STAGE_WRITE_ES = "写入 ES";

    /** 同步阶段：抓取手动同步卡片。 */
    public static final String STAGE_FETCH_MANUAL_CARD = "抓取手动同步卡片";

    /** 同步阶段：写入完成。 */
    public static final String STAGE_WRITE_COMPLETED = "写入完成";

    /** 同步阶段：完成前校验。 */
    public static final String STAGE_FINAL_CHECK = "完成前校验";

    /** 同步阶段：任务完成。 */
    public static final String STAGE_COMPLETED = "任务完成";

    /** 同步阶段：同步失败。 */
    public static final String STAGE_SYNC_FAILED = "同步失败";

    /** 同步阶段：整理卡片数据。 */
    public static final String STAGE_ARRANGE_CARD = "整理卡片数据";

    /** 同步阶段：写入 MySQL。 */
    public static final String STAGE_WRITE_MYSQL = "写入 MySQL";

    /** 同步阶段：同步 Elasticsearch。 */
    public static final String STAGE_SYNC_ES = "同步 Elasticsearch";

    /** 同步阶段：同步头像。 */
    public static final String STAGE_SYNC_ICON = "同步头像";

    /** 同步阶段：抓取指定卡片。 */
    public static final String STAGE_FETCH_MANUAL_CARD_RUNNING = "抓取指定卡片";

    /** 同步阶段：准备 ES 索引。 */
    public static final String STAGE_PREPARE_ES_INDEX = "准备 ES 索引";

    /** 同步阶段：构建 ES 文档。 */
    public static final String STAGE_BUILD_ES_DOCUMENT = "构建 ES 文档";

    /** 同步消息：任务已创建。 */
    public static final String MESSAGE_TASK_CREATED = "同步任务已创建";

    /** 同步消息：初始化同步任务。 */
    public static final String MESSAGE_INIT_SYNC_TASK = "正在初始化同步任务";

    /** 同步消息：拉取 DokkanDB 数据。 */
    public static final String MESSAGE_FETCH_DOKKAN_DB = "正在拉取 DokkanDB 数据";

    /** 同步消息：完成前校验。 */
    public static final String MESSAGE_FINAL_CHECK = "正在执行完成前校验";

    /** 同步消息：读取卡片数据。 */
    public static final String MESSAGE_READ_CARD_DATA = "正在读取卡片数据";

    /** 同步消息：提交 ES 数据。 */
    public static final String MESSAGE_ES_SUBMITTING = "正在提交 ES 数据";

    /** 同步消息：同步成功。 */
    public static final String MESSAGE_SYNC_SUCCESS = "同步成功";

    /** 同步消息：同步失败。 */
    public static final String MESSAGE_SYNC_FAILED = "同步失败";

    /** 同步消息：手动同步抓取前缀。 */
    public static final String MESSAGE_FETCH_MANUAL_CARD_PREFIX = "正在抓取 ";

    /** 同步消息：卡片 ID 后缀。 */
    public static final String MESSAGE_CARD_ID_SUFFIX = " 个 cardId";

    /** 同步消息：已同步数量前缀。 */
    public static final String MESSAGE_SYNCED_PREFIX = "已同步 ";

    /** 同步消息：卡片数量后缀。 */
    public static final String MESSAGE_CARD_COUNT_SUFFIX = " 个卡片";

    /** 同步消息：已获取数量前缀。 */
    public static final String MESSAGE_FETCHED_PREFIX = "已获取 ";

    /** 同步消息：有效卡片数量后缀。 */
    public static final String MESSAGE_VALID_CARD_SUFFIX = " 个有效卡片";

    /** 同步消息：外部数据抓取完成。 */
    public static final String MESSAGE_FETCH_COMPLETE_ARRANGE = "外部数据抓取完成，正在去重并转换";

    /** 同步消息：正在写入卡片数据。 */
    public static final String MESSAGE_WRITE_CARD_DATA = "正在写入卡片基础数据及关联技能";

    /** 同步消息：正在更新 ES 文档。 */
    public static final String MESSAGE_UPDATE_ES_DOCUMENT = "MySQL 写入完成，正在更新 ES 文档";

    /** 同步消息：头像同步完成。 */
    public static final String MESSAGE_ICON_SYNC_HANDLED = "ES 更新完成，头像下载与对象存储已处理";

    /** 同步消息：ES 索引准备。 */
    public static final String MESSAGE_PREPARE_ES_INDEX = "正在检查并创建卡片索引";

    /** 同步消息：读取 MySQL 关联数据完成前缀。 */
    public static final String MESSAGE_MYSQL_READ_COMPLETE_PREFIX = "关联数据读取完成，共 ";

    /** 同步消息：卡片数量后缀。 */
    public static final String MESSAGE_CARD_COUNT_UNIT_SUFFIX = " 张卡片";

    /** 同步消息：构建 ES 文档完成前缀。 */
    public static final String MESSAGE_ES_DOCUMENT_BUILT_PREFIX = "已构建 ";

    /** 同步消息：ES 文档数量后缀。 */
    public static final String MESSAGE_ES_DOCUMENT_COUNT_SUFFIX = " 个卡片文档";

    /** 卡片头像源站 ID 和本地 cardId 的偏移。 */
    public static final long DOKKAN_DB_CARD_ID_OFFSET = 1L;

    /** 卡片头像源站 URL 模板。 */
    public static final String CARD_ICON_SOURCE_URL_TEMPLATE = "https://enaskhebnjtktdfszdcb.supabase.co/storage/v1/"
            + "object/public/assets/character/thumb/card_%d_thumb_folder/card_%d_thumb.png";

    /** 卡片头像 HTTP User-Agent。 */
    public static final String CARD_ICON_USER_AGENT = "dokkan-web/1.0";

    /** 卡片头像对象存储内容类型。 */
    public static final String CARD_ICON_CONTENT_TYPE = "image/png";

    /** 卡片头像对象存储业务目录。 */
    public static final String CARD_ICON_DB_DIRECTORY = "db";

    /** 卡片头像文件扩展名。 */
    public static final String CARD_ICON_FILE_SUFFIX = ".png";

    /** URL 路径分隔符。 */
    public static final String PATH_SEPARATOR = "/";

    /** HTTP 协议前缀。 */
    public static final String HTTP_PROTOCOL_PREFIX = "http://";

    /** HTTPS 协议前缀。 */
    public static final String HTTPS_PROTOCOL_PREFIX = "https://";

    /** MinIO 对象不存在错误码。 */
    public static final String MINIO_NO_SUCH_KEY_CODE = "NoSuchKey";

    /** MinIO 公开读策略模板，按 bucket 动态填充。 */
    public static final String MINIO_PUBLIC_READ_POLICY_TEMPLATE = """
            {
              "Version": "2012-10-17",
              "Statement": [
                {
                  "Effect": "Allow",
                  "Principal": "*",
                  "Action": ["s3:GetObject"],
                  "Resource": ["arn:aws:s3:::%s/*"]
                }
              ]
            }
            """;

    /** EZA 数据数量超过该阈值时标记为 Super EZA。 */
    public static final int SUPER_EZA_COUNT_THRESHOLD = 1;

    /** EZA 普通阶段列表下标。 */
    public static final int EZA_BASE_INDEX = 0;

    /** 卡片属性 key 连接符，用于构造复合 key。 */
    public static final String KEY_SEPARATOR = ":";

    /** 空字符串兜底值。 */
    public static final String EMPTY_TEXT = "";

    /**
     * 工具类禁止实例化。
     */
    private CardSyncConstants() {
    }
}
