package com.hb.dokkan.common.constants;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Description 翻译链路常量
 * @Author stargazer
 * @Date 2026/8/15 19:30
 **/
public final class TranslationConstants {

    /** 翻译接口允许的最大文本长度，单位：字符。 */
    public static final int MAX_TRANSLATION_TEXT_LENGTH = 10_000;

    /** 翻译接口参数错误提示，约束文本必须非空且不超过最大长度。 */
    public static final String TRANSLATION_TEXT_ERROR_MESSAGE = "text must contain 1 to 10000 characters";

    /** 普通翻译缓存格式版本，变更缓存结构或核心词表时递增。 */
    public static final int CACHE_FORMAT_VERSION = 4;

    /** 翻译词表哈希基数，保持旧缓存哈希算法稳定。 */
    public static final int GLOSSARY_HASH_BASE = 31;

    /** Google 翻译单批请求最大字符数，避免远程接口拒绝过大的请求。 */
    public static final int GOOGLE_TRANSLATION_MAX_BATCH_CHARS = 4_500;

    /** Google 翻译并发许可数量，控制远程翻译接口并发压力。 */
    public static final int GOOGLE_TRANSLATION_PERMITS = 4;

    /** Google 翻译请求超时时间，单位：秒。 */
    public static final int GOOGLE_TRANSLATION_TIMEOUT_SECONDS = 30;

    /** DokkanDB 术语接口请求超时时间，单位：秒。 */
    public static final int DOKKAN_DB_TERMINOLOGY_TIMEOUT_SECONDS = 30;

    /** Google 翻译接口路径。 */
    public static final String GOOGLE_TRANSLATION_PATH = "/translate_a/single";

    /** Google 翻译客户端类型参数名。 */
    public static final String GOOGLE_TRANSLATION_PARAM_CLIENT = "client";

    /** Google 翻译客户端类型参数值。 */
    public static final String GOOGLE_TRANSLATION_CLIENT_VALUE = "gtx";

    /** Google 翻译源语言参数名。 */
    public static final String GOOGLE_TRANSLATION_PARAM_SOURCE_LANGUAGE = "sl";

    /** Google 翻译目标语言参数名。 */
    public static final String GOOGLE_TRANSLATION_PARAM_TARGET_LANGUAGE = "tl";

    /** Google 翻译返回详情参数名。 */
    public static final String GOOGLE_TRANSLATION_PARAM_DETAIL = "dt";

    /** Google 翻译返回翻译文本详情参数值。 */
    public static final String GOOGLE_TRANSLATION_DETAIL_TEXT_VALUE = "t";

    /** Google 翻译 JSON 返回开关参数名。 */
    public static final String GOOGLE_TRANSLATION_PARAM_JSON = "dj";

    /** Google 翻译 JSON 返回开关参数值。 */
    public static final String GOOGLE_TRANSLATION_JSON_VALUE = "1";

    /** Google 翻译查询文本参数名。 */
    public static final String GOOGLE_TRANSLATION_PARAM_QUERY = "q";

    /** Google 翻译目标语言：简体中文。 */
    public static final String LANGUAGE_ZH_CN = "zh-CN";

    /** Google 翻译源语言：日语。 */
    public static final String LANGUAGE_JA = "ja";

    /** Google 翻译源语言：英语。 */
    public static final String LANGUAGE_EN = "en";

    /** 识别日文假名的正则，用于判断源语言。 */
    public static final String JAPANESE_KANA_REGEX = "[\\p{IsHiragana}\\p{IsKatakana}]";

    /** 保护占位符和常见卡牌稀有度文本的默认正则。 */
    public static final String DEFAULT_PROTECTED_TERM_REGEX = "\\{[^{}]+}|(?i:\\b(?:LR|UR|SSR|HERO|BOSS)\\b)";

    /** 翻译批量行起始标记前缀，用于从合并响应中拆分原始行。 */
    public static final String ROW_MARKER_PREFIX = "__DOKKAN_ROW_";

    /** 翻译批量行起始标记后缀。 */
    public static final String ROW_MARKER_SUFFIX = "__";

