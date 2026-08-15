package com.hb.dokkan.service.translation;

import com.hb.dokkan.common.constants.TranslationConstants;
import com.hb.dokkan.common.domain.po.mysql.category.DokkanCategoryPO;
import com.hb.dokkan.common.utils.JsonUtils;
import com.hb.dokkan.common.utils.TranslationUtils;
import com.hb.dokkan.infrastructure.mysql.categories.DokkanCategoryRepository;
import com.hb.dokkan.service.translation.client.GoogleTranslationClient;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Description Dokkan文本翻译服务
 * @Author stargazer
 * @Date 2026/8/15 19:00
 **/
@Slf4j
@Service
public class DokkanTranslationService {

    /** 日文假名匹配模式，用于区分 Google 翻译源语言。 */
    private static final Pattern JAPANESE_KANA = Pattern.compile(TranslationConstants.JAPANESE_KANA_REGEX);

    /** 翻译缓存，key 为原文，value 为简体中文翻译。 */
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    /** 运行期领域术语表，来源于本地分类数据库。 */
    private final Map<String, String> domainGlossary = new ConcurrentHashMap<>();

    /** 缓存写入锁，避免并发持久化互相覆盖。 */
    private final Object cacheWriteLock = new Object();

    /** 翻译并发许可，限制远程 Google 翻译调用并发。 */
    private final Semaphore translationPermits = new Semaphore(TranslationConstants.GOOGLE_TRANSLATION_PERMITS);

    /** 领域术语是否已经预热。 */
    private volatile boolean domainGlossaryLoaded;

    @Resource
    private DokkanCategoryRepository categoryRepository;

    @Resource
    private GoogleTranslationClient googleTranslationClient;

    @Resource
    private TranslationMappingService translationMappingService;

    @Value("${dokkan.translation.enabled:true}")
    private boolean enabled;

    @Value("${dokkan.translation.cache-file:./data/dokkan-translation-cache.json}")
    private String cacheFile;

    /**
     * 初始化翻译服务，加载自定义词表和本地缓存。
     */
    @PostConstruct
    public void initialize() {
        loadCustomGlossary();
        loadCache();
    }

    /**
     * 注册可信术语，可信术语会优先于远程翻译和缓存。
     *
     * @param source  原文术语
     * @param chinese 简体中文术语
     */
    public void registerTrustedTerm(String source, String chinese) {
        if (StringUtils.isAnyBlank(source, chinese) || source.equals(chinese)) {
            return;
        }
        domainGlossary.put(source, TranslationUtils.toSimpleChineseText(chinese));
        cache.keySet().removeIf(cachedSource -> cachedSource.contains(source));
    }

    /**
     * 翻译单条文本，失败时沿用批量翻译的源文本兜底策略。
     *
     * @param source 待翻译文本
     * @return 翻译后的文本
     */
    public String translate(String source) {
        return translateAll(Collections.singletonList(source)).getFirst();
    }

    /**
     * 批量翻译文本，优先使用可信术语和缓存，缺失部分再调用远程翻译。
     *
     * @param sourceTexts 待翻译文本集合
     * @return 与入参顺序一致的翻译结果列表
     */
    public List<String> translateAll(Collection<String> sourceTexts) {
        if (Objects.isNull(sourceTexts)) {
            return Collections.emptyList();
        }
        List<String> sources = new ArrayList<>(sourceTexts);
        if (!enabled || sources.isEmpty()) {
            return sources;
        }

        // 先预热领域术语，保障分类等固定名称翻译稳定。
        ensureDomainGlossary();
        // 找出既不是可信术语、也未命中缓存的文本，避免重复请求远程翻译。
        List<String> missing = sources.stream()
                .filter(StringUtils::isNotBlank)
                .filter(source -> Objects.isNull(trustedTranslation(source)))
                .filter(source -> !cache.containsKey(source))
                .distinct()
                .toList();
        if (!missing.isEmpty()) {
            // 缺失内容按批次翻译，完成后持久化缓存。
            translateMissing(missing);
            persistCache();
        }
        return sources.stream()
                .map(this::translatedValue)
                .toList();
    }

