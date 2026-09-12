package com.hb.dokkan.service.translation;

import com.hb.dokkan.common.constants.TranslationConstants;
import com.hb.dokkan.common.constants.LlmQuotaConstants;
import com.hb.dokkan.common.domain.dto.translation.LlmModelCatalogDTO;
import com.hb.dokkan.common.domain.dto.translation.LlmTokenUsageDTO;
import com.hb.dokkan.common.utils.DateUtils;
import com.hb.dokkan.common.utils.JsonUtils;
import com.hb.dokkan.config.translation.LlmTranslationProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @Description LLM模型额度服务
 * @Author stargazer
 * @Date 2026/8/16 00:00
 **/
@Slf4j
@Service
public class DokkanLlmModelQuotaService {

    /** LLM 模型用量缓存，key 为模型名称。 */
    private final Map<String, ModelUsage> modelUsageMap = new ConcurrentHashMap<>();

    /** 模型选择与用量持久化锁，避免批量并发翻译时覆盖用量文件。 */
    private final Object usageLock = new Object();

    /** 已校验的模型额度快照，按 JSON 顺序保存。 */
    private final Map<String, LlmModelCatalogDTO.ModelQuota> modelQuotas = new LinkedHashMap<>();

    /** 预解析的最后有效日期。 */
    private final Map<String, LocalDate> expiryDates = new LinkedHashMap<>();

    /** 当前进程在途请求预占 token，避免并发重复使用相同余额。 */
    private final Map<String, Long> reservedTokens = new LinkedHashMap<>();

    /** 配置快照标识，与旧模型用量隔离。 */
    private String snapshotId;

    /** 配置中的日期时区。 */
    private ZoneId quotaZone;

    /** 到期判断时钟，可在测试中固定。 */
    private Clock clock = Clock.systemUTC();

    @Resource
    private LlmTranslationProperties properties;

    /**
     * 初始化模型用量状态。
     */
    @PostConstruct
    public void initialize() {
        if (!properties.isEnabled()) {
            return;
        }
        loadModelCatalog();
        loadUsage();
    }

    /**
     * 读取并严格校验模型快照；缺失、重复模型或非法日期禁止启用翻译。
     */
    private void loadModelCatalog() {
        try {
            LlmModelCatalogDTO catalog = JsonUtils.file2Object(
                    Path.of(properties.getModelConfigFile()), LlmModelCatalogDTO.class);
            if (Objects.isNull(catalog) || StringUtils.isBlank(catalog.snapshotId())
                    || catalog.snapshotId().contains(LlmQuotaConstants.USAGE_KEY_SEPARATOR)
                    || Objects.isNull(catalog.models()) || catalog.models().isEmpty()) {
                throw new IllegalArgumentException(LlmQuotaConstants.INVALID_CATALOG);
            }
            snapshotId = catalog.snapshotId().trim();
            quotaZone = ZoneId.of(catalog.timeZone());
            for (LlmModelCatalogDTO.ModelQuota quota : catalog.models()) {
                if (Objects.isNull(quota) || StringUtils.isBlank(quota.model())
                        || !quota.model().equals(quota.model().trim())
                        || quota.model().contains(LlmQuotaConstants.USAGE_KEY_SEPARATOR)
                        || (!Boolean.TRUE.equals(quota.unlimited())
                            && (Objects.isNull(quota.remainingTokens()) || quota.remainingTokens() < 0))
                        || Objects.isNull(quota.enabled())
                        || modelQuotas.containsKey(quota.model())) {
                    throw new IllegalArgumentException(LlmQuotaConstants.INVALID_CATALOG);
                }
                expiryDates.put(quota.model(), Boolean.TRUE.equals(quota.unlimited())
                        ? LocalDate.MAX : DateUtils.parseIsoDate(quota.expiresOn()));
                modelQuotas.put(quota.model(), quota);
            }
            log.info("LLM quota catalog loaded, snapshotId={}, modelCount={}, timeZone={}",
                    snapshotId, modelQuotas.size(), quotaZone);
        } catch (Exception e) {
            throw new IllegalStateException(LlmQuotaConstants.INVALID_CATALOG, e);
        }
    }

    /**
     * 估算本次 LLM 翻译请求 token 消耗。
     *
     * @param source       待翻译文本
     * @param systemPrompt 本次请求 system prompt
     * @return token 估算结果
     */
    public TokenEstimate estimate(String source, String systemPrompt) {
        long promptTokens = estimateTokens(source) + estimateTokens(systemPrompt);
        long estimatedTotalTokens = promptTokens + Math.max(TranslationConstants.LLM_TOKEN_ESTIMATE_MIN_TOKENS,
                properties.getEstimatedCompletionTokens());
        return new TokenEstimate(promptTokens, estimatedTotalTokens);
    }