    /** 翻译批量末尾标记，用于定位最后一行结束位置。 */
    public static final String ROW_END_MARKER = "__DOKKAN_ROW_END__";

    /** 翻译批量文本行分隔符，固定使用换行以便远程翻译保留行结构。 */
    public static final String BATCH_ROW_SEPARATOR = "\n";

    /** 受保护术语 token 前缀，用于避免远程翻译改写领域术语。 */
    public static final String TOKEN_MARKER_PREFIX = "__DOKKAN_TOKEN_";

    /** 受保护术语 token 后缀。 */
    public static final String TOKEN_MARKER_SUFFIX = "__";

    /** 翻译缓存 JSON 字段：格式版本。 */
    public static final String CACHE_FIELD_VERSION = "version";

    /** 翻译缓存 JSON 字段：词表哈希。 */
    public static final String CACHE_FIELD_GLOSSARY_HASH = "glossaryHash";

    /** 翻译缓存 JSON 字段：翻译缓存明细。 */
    public static final String CACHE_FIELD_TRANSLATIONS = "translations";

    /** 翻译缓存临时文件后缀，用于原子写入前的中间文件。 */
    public static final String CACHE_TEMP_FILE_SUFFIX = ".tmp";

    /** Google 翻译响应 JSON 字段：句子数组。 */
    public static final String GOOGLE_RESPONSE_FIELD_SENTENCES = "sentences";

    /** Google 翻译响应 JSON 字段：翻译文本。 */
    public static final String GOOGLE_RESPONSE_FIELD_TRANS = "trans";

    /** DokkanDB 分类接口路径，用于预热分类术语。 */
    public static final String DOKKAN_DB_CATEGORIES_PATH = "/api/categories";

    /** DokkanDB 链接接口路径，用于预热链接术语。 */
    public static final String DOKKAN_DB_LINKS_PATH = "/api/links";

    /** LLM 请求字段：模型名称。 */
    public static final String LLM_REQUEST_FIELD_MODEL = "model";

    /** LLM 请求字段：温度。 */
    public static final String LLM_REQUEST_FIELD_TEMPERATURE = "temperature";

    /** LLM 请求字段：消息列表。 */
    public static final String LLM_REQUEST_FIELD_MESSAGES = "messages";

    /** LLM 请求字段：消息角色。 */
    public static final String LLM_REQUEST_FIELD_ROLE = "role";

    /** LLM 请求字段：消息内容。 */
    public static final String LLM_REQUEST_FIELD_CONTENT = "content";

    /** LLM system 消息角色值。 */
    public static final String LLM_ROLE_SYSTEM = "system";

    /** LLM user 消息角色值。 */
    public static final String LLM_ROLE_USER = "user";

    /** LLM HTTP 授权头 Bearer 前缀，后接 API Key。 */
    public static final String LLM_AUTHORIZATION_BEARER_PREFIX = "Bearer ";

    /** LLM 响应 JSON 字段：候选列表。 */
    public static final String LLM_RESPONSE_FIELD_CHOICES = "choices";

    /** LLM 响应 JSON 字段：消息对象。 */
    public static final String LLM_RESPONSE_FIELD_MESSAGE = "message";

    /** LLM 响应 JSON 字段：token 用量对象。 */
    public static final String LLM_RESPONSE_FIELD_USAGE = "usage";

    /** LLM 响应 usage 字段：输入 prompt token 数。 */
    public static final String LLM_RESPONSE_FIELD_PROMPT_TOKENS = "prompt_tokens";

    /** LLM 响应 usage 字段：输出 completion token 数。 */
    public static final String LLM_RESPONSE_FIELD_COMPLETION_TOKENS = "completion_tokens";

    /** LLM 响应 usage 字段：总 token 数。 */
    public static final String LLM_RESPONSE_FIELD_TOTAL_TOKENS = "total_tokens";

    /** LLM 模型用量缓存 JSON 字段：模型用量明细。 */
    public static final String LLM_USAGE_FIELD_MODELS = "models";