    /**
     * 强制重新翻译文本，重新加载本地分类术语并移除命中的旧翻译缓存。
     *
     * @param sourceTexts 待重新翻译文本集合
     * @return 与入参顺序一致的翻译结果列表
     */
    public List<String> retranslateAll(Collection<String> sourceTexts) {
        if (Objects.isNull(sourceTexts)) {
            return Collections.emptyList();
        }
        List<String> sources = new ArrayList<>(sourceTexts);
        refreshCategoryDomainGlossary();
        sources.stream()
                .filter(StringUtils::isNotBlank)
                .forEach(cache::remove);
        return translateAll(sources);
    }

    /**
     * 加载数据库自定义术语表，失败时降级为空词表并继续翻译主链路。
     */
    public synchronized void loadCustomGlossary() {
        try {
            translationMappingService.refreshMappingCache();
            log.info("Dokkan translation mapping glossary loaded, size={}",
                    translationMappingService.listEnabledMappingMap().size());
        } catch (Exception e) {
            log.warn("Failed to load Dokkan translation mapping glossary", e);
        }
    }

    /**
     * 加载本地翻译缓存，缓存版本或词表哈希不匹配时忽略旧缓存。
     */
    private void loadCache() {
        Path path = Path.of(cacheFile).toAbsolutePath().normalize();
        if (!Files.isRegularFile(path)) {
            return;
        }
        try {
            Map<String, Object> root = JsonUtils.file2StringObjectMap(path);
            if (Objects.isNull(root)
                    || !Objects.equals(root.get(TranslationConstants.CACHE_FIELD_VERSION),
                    TranslationConstants.CACHE_FORMAT_VERSION)
                    || !Objects.equals(root.get(TranslationConstants.CACHE_FIELD_GLOSSARY_HASH), glossaryHash())
                    || !(root.get(TranslationConstants.CACHE_FIELD_TRANSLATIONS) instanceof Map<?, ?>)) {
                log.info("Ignoring legacy Dokkan translation cache, path={}", path);
                return;
            }
            Map<String, String> translations = JsonUtils.convert2StringMap(
                    root.get(TranslationConstants.CACHE_FIELD_TRANSLATIONS));
            cache.putAll(translations);
            log.info("Dokkan translation cache loaded, path={}, size={}", path, cache.size());
        } catch (Exception e) {
            log.warn("Failed to load Dokkan translation cache, path={}", path, e);
        }
    }

    /**
     * 获取单条文本最终翻译值，优先可信术语，其次缓存，最后保留源文本。
     *
     * @param source 原文文本
     * @return 归一化后的翻译值
     */
    private String translatedValue(String source) {
        if (StringUtils.isBlank(source)) {
            return source;
        }
        String trusted = trustedTranslation(source);
        return normalizeTerms(Objects.isNull(trusted) ? cache.getOrDefault(source, source) : trusted);
    }

    /**
     * 预热数据库领域术语，失败时降级为仅使用静态词表和缓存。
     */
    private synchronized void ensureDomainGlossary() {
        if (domainGlossaryLoaded) {
            return;
        }
        try {
            // 技能描述里主要出现分类名，直接使用本地分类表的英文名和中文名构建术语映射，避免翻译时再查外部接口。
            categoryRepository.list().stream()
                    .filter(row -> StringUtils.isNoneBlank(row.getCategoryNameEn(), row.getCategoryName()))
                    .forEach(row -> registerTrustedTerm(row.getCategoryNameEn(), row.getCategoryName()));
            domainGlossaryLoaded = true;
            log.info("Dokkan database translation memory loaded, terms={}", domainGlossary.size());
        } catch (Exception e) {
            // 术语预热失败不影响翻译主链路，失败时继续使用静态词表与缓存。
            domainGlossaryLoaded = true;
            log.warn("Failed to build Dokkan database translation memory", e);
        }
    }