    /**
     * 选择未过期且可用余额足够的模型，优先最近到期，同日按 JSON 顺序，并预占估算额度。
     *
     * @param estimate       token 估算结果
     * @param excludedModels 本次请求已失败或已尝试模型
     * @return 模型选择结果
     */
    public ModelSelection selectModel(TokenEstimate estimate, Collection<String> excludedModels) {
        synchronized (usageLock) {
            if (Objects.isNull(estimate) || estimate.promptTokens() < 0
                    || estimate.estimatedTotalTokens() <= 0
                    || estimate.estimatedTotalTokens() < estimate.promptTokens()) {
                throw new IllegalArgumentException(LlmQuotaConstants.INVALID_ESTIMATE);
            }
            List<String> modelPool = modelPool();
            Set<String> excluded = Objects.isNull(excludedModels) ? Set.of() : new HashSet<>(excludedModels);
            ModelSelection selected = modelPool.stream()
                    .filter(model -> !excluded.contains(model))
                    .filter(model -> !LocalDate.now(clock.withZone(quotaZone)).isAfter(expiryDates.get(model)))
                    .map(model -> new ModelSelection(model, remainingTokens(model), estimate.promptTokens(),
                            estimate.estimatedTotalTokens()))
                    .filter(selection -> selection.remainingTokens() >= estimate.estimatedTotalTokens())
                    .min(Comparator.comparing((ModelSelection selection) -> expiryDates.get(selection.model()))
                            .thenComparingInt(selection -> modelPool.indexOf(selection.model())))
                    .orElseThrow(() -> new IllegalStateException(
                            TranslationConstants.LLM_ALL_MODELS_QUOTA_EXHAUSTED_ERROR_MESSAGE));
            if (!isUnlimited(selected.model())) {
                reservedTokens.merge(selected.model(), estimate.estimatedTotalTokens(), Long::sum);
            }
            return selected;
        }
    }

    /**
     * 请求结束后释放预占额度；成功、失败和额度切换均需调用。
     *
     * @param model 本次选择的模型
     * @param estimate 本次请求估算额度
     */
    public void releaseReservation(String model, TokenEstimate estimate) {
        synchronized (usageLock) {
            reservedTokens.computeIfPresent(model,
                    (key, reserved) -> Math.max(0L, reserved - estimate.estimatedTotalTokens()));
        }
    }

    /**
     * 记录模型调用成功后的真实或估算 token 消耗。
     *
     * @param model    模型名称
     * @param usage    LLM 响应 usage，缺失时为空
     * @param estimate 调用前 token 估算结果
     */
    public void recordSuccess(String model, LlmTokenUsageDTO usage, TokenEstimate estimate) {
        synchronized (usageLock) {
            ModelUsage modelUsage = modelUsageMap.computeIfAbsent(usageKey(model), ignored -> new ModelUsage());
            long promptTokens = Objects.isNull(usage) ? estimate.promptTokens() : usage.getPromptTokens();
            long completionTokens = Objects.isNull(usage) ? Math.max(0L,
                    estimate.estimatedTotalTokens() - estimate.promptTokens()) : usage.getCompletionTokens();
            long totalTokens = Objects.isNull(usage) ? estimate.estimatedTotalTokens() : usage.getTotalTokens();
            if (Objects.isNull(usage)) {
                log.warn("{}, model={}, estimatedTotalTokens={}",
                        TranslationConstants.LLM_USAGE_MISSING_WARNING_MESSAGE, model, totalTokens);
            }
            // 使用真实 usage 或估算值累计模型额度，保证下一次选择能避开余额不足模型。
            modelUsage.usedTokens += totalTokens;
            modelUsage.promptTokens += promptTokens;
            modelUsage.completionTokens += completionTokens;
            modelUsage.requestCount++;
            modelUsage.successfulRequests++;
            modelUsage.lastUsedAt = LocalDateTime.now().toString();
            // 并发成功请求不得清除另一请求已标记的外部额度耗尽状态。
            persistUsage();
            logStatistics(model, modelUsage);
            log.info("LLM model usage recorded, model={}, promptTokens={}, completionTokens={}, totalTokens={}, remainingTokens={}",
                    model, promptTokens, completionTokens, totalTokens, remainingTokens(model));
        }
    }

