package com.hb.dokkan.common.constants;

/** JSON 模型额度配置及账期标识常量。 */
public final class LlmQuotaConstants {
    /** 无限额度在内部选择结果中的标识，不代表服务商余额。 */
    public static final long UNLIMITED_TOKENS = Long.MAX_VALUE;
    /** Chat Completions 思考程度字段。 */
    public static final String REASONING_EFFORT = "reasoning_effort";
    /** 新统计口径下成功完成的模型调用数，不继承旧 requestCount。 */
    public static final String SUCCESSFUL_REQUESTS = "successfulRequests";
    /** 新统计口径下失败完成的模型调用数。 */
    public static final String FAILED_REQUESTS = "failedRequests";
    /** 调用成功率百分数，无样本时为 null。 */
    public static final String SUCCESS_RATE_PERCENT = "successRatePercent";
    /** 比率换算百分数。 */
    public static final double PERCENT_MULTIPLIER = 100.0;
    /** 账期和模型 ID 的分隔符；配置标识中禁止包含此字符。 */
    public static final String USAGE_KEY_SEPARATOR = "::";

    /** 模型配置无效时禁止继续选择模型，避免意外使用付费模型。 */
    public static final String INVALID_CATALOG = "Invalid LLM model quota catalog";

    /** 已存在用量文件无法读取时禁止清零继续调用。 */
    public static final String INVALID_USAGE = "Cannot load LLM quota usage";

    /** 请求估算必须为正数且至少覆盖输入，避免绕过额度检查。 */
    public static final String INVALID_ESTIMATE = "Invalid LLM token estimate";

    /** 工具常量类不允许实例化。 */
    private LlmQuotaConstants() {
    }
}