    /**
     * 刷新本地分类领域术语表。
     */
    public synchronized void refreshCategoryDomainGlossary() {
        domainGlossaryLoaded = false;
        domainGlossary.clear();
        ensureDomainGlossary();
    }

    /**
     * 查询可信术语翻译，优先自定义词表，其次领域词表，最后静态词表。
     *
     * @param source 原文术语
     * @return 可信翻译，不存在时返回 null
     */
    private String trustedTranslation(String source) {
        String value = translationMappingService.listEnabledMappingMap().get(source);
        if (Objects.nonNull(value)) {
            return value;
        }
        value = domainGlossary.get(source);
        return Objects.isNull(value) ? TranslationConstants.DEFAULT_TERM_GLOSSARY.get(source) : value;
    }

    /**
     * 将缺失翻译按字符阈值拆分为多个批次。
     *
     * @param missing 缺失翻译的文本列表
     */
    private void translateMissing(List<String> missing) {
        List<String> batch = new ArrayList<>();
        int chars = 0;
        for (String source : missing) {
            if (!batch.isEmpty() && chars + source.length() > TranslationConstants.GOOGLE_TRANSLATION_MAX_BATCH_CHARS) {
                translateBatch(batch);
                batch.clear();
                chars = 0;
            }
            batch.add(source);
            chars += source.length();
        }
        if (!batch.isEmpty()) {
            translateBatch(batch);
        }
    }

    /**
     * 翻译单个批次，失败时不写入缓存并保留源文本兜底。
     *
     * @param sources 批次原文列表
     */
    private void translateBatch(List<String> sources) {
        boolean acquired = false;
        List<String> retrySources = new ArrayList<>(sources);
        try {
            // 获取并发许可，限制外部翻译接口压力。
            translationPermits.acquire();
            acquired = true;
            // 保护领域术语和行标记，避免远程翻译破坏结果拆分和术语一致性。
            ProtectedText protectedText = buildProtectedText(sources);
            String response = googleTranslationClient.translate(protectedText.text(), detectSourceLanguage(sources),
                    sources.size());
            Map<Integer, String> translated = parseResponse(response, protectedText);
            // 将完整行写入缓存，不完整行保留到单条重试。
            for (int i = 0; i < sources.size(); i++) {
                String value = translated.get(i);
                if (StringUtils.isNotBlank(value)) {
                    cache.put(sources.get(i), normalizeTerms(value));
                    retrySources.remove(sources.get(i));
                }
            }
            log.info("Dokkan text translation completed, requested={}, translated={}", sources.size(), translated.size());
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.warn("Dokkan text translation failed; source text will be retained, count={}", sources.size(), e);
        } finally {
            if (acquired) {
                translationPermits.release();
            }
        }
        retryIncompleteTranslations(sources, retrySources);
    }

    /**
     * 对批量翻译中缺失的行进行单条重试。
     *
     * @param sources      原批次文本列表
     * @param retrySources 待重试文本列表
     */
    private void retryIncompleteTranslations(List<String> sources, List<String> retrySources) {
        if (sources.size() <= 1 || retrySources.isEmpty()) {
            return;
        }
        log.info("Retrying incomplete Dokkan translations individually, count={}", retrySources.size());
        retrySources.forEach(source -> translateBatch(Collections.singletonList(source)));
    }