    /**
     * 标记模型额度已不可用。
     *
     * @param model  模型名称
     * @param reason 额度不可用原因
     */
    public void markQuotaExceeded(String model, String reason) {
        synchronized (usageLock) {
            // 无本地限额的模型仅让本次请求失败，不因 429 永久禁用后续请求。
            if (isUnlimited(model)) {
                return;
            }
            ModelUsage modelUsage = modelUsageMap.computeIfAbsent(usageKey(model), ignored -> new ModelUsage());
            // 标记不可用，但不伪造 token 消耗；更换额度快照后重新记账。
            modelUsage.quotaExhausted = true;
            modelUsage.quotaExhaustedReason = StringUtils.defaultIfBlank(reason,
                    TranslationConstants.LLM_QUOTA_EXHAUSTED_DEFAULT_REASON);
            modelUsage.lastUsedAt = LocalDateTime.now().toString();
            persistUsage();
            log.warn("LLM model quota marked exhausted, model={}, reason={}", model,
                    modelUsage.quotaExhaustedReason);
        }
    }

    /**
     * 获取模型池大小。
     *
     * @return 模型数量
     */
    public int modelCount() {
        return modelPool().size();
    }

    /** 返回模型 JSON 中的思考参数；未配置时保留原请求行为。 */
    public String reasoningEffort(String model) {
        return modelQuotas.get(model).reasoningEffort();
    }

    /** 判断是否跳过本地 token 额度及到期限制，不代表服务商无限额度。 */
    private boolean isUnlimited(String model) {
        return Boolean.TRUE.equals(modelQuotas.get(model).unlimited());
    }

    /** 记录一次失败的实际模型调用；不猜测失败请求消耗的 token。 */
    public void recordFailure(String model) {
        synchronized (usageLock) {
            ModelUsage usage = modelUsageMap.computeIfAbsent(usageKey(model), ignored -> new ModelUsage());
            usage.failedRequests++;
            usage.lastUsedAt = LocalDateTime.now().toString();
            persistUsage();
            logStatistics(model, usage);
        }
    }

    /** 计算已完成请求成功率；历史缺少失败记录的 requestCount 不进入分母。 */
    private Double successRatePercent(ModelUsage usage) {
        double completed = (double) usage.successfulRequests + usage.failedRequests;
        return completed == 0 ? null
                : usage.successfulRequests * LlmQuotaConstants.PERCENT_MULTIPLIER / completed;
    }

    /** 输出同一统计口径的成功数、失败数及成功率，便于运行时排查。 */
    private void logStatistics(String model, ModelUsage usage) {
        log.info("LLM translation statistics, model={}, successfulRequests={}, failedRequests={}, successRatePercent={}",
                model, usage.successfulRequests, usage.failedRequests, successRatePercent(usage));
    }

    /**
     * 加载模型用量缓存文件。
     */
    private void loadUsage() {
        synchronized (usageLock) {
            Path path = Path.of(properties.getUsageFile()).toAbsolutePath().normalize();
            if (!Files.isRegularFile(path)) {
                return;
            }
            try {
                Map<String, Object> root = JsonUtils.file2StringObjectMap(path);
                if (!(root.get(TranslationConstants.LLM_USAGE_FIELD_MODELS) instanceof Map<?, ?> models)) {
                    throw new IllegalStateException(LlmQuotaConstants.INVALID_USAGE);
                }
                models.forEach((model, value) -> {
                    if (Objects.nonNull(model) && Objects.nonNull(value)) {
                        modelUsageMap.put(String.valueOf(model), buildModelUsage(value));
                    }
                });
                log.info("LLM model usage loaded, path={}, modelCount={}", path, modelUsageMap.size());
            } catch (Exception e) {
                throw new IllegalStateException(LlmQuotaConstants.INVALID_USAGE, e);
            }
        }
    }

