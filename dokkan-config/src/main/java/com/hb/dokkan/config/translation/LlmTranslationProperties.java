package com.hb.dokkan.config.translation;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


/**
 * @Description LLM翻译配置
 * @Author stargazer
 * @Date 2026/8/15 19:00
 **/
@Data
@Component
@ConfigurationProperties(prefix = "dokkan.llm-translation")
public class LlmTranslationProperties {

    /** 是否启用 LLM 翻译，默认关闭以避免未配置密钥时误调用外部接口。 */
    private boolean enabled;

    /** Responses 完整请求地址，包含 /v1/responses，不再拼接任何后缀。 */
    private String baseUrl;

    /** OpenAI 兼容接口 API Key，只允许通过环境变量或本地配置注入。 */
    private String apiKey;

    /** 模型、剩余额度、有效期统一从此 JSON 文件加载，启动时生效。 */
    private String modelConfigFile = LlmTranslationConfigConstants.DEFAULT_MODEL_CONFIG_FILE;

    /** LLM 模型 token 用量状态文件路径。 */
    private String usageFile = LlmTranslationConfigConstants.DEFAULT_USAGE_FILE;

    /** 调用前为模型输出预留的估算 token 数。 */
    private long estimatedCompletionTokens = LlmTranslationConfigConstants.DEFAULT_ESTIMATED_COMPLETION_TOKENS;

    /** 单次翻译遇到额度错误时最多切换尝试的模型数量。 */
    private int quotaSwitchMaxAttempts = LlmTranslationConfigConstants.DEFAULT_QUOTA_SWITCH_MAX_ATTEMPTS;

    /** LLM 请求超时时间，单位：秒。 */
    private int timeoutSeconds = LlmTranslationConfigConstants.DEFAULT_TIMEOUT_SECONDS;

    /** LLM 采样温度，翻译场景默认低温保证稳定。 */
    private double temperature = LlmTranslationConfigConstants.DEFAULT_TEMPERATURE;

    /** LLM system prompt，用于约束 Dokkan 术语、格式和输出语言。 */
    private String systemPrompt = LlmTranslationConfigConstants.DEFAULT_SYSTEM_PROMPT;
}