    /**
     * 构建受保护翻译文本，按行标记合并多条文本。
     *
     * @param sources 原文列表
     * @return 受保护文本上下文
     */
    private ProtectedText buildProtectedText(List<String> sources) {
        StringBuilder text = new StringBuilder();
        Map<String, String> tokens = new LinkedHashMap<>();
        Map<String, String> sourceTokenKeys = new LinkedHashMap<>();
        Map<Integer, List<String>> rowTokens = new LinkedHashMap<>();
        Map<String, String> glossary = glossarySnapshot();
        Pattern protectedTerms = buildProtectedTermPattern(glossary);
        int tokenIndex = 0;
        for (int i = 0; i < sources.size(); i++) {
            tokenIndex = appendProtectedRow(sources.get(i), i, tokenIndex, protectedTerms,
                    new ProtectedRowContext(text, tokens, sourceTokenKeys, rowTokens, glossary));
        }
        text.append(TranslationConstants.ROW_END_MARKER);
        return new ProtectedText(text.toString(), tokens, rowTokens, sources.size());
    }

    /**
     * 追加单行受保护翻译文本，并记录该行期望保留的 token。
     *
     * @param source         原文文本
     * @param rowIndex       行号
     * @param tokenIndex     当前 token 序号
     * @param protectedTerms 需要保护的术语模式
     * @param context        受保护文本上下文
     * @return 下一次可用 token 序号
     */
    private int appendProtectedRow(String source, int rowIndex, int tokenIndex, Pattern protectedTerms,
                                   ProtectedRowContext context) {
        Matcher matcher = protectedTerms.matcher(source);
        StringBuilder protectedText = new StringBuilder();
        int nextTokenIndex = tokenIndex;
        while (matcher.find()) {
            String sourceToken = matcher.group();
            String key = context.sourceTokenKeys().get(sourceToken);
            if (Objects.isNull(key)) {
                key = TranslationConstants.TOKEN_MARKER_PREFIX + nextTokenIndex++
                        + TranslationConstants.TOKEN_MARKER_SUFFIX;
                context.sourceTokenKeys().put(sourceToken, key);
                context.tokens().put(key, context.glossary().getOrDefault(sourceToken, sourceToken));
            }
            List<String> expectedTokens = context.rowTokens().computeIfAbsent(rowIndex, ignored -> new ArrayList<>());
            if (!expectedTokens.contains(key)) {
                expectedTokens.add(key);
            }
            matcher.appendReplacement(protectedText, Matcher.quoteReplacement(key));
        }
        matcher.appendTail(protectedText);
        context.text()
                .append(rowMarker(rowIndex))
                .append(TranslationConstants.BATCH_ROW_SEPARATOR)
                .append(protectedText)
                .append(TranslationConstants.BATCH_ROW_SEPARATOR);
        return nextTokenIndex;
    }

    /**
     * 解析 Google 翻译响应，并按行标记还原为行号到译文的映射。
     *
     * @param response      Google 翻译原始响应
     * @param protectedText 受保护文本上下文
     * @return 行号到译文的映射
     */
    private Map<Integer, String> parseResponse(String response, ProtectedText protectedText) {
        List<String> sentenceTexts = JsonUtils.readArrayFieldTexts(response,
                TranslationConstants.GOOGLE_RESPONSE_FIELD_SENTENCES,
                TranslationConstants.GOOGLE_RESPONSE_FIELD_TRANS);
        String translated = String.join(StringUtils.EMPTY, sentenceTexts);
        Map<Integer, String> result = new LinkedHashMap<>();
        for (int i = 0; i < protectedText.size(); i++) {
            String value = extractRow(translated, i, protectedText);
            if (StringUtils.isNotBlank(value)) {
                result.put(i, value);
            }
        }
        return result;
    }