    /** LLM 模型用量缓存 JSON 字段：已消耗 token 数。 */
    public static final String LLM_USAGE_FIELD_USED_TOKENS = "usedTokens";

    /** LLM 模型用量缓存 JSON 字段：累计 prompt token 数。 */
    public static final String LLM_USAGE_FIELD_PROMPT_TOKENS = "promptTokens";

    /** LLM 模型用量缓存 JSON 字段：累计 completion token 数。 */
    public static final String LLM_USAGE_FIELD_COMPLETION_TOKENS = "completionTokens";

    /** LLM 模型用量缓存 JSON 字段：累计请求次数。 */
    public static final String LLM_USAGE_FIELD_REQUEST_COUNT = "requestCount";

    /** LLM 模型用量缓存 JSON 字段：最近使用时间。 */
    public static final String LLM_USAGE_FIELD_LAST_USED_AT = "lastUsedAt";

    /** LLM 模型用量缓存 JSON 字段：额度耗尽标记。 */
    public static final String LLM_USAGE_FIELD_QUOTA_EXHAUSTED = "quotaExhausted";

    /** LLM 模型用量缓存 JSON 字段：额度耗尽原因。 */
    public static final String LLM_USAGE_FIELD_QUOTA_EXHAUSTED_REASON = "quotaExhaustedReason";

    /** LLM 翻译未启用时的错误提示。 */
    public static final String LLM_DISABLED_ERROR_MESSAGE = "LLM translation is disabled";

    /** LLM 翻译缺少必要配置时的错误提示。 */
    public static final String LLM_CONFIG_ERROR_MESSAGE = "LLM translation requires base-url, api-key and model configuration";

    /** LLM 响应缺少内容字段时的错误提示。 */
    public static final String LLM_EMPTY_CONTENT_ERROR_MESSAGE = "LLM response does not contain choices[0].message.content";

    /** LLM 请求失败时的错误提示前缀。 */
    public static final String LLM_REQUEST_FAILED_MESSAGE_PREFIX = "LLM translation request failed: ";

    /** LLM 模型额度不足时的错误提示前缀。 */
    public static final String LLM_QUOTA_EXCEEDED_MESSAGE_PREFIX = "LLM model quota exceeded: ";

    /** LLM 所有模型额度不足时的错误提示。 */
    public static final String LLM_ALL_MODELS_QUOTA_EXHAUSTED_ERROR_MESSAGE = "All configured LLM free model quotas are exhausted";

    /** LLM 模型池未配置时的错误提示。 */
    public static final String LLM_MODEL_POOL_EMPTY_ERROR_MESSAGE = "LLM translation requires at least one model";

    /** LLM 响应缺少 usage 时的日志提示。 */
    public static final String LLM_USAGE_MISSING_WARNING_MESSAGE = "LLM response usage missing, estimated tokens will be recorded";

    /** LLM 默认限额错误原因。 */
    public static final String LLM_QUOTA_EXHAUSTED_DEFAULT_REASON = "quota exceeded";

    /** LLM 限额错误关键字：quota。 */
    public static final String LLM_QUOTA_ERROR_KEYWORD_QUOTA = "quota";

    /** LLM 限额错误关键字：insufficient。 */
    public static final String LLM_QUOTA_ERROR_KEYWORD_INSUFFICIENT = "insufficient";

    /** LLM 限额错误关键字：rate_limit。 */
    public static final String LLM_QUOTA_ERROR_KEYWORD_RATE_LIMIT = "rate_limit";

    /** LLM 限额错误关键字：余额。 */
    public static final String LLM_QUOTA_ERROR_KEYWORD_BALANCE_CN = "余额";

    /** LLM 限额错误关键字：额度。 */
    public static final String LLM_QUOTA_ERROR_KEYWORD_QUOTA_CN = "额度";

    /** LLM system prompt 与动态术语表之间的分隔符。 */
    public static final String LLM_PROMPT_SECTION_SEPARATOR = "\n\n";

    /** LLM 动态术语表标题，提示模型优先使用本地已有映射。 */
    public static final String LLM_DYNAMIC_GLOSSARY_TITLE = "Runtime Dokkan proper-noun glossary. Use these exact translations when they appear in Leader Skill, Passive Skill, Active Skill, Super Attack, category, link or condition text:";

