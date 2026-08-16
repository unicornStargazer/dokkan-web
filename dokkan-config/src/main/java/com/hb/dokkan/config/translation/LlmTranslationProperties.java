package com.hb.dokkan.config.translation;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

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

    /** OpenAI 兼容接口 baseUrl，例如 https://api.openai.com/v1。 */
    private String baseUrl;

    /** OpenAI 兼容接口 API Key，只允许通过环境变量或本地配置注入。 */
    private String apiKey;

    /** OpenAI 兼容接口模型名称，兼容旧配置并作为模型池首选模型。 */
    private String model;

    /** OpenAI 兼容接口可切换模型池，按配置顺序参与额度选择。 */
    private List<String> models = LlmTranslationConfigConstants.DEFAULT_MODELS;

    /** 每个免费模型可用 token 额度。 */
    private long freeQuotaTokens = LlmTranslationConfigConstants.DEFAULT_FREE_QUOTA_TOKENS;

    /** LLM 模型 token 用量状态文件路径。 */
    private String usageFile = LlmTranslationConfigConstants.DEFAULT_USAGE_FILE;

    /** 调用前为模型输出预留的估算 token 数。 */
    private long estimatedCompletionTokens = LlmTranslationConfigConstants.DEFAULT_ESTIMATED_COMPLETION_TOKENS;

    /** 单次翻译遇到额度错误时最多切换尝试的模型数量。 */
    private int quotaSwitchMaxAttempts = LlmTranslationConfigConstants.DEFAULT_QUOTA_SWITCH_MAX_ATTEMPTS;

    /** OpenAI 兼容聊天补全接口路径。 */
    private String chatCompletionsPath = LlmTranslationConfigConstants.DEFAULT_CHAT_COMPLETIONS_PATH;

    /** LLM 请求超时时间，单位：秒。 */
    private int timeoutSeconds = LlmTranslationConfigConstants.DEFAULT_TIMEOUT_SECONDS;

    /** LLM 采样温度，翻译场景默认低温保证稳定。 */
    private double temperature = LlmTranslationConfigConstants.DEFAULT_TEMPERATURE;

    /** LLM system prompt，用于约束 Dokkan 术语、格式和输出语言。 */
    private String systemPrompt = LlmTranslationConfigConstants.DEFAULT_SYSTEM_PROMPT;
}
