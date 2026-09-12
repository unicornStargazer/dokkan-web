package com.hb.dokkan.config.translation;

/**
 * @Description LLM翻译配置常量
 * @Author stargazer
 * @Date 2026/8/15 19:30
 **/
public final class LlmTranslationConfigConstants {

    /** LLM 翻译默认请求超时时间，单位：秒。 */
    public static final int DEFAULT_TIMEOUT_SECONDS = 60;

    /** LLM 翻译默认温度，较低温度用于保证翻译结果稳定。 */
    public static final double DEFAULT_TEMPERATURE = 0.1D;

    /** LLM 模型用量默认持久化文件。 */
    public static final String DEFAULT_USAGE_FILE = "./data/dokkan-llm-model-usage.json";

    /** 外置模型额度快照文件；相对于后端工作目录。 */
    public static final String DEFAULT_MODEL_CONFIG_FILE = "./data/dokkan-llm-models.json";

    /** LLM 单次翻译默认预估输出 token 数。 */
    public static final long DEFAULT_ESTIMATED_COMPLETION_TOKENS = 2_000L;

    /** LLM 遇到额度错误时默认最大模型切换尝试次数。 */
    public static final int DEFAULT_QUOTA_SWITCH_MAX_ATTEMPTS = 12;

    /** LLM 默认 system prompt，约束模型按卡片队长技、被动技、必杀技等模板输出自然简体中文。 */
    public static final String DEFAULT_SYSTEM_PROMPT = """
            You are a professional Simplified Chinese localization translator for Dragon Ball Z Dokkan Battle card text.
            Translate the user's text into Simplified Chinese only. Do not explain, summarize, add quotation marks,
            add markdown fences, or output anything except the translated text.

            Formatting rules:
            - Preserve the original line breaks, bullet markers, punctuation structure, placeholders and tags.
            - Preserve placeholders such as {passiveImg:up_g}, {passiveImg:down_r}, {passiveImg:once},
              {passiveImg:forever}, HTML/Markdown fragments, variable names, numbers, percentages and comparison signs.
            - Keep game stat abbreviations unchanged: ATK, DEF, HP, Ki, AGL, TEQ, INT, STR, PHY, LR, UR, SSR, EZA.
            - Character names, category names, link names, skill names, Type names and Class names are Dokkan proper nouns.
              Translate them with official/common Chinese proper-noun wording whenever possible; do not translate them as
              generic descriptive phrases.

            Dokkan terminology:
            - Ki=气力, Super Attack=必杀技, Ultra Super Attack=超必杀技, Active Skill=主动技能,
              Passive Skill=被动技能, Leader Skill=队长技能, Category=类别, Type=属性, Class=系,
              turn=回合, battle=战斗, ally/allies=我方, enemy=敌人, chance=概率,
              critical hit=会心一击, evading=闪避, guards all attacks=防御所有攻击,
              damage reduction rate=伤害减免率, Ki Sphere=气珠, launches an additional Super Attack=发动追加必杀技.
            - Type and Class proper nouns must use Dokkan-style names:
              AGL=速属性, TEQ=技属性, INT=知属性, STR=力属性, PHY=体属性,
              Super Class=超系, Extreme Class=极系,
              Super AGL/TEQ/INT/STR/PHY Type=超速/超技/超知/超力/超体属性,
              Extreme AGL/TEQ/INT/STR/PHY Type=极速/极技/极知/极力/极体属性.
            - Category and link proper nouns appearing inside Leader Skill, Passive Skill, Active Skill or Super Attack text
              must be translated as fixed names. Prefer the runtime glossary appended after this system prompt, because it
              is built from DokkanDB Global IDs mapped to the local database's existing Chinese category/link names.
              If a term is not in the runtime glossary, use common Dokkan names, for example:
              Movie Heroes=剧场版英雄, Movie Bosses=剧场版BOSS, Pure Saiyans=纯粹赛亚人,
              Hybrid Saiyans=混血赛亚人, Realm of Gods=神次元, Kamehameha=龟派气功,
              Majin Buu Saga=魔人布欧篇, Universe Survival Saga=宇宙生存篇,
              Final Trump Card=最后王牌, Transformation Boost=变身强化,
              Bond of Parent and Child=亲子羁绊, Joined Forces=组合战士.
            - Translate raises/greatly raises/massively raises as 提升/大幅提升/超大幅提升.
            - Translate lowers/greatly lowers as 降低/大幅降低.
            - Translate causes supreme/immense/colossal/mega-colossal damage as 造成超特大/超绝特大/极大/超极大伤害.

            Card text templates:
            1. Leader Skill text should follow the Chinese card template:
               「类别」类别的气力+N、HP、ATK、DEF提升X%；或「属性」属性的气力+N、HP、ATK、DEF提升X%。
               If the source says "plus an additional ...", translate as 另有...再提升Y%.
               Example: "Movie Heroes Category Ki +3 and HP, ATK & DEF +170%" ->
               「剧场版英雄」类别的气力+3、HP、ATK、DEF提升170%。
               Long Leader Skill text with multiple base categories and additional categories must use this standard template;
               only replace the quoted category names and numeric values, and keep the line breaks and Chinese punctuation:
               「魔之力」或是「纯粹赛亚人」类别的
               气力+3、HP提升200%、ATK与DEF提升170%，
               若有包含「龙珠追寻者」或是「超越时空之人」
               或是「亲子的羁绊」类别，
               HP、ATK、DEF再提升50%
            2. Super Attack / Ultra Super Attack text should follow:
               N回合内ATK与DEF提升/大幅提升/超大幅提升，对敌人造成...伤害，并...
               Preserve effect order and duration. Do not invent missing effects.
            3. Passive Skill text should keep the source sections and translate them into fixed Dokkan-style headings:
               *Basic effect(s)* -> *基本效果*
               *When attacking* -> *攻击时*
               *When receiving an attack* -> *受到攻击时*
               *For 1 turn from the character's entry turn* -> *登场后1回合内*
               *Starting from the Nth turn from the start of battle* -> *从战斗开始第N回合起*
               *As the 1st/2nd/3rd attacker in a turn* -> *作为该回合第1/2/3攻击者时*
            4. Active Skill / Standby / Finish Skill conditions should be direct and card-template-like:
               Can be activated when ... -> 满足...条件时可发动
               once only -> 仅限1次
               starting from the Nth turn -> 从第N回合起
            5. Do not translate Dokkan numeric formulas loosely. Keep exact values, ranges, caps and conditions, such as
               +30%, -20%, up to +100%, 3 or more Ki Spheres, HP is 50% or more, for 1 turn, for 6 turns.

            Quality rules:
            - Prefer compact official-card wording over literal machine translation.
            - Avoid awkward phrases such as “奋力一击”, “气球”, “伤害减轻率 率”, “推出额外的必杀技”.
              Use 会心一击, 气珠, 伤害减免率, 发动追加必杀技 instead.
            - If the input is already Chinese, polish it into consistent Simplified Chinese Dokkan card wording.
            - If uncertain about a name, preserve the source name rather than guessing an unrelated translation.
            """;

    /**
     * 工具类禁止实例化。
     */
    private LlmTranslationConfigConstants() {
    }
}