    /** LLM 动态术语表条目格式。 */
    public static final String LLM_DYNAMIC_GLOSSARY_ENTRY_FORMAT = "- %s = %s";

    /** LLM 动态术语表最大条目数，避免 prompt 过长影响接口稳定性。 */
    public static final int LLM_DYNAMIC_GLOSSARY_MAX_TERMS = 300;

    /** LLM 翻译最小并发许可数，避免配置异常时没有可用并发。 */
    public static final int LLM_MIN_CONCURRENCY_PERMITS = 1;

    /** LLM 翻译最大并发许可数，避免批量重翻时压垮外部模型服务。 */
    public static final int LLM_MAX_CONCURRENCY_PERMITS = 5;

    /** LLM 并发等待被中断时的错误提示。 */
    public static final String LLM_CONCURRENCY_INTERRUPTED_ERROR_MESSAGE = "LLM translation concurrency interrupted";

    /** LLM token 估算：英文字符折算 token 的字符数。 */
    public static final int LLM_TOKEN_ESTIMATE_ENGLISH_CHARS_PER_TOKEN = 4;

    /** LLM token 估算：最小估算 token 数，避免极短文本被低估。 */
    public static final long LLM_TOKEN_ESTIMATE_MIN_TOKENS = 1L;

    /** LLM 模型用量缓存临时文件后缀。 */
    public static final String LLM_USAGE_TEMP_FILE_SUFFIX = ".tmp";

    /** 卡片文本归一化正则：忽略大小写的 Ki 英文词。 */
    public static final String NORMALIZE_REGEX_KI_WORD = "(?i)\\bKi\\b";

    /** 卡片文本归一化正则：行首问号转项目符号。 */
    public static final String NORMALIZE_REGEX_LINE_START_QUESTION = "(?m)^\\?";

    /** 卡片文本归一化正则：阿拉伯数字后的“转”应为“回合”。 */
    public static final String NORMALIZE_REGEX_TURN_SUFFIX = "(\\d+)转";

    /** 卡片文本归一化正则：带空格或“个”的回合表达式。 */
    public static final String NORMALIZE_REGEX_TURN_WITH_SPACES = "(\\d+)[ \\t]*个?[ \\t]*回合";

    /** 卡片文本归一化正则：ATK 与 DEF 的连接表达式。 */
    public static final String NORMALIZE_REGEX_ATK_DEF_CONNECTOR = "ATK[ \\t]*(?:和|与|&)[ \\t]*DEF";

    /** 卡片文本归一化正则：HP、ATK、DEF 三维属性表达式。 */
    public static final String NORMALIZE_REGEX_HP_ATK_DEF = "HP[ \\t]*[、,，][ \\t]*ATK与DEF";

    /** 卡片文本归一化正则：中文标点前多余空白。 */
    public static final String NORMALIZE_REGEX_SPACE_BEFORE_PUNCTUATION = "[ \\t]+([，。；：、%])";

    /** 卡片文本归一化替换值：回合表达式。 */
    public static final String NORMALIZE_REPLACEMENT_TURN = "$1回合";

    /** 卡片文本归一化替换值：ATK 与 DEF。 */
    public static final String NORMALIZE_REPLACEMENT_ATK_DEF = "ATK与DEF";

    /** 卡片文本归一化替换值：HP、ATK、DEF。 */
    public static final String NORMALIZE_REPLACEMENT_HP_ATK_DEF = "HP、ATK、DEF";

    /** 卡片文本归一化替换值：项目符号。 */
    public static final String NORMALIZE_REPLACEMENT_BULLET = "・";

    /** 卡片文本归一化替换值：气力术语。 */
    public static final String NORMALIZE_REPLACEMENT_KI = "气力";

    /** 卡片文本归一化替换值：保留正则首个捕获分组。 */
    public static final String NORMALIZE_REPLACEMENT_FIRST_GROUP = "$1";

