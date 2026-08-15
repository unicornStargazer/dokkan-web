package com.hb.dokkan.config.translation;

/**
 * @Description LLM翻译配置常量
 * @Author stargazer
 * @Date 2026/8/15 19:30
 **/
public final class LlmTranslationConfigConstants {

    /** OpenAI 兼容接口默认聊天补全路径。 */
    public static final String DEFAULT_CHAT_COMPLETIONS_PATH = "/chat/completions";

    /** LLM 翻译默认请求超时时间，单位：秒。 */
    public static final int DEFAULT_TIMEOUT_SECONDS = 60;

    /** LLM 翻译默认温度，较低温度用于保证翻译结果稳定。 */
    public static final double DEFAULT_TEMPERATURE = 0.1D;

    /** LLM 默认 system prompt，约束模型只输出自然简体中文并保留 Dokkan 术语。 */
    public static final String DEFAULT_SYSTEM_PROMPT = "You are a professional localization translator for "
            + "Dragon Ball Z Dokkan Battle. Translate the user's text into natural Simplified Chinese only. "
            + "Do not explain or add quotation marks. Preserve line breaks, placeholders such as "
            + "{passiveImg:up_g}, HTML/Markdown, variable names, numbers, percentages, ATK, DEF, HP, Ki, "
            + "AGL, TEQ, INT, STR, PHY, LR, UR and SSR exactly. Use consistent Dokkan terminology: "
            + "Ki=气力, Super Attack=必杀技, Active Skill=主动技能, Passive Skill=被动技能, "
            + "Leader Skill=队长技能, Category=分类, turn=回合, chance=概率. Keep character names, "
            + "category names and skill names concise and idiomatic in Chinese.";

    /**
     * 工具类禁止实例化。
     */
    private LlmTranslationConfigConstants() {
    }
}