    /**
     * 从合并译文中抽取指定行，并恢复该行受保护术语。
     *
     * @param translated     合并译文
     * @param index          行号
     * @param protectedText  受保护文本上下文
     * @return 指定行译文，标记缺失或 token 缺失时返回 null
     */
    private String extractRow(String translated, int index, ProtectedText protectedText) {
        String startMarker = rowMarker(index);
        String endMarker = index + 1 < protectedText.size() ? rowMarker(index + 1) : TranslationConstants.ROW_END_MARKER;
        int start = translated.indexOf(startMarker);
        int end = start < 0 ? -1 : translated.indexOf(endMarker, start + startMarker.length());
        if (start < 0 || end < 0) {
            return null;
        }
        String value = translated.substring(start + startMarker.length(), end).trim();
        List<String> expectedTokens = protectedText.rowTokens().getOrDefault(index, Collections.emptyList());
        for (String token : expectedTokens) {
            if (!value.contains(token)) {
                log.warn("Incomplete Dokkan translation row, index={}, missingToken={}", index, token);
                return null;
            }
        }
        for (String token : expectedTokens) {
            value = value.replace(token, protectedText.tokens().get(token));
        }
        return value;
    }

    /**
     * 构建批量翻译行标记。
     *
     * @param index 行号
     * @return 行标记
     */
    private String rowMarker(int index) {
        return TranslationConstants.ROW_MARKER_PREFIX + index + TranslationConstants.ROW_MARKER_SUFFIX;
    }

    /**
     * 根据是否存在日文假名判断 Google 翻译源语言。
     *
     * @param sources 原文列表
     * @return Google 翻译源语言编码
     */
    private String detectSourceLanguage(List<String> sources) {
        return sources.stream()
                .filter(Objects::nonNull)
                .anyMatch(text -> JAPANESE_KANA.matcher(text).find())
                ? TranslationConstants.LANGUAGE_JA : TranslationConstants.LANGUAGE_EN;
    }

    /**
     * 归一化翻译术语，修正常见机器翻译表述。
     *
     * @param translated 原始译文
     * @return 归一化译文
     */
    private String normalizeTerms(String translated) {
        String normalized = TranslationUtils.toSimpleChineseText(translated);
        for (Map.Entry<String, String> entry : TranslationConstants.NORMALIZE_TEXT_REPLACEMENTS.entrySet()) {
            normalized = normalized.replace(entry.getKey(), entry.getValue());
        }
        return normalized
                .replaceAll(TranslationConstants.NORMALIZE_REGEX_KI_WORD,
                        TranslationConstants.NORMALIZE_REPLACEMENT_KI)
                .replaceAll(TranslationConstants.NORMALIZE_REGEX_LINE_START_QUESTION,
                        TranslationConstants.NORMALIZE_REPLACEMENT_BULLET)
                .replaceAll(TranslationConstants.NORMALIZE_REGEX_TURN_SUFFIX,
                        TranslationConstants.NORMALIZE_REPLACEMENT_TURN)
                .replaceAll(TranslationConstants.NORMALIZE_REGEX_TURN_WITH_SPACES,
                        TranslationConstants.NORMALIZE_REPLACEMENT_TURN)
                .replaceAll(TranslationConstants.NORMALIZE_REGEX_ATK_DEF_CONNECTOR,
                        TranslationConstants.NORMALIZE_REPLACEMENT_ATK_DEF)
                .replaceAll(TranslationConstants.NORMALIZE_REGEX_HP_ATK_DEF,
                        TranslationConstants.NORMALIZE_REPLACEMENT_HP_ATK_DEF)
                .replaceAll(TranslationConstants.NORMALIZE_REGEX_SPACE_BEFORE_PUNCTUATION,
                        TranslationConstants.NORMALIZE_REPLACEMENT_FIRST_GROUP);
    }

    /**
     * 持久化翻译缓存，失败时只记录日志，不影响当前翻译返回。
     */
    private void persistCache() {
        synchronized (cacheWriteLock) {
            Path target = Path.of(cacheFile).toAbsolutePath().normalize();
            Path temp = target.resolveSibling(target.getFileName() + TranslationConstants.CACHE_TEMP_FILE_SUFFIX);
            try {
                // 先写临时文件，再移动到目标文件，减少写入中断导致的缓存损坏。
                Files.createDirectories(Objects.requireNonNull(target.getParent()));
                JsonUtils.writeJson2File(buildCachePayload(), temp);
                moveCacheFile(temp, target);
            } catch (Exception e) {
                log.warn("Failed to persist Dokkan translation cache, path={}", target, e);
            }
        }
    }