    /** 常用卡牌属性：HP。 */
    public static final String TERM_HP = "HP";

    /** 常用卡牌属性：ATK。 */
    public static final String TERM_ATK = "ATK";

    /** 常用卡牌属性：DEF。 */
    public static final String TERM_DEF = "DEF";

    /** 卡片文本归一化固定替换规则，按插入顺序执行以保证术语覆盖稳定。 */
    public static final Map<String, String> NORMALIZE_TEXT_REPLACEMENTS = buildNormalizeTextReplacements();

    /** 静态翻译术语表，保护 Dokkan 领域词汇和角色名不被远程翻译误改。 */
    public static final Map<String, String> DEFAULT_TERM_GLOSSARY = buildDefaultTermGlossary();

    /** JSON 空字符串兜底值。 */
    public static final String EMPTY_TEXT = "";

    /** URL 路径斜杠，用于规范化外部接口 baseUrl。 */
    public static final String URL_SLASH = "/";

    /** 正则分支分隔符。 */
    public static final String REGEX_OR_SEPARATOR = "|";

    /** 卡片转换标签：Dokkan Festival。 */
    public static final String CARD_TAG_DOKKAN_FESTIVAL = "Dokkan Festival";

    /** 卡片转换标签：Carnival。 */
    public static final String CARD_TAG_CARNIVAL = "Carnival";

    /** 卡片转换标签：Summonable。 */
    public static final String CARD_TAG_SUMMONABLE = "Summonable";

    /** Standby 技能标签，用于持久化区分技能类型。 */
    public static final String SKILL_LABEL_STANDBY = "standby";

    /** Finish 技能标签，用于持久化区分技能类型。 */
    public static final String SKILL_LABEL_FINISH = "finish";

    /** 特殊攻击默认起始等级。 */
    public static final int SPECIAL_ATTACK_LEVEL_START = 1;

    /** 卡片转换类型值，沿用现有 wiki 转换模型约定。 */
    public static final int WIKI_TRANSFORMATION_TYPE = 103;

    /** EZA 多阶段拆分阈值，达到该阶段时额外保留 pre 阶段信息。 */
    public static final int EZA_PRE_STEP_THRESHOLD = 8;

    /** 高稀有度卡片默认 cost 的稀有度阈值。 */
    public static final int HIGH_RARITY_COST_THRESHOLD = 5;

    /** 高稀有度卡片默认 cost。 */
    public static final int HIGH_RARITY_DEFAULT_COST = 77;

    /** Dokkan Festival 卡片默认 cost。 */
    public static final int DOKKAN_FESTIVAL_DEFAULT_COST = 58;

    /** 普通常驻池卡片默认 cost。 */
    public static final int SUMMONABLE_DEFAULT_COST = 42;

    /** 免费卡片默认 cost。 */
    public static final int FREE_CARD_DEFAULT_COST = 32;

    /**
     * 构建卡片翻译归一化固定替换规则。
     *
     * @return 不可变替换规则 Map
     */
    private static Map<String, String> buildNormalizeTextReplacements() {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("攻击力", TERM_ATK);
        values.put("防御力", TERM_DEF);
        values.put("生命值", TERM_HP);
        values.put("超高概率", "超高机率");
        values.put("高概率", "高机率");
        values.put("中概率", "中等机率");
        values.put("几率", "机率");
        values.put("概率", "机率");
        values.put("大幅度提升", "大幅提升");
        values.put("闪避", "回避");
        values.put("奋力一击", "会心一击");
        values.put("暴击", "会心一击");
        values.put("伤害减少率", "伤害减免率");
        values.put("伤害减轻率", "伤害减免率");
        values.put("防范一切攻击", "防御所有攻击");
        values.put("类别类别", "类别");
        return Collections.unmodifiableMap(values);
    }