    /**
     * 根据 JSON 对象构建模型用量状态。
     *
     * @param value JSON 读取出的模型用量对象
     * @return 模型用量状态
     */
    private ModelUsage buildModelUsage(Object value) {
        Map<String, Object> usage = JsonUtils.convert2StringObjectMap(value);
        ModelUsage modelUsage = new ModelUsage();
        modelUsage.usedTokens = longValue(usage.get(TranslationConstants.LLM_USAGE_FIELD_USED_TOKENS));
        if (modelUsage.usedTokens < 0) {
            throw new IllegalStateException(LlmQuotaConstants.INVALID_USAGE);
        }
        modelUsage.promptTokens = longValue(usage.get(TranslationConstants.LLM_USAGE_FIELD_PROMPT_TOKENS));
        modelUsage.completionTokens = longValue(usage.get(TranslationConstants.LLM_USAGE_FIELD_COMPLETION_TOKENS));
        modelUsage.requestCount = longValue(usage.get(TranslationConstants.LLM_USAGE_FIELD_REQUEST_COUNT));
        modelUsage.successfulRequests = longValue(usage.get(LlmQuotaConstants.SUCCESSFUL_REQUESTS));
        modelUsage.failedRequests = longValue(usage.get(LlmQuotaConstants.FAILED_REQUESTS));
        if (modelUsage.successfulRequests < 0 || modelUsage.failedRequests < 0) {
            throw new IllegalStateException(LlmQuotaConstants.INVALID_USAGE);
        }
        modelUsage.lastUsedAt = stringValue(usage.get(TranslationConstants.LLM_USAGE_FIELD_LAST_USED_AT));
        modelUsage.quotaExhausted = booleanValue(usage.get(TranslationConstants.LLM_USAGE_FIELD_QUOTA_EXHAUSTED));
        modelUsage.quotaExhaustedReason = stringValue(
                usage.get(TranslationConstants.LLM_USAGE_FIELD_QUOTA_EXHAUSTED_REASON));
        return modelUsage;
    }

    /**
     * 持久化模型用量状态，失败时只记录日志，不影响当前翻译返回。
     */
    private void persistUsage() {
        Path target = Path.of(properties.getUsageFile()).toAbsolutePath().normalize();
        Path temp = target.resolveSibling(target.getFileName() + TranslationConstants.LLM_USAGE_TEMP_FILE_SUFFIX);
        try {
            // 先写临时文件，再移动到目标文件，减少写入中断导致的用量文件损坏。
            Files.createDirectories(Objects.requireNonNull(target.getParent()));
            JsonUtils.writeJson2File(buildUsagePayload(), temp);
            moveUsageFile(temp, target);
        } catch (Exception e) {
            log.warn("Failed to persist LLM model usage, path={}", target, e);
        }
    }