    /**
     * 构建缓存持久化载荷，翻译明细按 key 排序保证文件稳定。
     *
     * @return 缓存 JSON 载荷
     */
    private Map<String, Object> buildCachePayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put(TranslationConstants.CACHE_FIELD_VERSION, TranslationConstants.CACHE_FORMAT_VERSION);
        payload.put(TranslationConstants.CACHE_FIELD_GLOSSARY_HASH, glossaryHash());
        payload.put(TranslationConstants.CACHE_FIELD_TRANSLATIONS, cache.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (left, right) -> left, LinkedHashMap::new)));
        return payload;
    }

    /**
     * 移动缓存临时文件到目标文件，原子移动失败时降级为普通覆盖移动。
     *
     * @param temp   缓存临时文件
     * @param target 缓存目标文件
     * @throws Exception 文件移动失败时抛出
     */
    private void moveCacheFile(Path temp, Path target) throws Exception {
        try {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception ignored) {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * 生成当前可用词表快照，静态词表优先级最低，自定义词表最高。
     *
     * @return 词表快照
     */
    private Map<String, String> glossarySnapshot() {
        Map<String, String> result = new LinkedHashMap<>(TranslationConstants.DEFAULT_TERM_GLOSSARY);
        result.putAll(domainGlossary);
        result.putAll(translationMappingService.listEnabledMappingMap());
        return result;
    }

    /**
     * 计算词表哈希，用于判断缓存是否仍然适配当前词表。
     *
     * @return 词表哈希
     */
    private int glossaryHash() {
        return TranslationConstants.GLOSSARY_HASH_BASE * TranslationConstants.DEFAULT_TERM_GLOSSARY.hashCode()
                + translationMappingService.listEnabledMappingMap().hashCode();
    }

    /**
     * 构建受保护术语匹配模式，长术语优先匹配避免被短术语截断。
     *
     * @param glossary 术语表
     * @return 受保护术语匹配模式
     */
    private static Pattern buildProtectedTermPattern(Map<String, String> glossary) {
        String glossaryTerms = glossary.keySet().stream()
                .filter(StringUtils::isNotBlank)
                .sorted(Comparator.comparingInt(String::length).reversed())
                .map(Pattern::quote)
                .collect(Collectors.joining(TranslationConstants.REGEX_OR_SEPARATOR));
        String pattern = StringUtils.isBlank(glossaryTerms)
                ? TranslationConstants.DEFAULT_PROTECTED_TERM_REGEX
                : TranslationConstants.DEFAULT_PROTECTED_TERM_REGEX + TranslationConstants.REGEX_OR_SEPARATOR + glossaryTerms;
        return Pattern.compile(pattern);
    }

    /**
     * 受保护翻译文本上下文。
     *
     * @param text      合并后的请求文本
     * @param tokens    token 到目标术语的映射
     * @param rowTokens 行号到该行期望 token 的映射
     * @param size      原文行数
     */
    private record ProtectedText(String text, Map<String, String> tokens,
                                 Map<Integer, List<String>> rowTokens, int size) {
    }

    /**
     * 追加受保护文本时的可变上下文。
     *
     * @param text            合并文本构造器
     * @param tokens          token 到目标术语的映射
     * @param sourceTokenKeys 原文术语到 token 的映射
     * @param rowTokens       行号到该行期望 token 的映射
     * @param glossary        当前词表快照
     */
    private record ProtectedRowContext(StringBuilder text, Map<String, String> tokens,
                                       Map<String, String> sourceTokenKeys,
                                       Map<Integer, List<String>> rowTokens,
                                       Map<String, String> glossary) {
    }
}