    /**
     * 构建默认术语表。
     *
     * @return 不可变术语表 Map
     */
    private static Map<String, String> buildDefaultTermGlossary() {
        Map<String, String> terms = new LinkedHashMap<>();
        terms.put("ダメージ軽減率", "伤害减免率");
        terms.put("超高確率", "超高机率");
        terms.put("高確率", "高机率");
        terms.put("中確率", "中等机率");
        terms.put("必殺技", "必杀技");
        terms.put("超必殺技", "超必杀技");
        terms.put("アクティブスキル", "主动技能");
        terms.put("パッシブスキル", "被动技能");
        terms.put("リーダースキル", "队长技");
        terms.put("サイヤ人", "赛亚人");
        terms.put("人造人間", "人造人");
        terms.put("魔人ブウ", "魔人布欧");
        terms.put("ベジータ", "贝吉塔");
        terms.put("フリーザ", "弗利萨");
        terms.put("ピッコロ", "比克");
        terms.put("トランクス", "特兰克斯");
        terms.put("クリリン", "克林");
        terms.put("孫悟空", "孙悟空");
        terms.put("孫悟飯", "孙悟饭");
        terms.put("超サイヤ人", "超级赛亚人");
        terms.put("気力", "气力");
        terms.put("気玉", "气珠");
        terms.put("虹気玉", "彩虹珠");
        terms.put("属性気玉", "属性气珠");
        terms.put("カテゴリ", "类别");
        terms.put("Category", "类别");
        terms.put("Type", "属性");
        terms.put("Super Class", "超系");
        terms.put("Extreme Class", "极系");
        terms.put("Super AGL Type", "超速属性");
        terms.put("Super TEQ Type", "超技属性");
        terms.put("Super INT Type", "超知属性");
        terms.put("Super STR Type", "超力属性");
        terms.put("Super PHY Type", "超体属性");
        terms.put("Extreme AGL Type", "极速属性");
        terms.put("Extreme TEQ Type", "极技属性");
        terms.put("Extreme INT Type", "极知属性");
        terms.put("Extreme STR Type", "极力属性");
        terms.put("Extreme PHY Type", "极体属性");
        terms.put("AGL Type", "速属性");
        terms.put("TEQ Type", "技属性");
        terms.put("INT Type", "知属性");
        terms.put("STR Type", "力属性");
        terms.put("PHY Type", "体属性");
        terms.put("Movie Heroes", "剧场版英雄");
        terms.put("Movie Bosses", "剧场版BOSS");
        terms.put("Pure Saiyans", "纯粹赛亚人");
        terms.put("Hybrid Saiyans", "混血赛亚人");
        terms.put("Realm of Gods", "神次元");
        terms.put("Kamehameha", "龟派气功");
        terms.put("Majin Buu Saga", "魔人布欧篇");
        terms.put("Universe Survival Saga", "宇宙生存篇");
        terms.put("Final Trump Card", "最后王牌");
        terms.put("Transformation Boost", "变身强化");
        terms.put("Bond of Parent and Child", "亲子羁绊");
        terms.put("Joined Forces", "组合战士");
        terms.put("Power of Wishes", "愿望之力");
        terms.put("Battle of Fate", "命运之战");
        terms.put("Earth-Bred Fighters", "地球培育的战士");
        terms.put("Super Heroes", "超级英雄");
        terms.put("Super Bosses", "超级BOSS");
        terms.put("ターン", "回合");
        terms.put("味方全員", "我方全体");
        terms.put("自身", "自身");
        terms.put("敵", "敌人");
        terms.put("回避率", "回避率");
        terms.put("会心の一撃", "会心一击");
        terms.put("critical hit", "会心一击");
        terms.put("critical hits", "会心一击");
        terms.put("damage reduction", "伤害减免率");
        terms.put("damage reduction rate", "伤害减免率");
        terms.put("必ず追加攻撃", "必可发动追加攻击");
        terms.put("全属性に効果抜群で攻撃", "对全属性造成属性克制伤害");
        terms.put("全ての攻撃をガード", "防御所有攻击");
        terms.put("必殺技を封じる", "封锁必杀技");
        terms.put("気絶させる", "使其晕眩");
        terms.put("極系", "极系");
        terms.put("超系", "超系");
        return Collections.unmodifiableMap(terms);
    }

    /**
     * 工具类禁止实例化。
     */
    private TranslationConstants() {
    }
}