    /**
     * 构建模型用量持久化载荷。
     *
     * @return 用量 JSON 载荷
     */
    private Map<String, Object> buildUsagePayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        // 保留旧账期和已移除模型的历史，当前快照只使用带 snapshotId 的记录。
        payload.put(TranslationConstants.LLM_USAGE_FIELD_MODELS, modelUsageMap.keySet().stream().sorted()
                .collect(Collectors.toMap(key -> key, key -> usagePayload(modelUsageMap.get(key)),
                        (left, right) -> left, LinkedHashMap::new)));
        return payload;
    }

    /**
     * 构建单个模型用量载荷。
     *
     * @param usage 模型用量状态
     * @return 用量 Map
     */
    private Map<String, Object> usagePayload(ModelUsage usage) {
        ModelUsage safeUsage = Objects.isNull(usage) ? new ModelUsage() : usage;
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put(TranslationConstants.LLM_USAGE_FIELD_USED_TOKENS, safeUsage.usedTokens);
        payload.put(TranslationConstants.LLM_USAGE_FIELD_PROMPT_TOKENS, safeUsage.promptTokens);
        payload.put(TranslationConstants.LLM_USAGE_FIELD_COMPLETION_TOKENS, safeUsage.completionTokens);
        payload.put(TranslationConstants.LLM_USAGE_FIELD_REQUEST_COUNT, safeUsage.requestCount);
        payload.put(LlmQuotaConstants.SUCCESSFUL_REQUESTS, safeUsage.successfulRequests);
        payload.put(LlmQuotaConstants.FAILED_REQUESTS, safeUsage.failedRequests);
        payload.put(LlmQuotaConstants.SUCCESS_RATE_PERCENT, successRatePercent(safeUsage));
        payload.put(TranslationConstants.LLM_USAGE_FIELD_LAST_USED_AT, safeUsage.lastUsedAt);
        payload.put(TranslationConstants.LLM_USAGE_FIELD_QUOTA_EXHAUSTED, safeUsage.quotaExhausted);
        payload.put(TranslationConstants.LLM_USAGE_FIELD_QUOTA_EXHAUSTED_REASON, safeUsage.quotaExhaustedReason);
        return payload;
    }

    /**
     * 移动模型用量临时文件到目标文件，原子移动失败时降级为普通覆盖移动。
     *
     * @param temp   临时文件
     * @param target 目标文件
     * @throws Exception 文件移动失败时抛出
     */
    private void moveUsageFile(Path temp, Path target) throws Exception {
        try {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception ignored) {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * 从 JSON 快照构建启用模型池，不回退到 properties 或 Java 内置模型。
     *
     * @return 模型池
     */
    private List<String> modelPool() {
        return modelQuotas.values().stream()
                .filter(quota -> Boolean.TRUE.equals(quota.enabled()))
                .map(LlmModelCatalogDTO.ModelQuota::model)
                .toList();
    }

    /**
     * 构造快照内模型用量标识，避免用户提供的当前余额再次扣除旧历史用量。
     *
     * @param model 模型 ID
     * @return 账期内唯一标识
     */
    private String usageKey(String model) {
        return snapshotId + LlmQuotaConstants.USAGE_KEY_SEPARATOR + model;
    }

    /**
     * 计算模型剩余 token 额度。
     *
     * @param model 模型名称
     * @return 剩余 token 数
     */
    private long remainingTokens(String model) {
        if (isUnlimited(model)) {
            return LlmQuotaConstants.UNLIMITED_TOKENS;
        }
        ModelUsage usage = modelUsageMap.get(usageKey(model));
        if (Objects.nonNull(usage) && usage.quotaExhausted) {
            return 0L;
        }
        long usedTokens = Objects.isNull(usage) ? 0L : usage.usedTokens;
        long available = Math.max(0L, modelQuotas.get(model).remainingTokens() - usedTokens);
        return Math.max(0L, available - reservedTokens.getOrDefault(model, 0L));
    }

    /**
     * 使用保守字符规则估算 token 数。
     *
     * @param text 待估算文本
     * @return token 数
     */
    private long estimateTokens(String text) {
        if (StringUtils.isBlank(text)) {
            return 0L;
        }
        long tokens = 0L;
        int englishChars = 0;
        for (int i = 0; i < text.length(); i++) {
            char value = text.charAt(i);
            if (isCjkOrJapanese(value)) {
                tokens++;
            } else if (!Character.isWhitespace(value)) {
                englishChars++;
            }
        }
        tokens += Math.ceilDiv(englishChars, TranslationConstants.LLM_TOKEN_ESTIMATE_ENGLISH_CHARS_PER_TOKEN);
        return Math.max(TranslationConstants.LLM_TOKEN_ESTIMATE_MIN_TOKENS, tokens);
    }

    /**
     * 判断字符是否属于中文、日文假名或韩文范围。
     *
     * @param value 字符
     * @return true 表示按单字符 token 估算
     */
    private boolean isCjkOrJapanese(char value) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(value);
        return Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS.equals(block)
                || Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A.equals(block)
                || Character.UnicodeBlock.HIRAGANA.equals(block)
                || Character.UnicodeBlock.KATAKANA.equals(block)
                || Character.UnicodeBlock.HANGUL_SYLLABLES.equals(block);
    }

    /**
     * 将对象转换为 long，空值或非法值返回 0。
     *
     * @param value 原始值
     * @return long 值
     */
    private long longValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (Objects.nonNull(value) && StringUtils.isNumeric(String.valueOf(value))) {
            return Long.parseLong(String.valueOf(value));
        }
        return 0L;
    }

    /**
     * 将对象转换为 boolean，空值返回 false。
     *
     * @param value 原始值
     * @return boolean 值
     */
    private boolean booleanValue(Object value) {
        return value instanceof Boolean bool && bool;
    }

    /**
     * 将对象转换为字符串，空值返回 null。
     *
     * @param value 原始值
     * @return 字符串值
     */
    private String stringValue(Object value) {
        return Objects.isNull(value) ? null : String.valueOf(value);
    }

    /**
     * 模型用量状态。
     */
    private static class ModelUsage {
        private long usedTokens;
        private long promptTokens;
        private long completionTokens;
        private long requestCount;
        private long successfulRequests;
        private long failedRequests;
        private String lastUsedAt;
        private boolean quotaExhausted;
        private String quotaExhaustedReason;
    }

    /**
     * LLM token 估算结果。
     *
     * @param promptTokens         预估 prompt token 数
     * @param estimatedTotalTokens 预估总 token 数
     */
    public record TokenEstimate(long promptTokens, long estimatedTotalTokens) {
    }

    /**
     * LLM 模型选择结果。
     *
     * @param model                模型名称
     * @param remainingTokens      选择前剩余 token 数
     * @param estimatedPromptTokens 预估 prompt token 数
     * @param estimatedTotalTokens 预估总 token 数
     */
    public record ModelSelection(String model, long remainingTokens, long estimatedPromptTokens,
                                 long estimatedTotalTokens) {
    }
}
