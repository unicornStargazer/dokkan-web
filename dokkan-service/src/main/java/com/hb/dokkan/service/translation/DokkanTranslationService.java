package com.hb.dokkan.service.translation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.houbb.opencc4j.util.ZhConverterUtil;
import com.hb.dokkan.common.domain.po.mysql.category.DokkanCategoryPO;
import com.hb.dokkan.common.domain.po.mysql.link.DokkanLinkPO;
import com.hb.dokkan.infrastructure.mysql.categories.DokkanCategoryRepository;
import com.hb.dokkan.infrastructure.mysql.links.DokkanLinkRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
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
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Dokkan text translation with terminology protection and a persistent cache. */
@Slf4j
@Service
public class DokkanTranslationService {
    private static final int CACHE_FORMAT_VERSION = 3;
    private static final int MAX_BATCH_CHARS = 4500;
    private static final Pattern JAPANESE_KANA = Pattern.compile("[\\p{IsHiragana}\\p{IsKatakana}]");
    private static final Map<String, String> TERM_GLOSSARY = buildGlossary();

    private final Map<String, String> cache = new ConcurrentHashMap<>();
    private final Map<String, String> customGlossary = new ConcurrentHashMap<>();
    private final Map<String, String> domainGlossary = new ConcurrentHashMap<>();
    private final Object cacheWriteLock = new Object();
    private final Semaphore translationPermits = new Semaphore(4);
    private volatile boolean domainGlossaryLoaded;

    @Resource
    private ObjectMapper objectMapper;

    @Resource(name = "googleTranslationWebClient")
    private WebClient translationClient;

    @Resource
    private ResourceLoader resourceLoader;

    @Resource
    private DokkanCategoryRepository categoryRepository;

    @Resource
    private DokkanLinkRepository linkRepository;

    @Resource
    @Qualifier("dokkanDbWebClient")
    private WebClient dokkanDbJpClient;

    @Resource
    @Qualifier("dokkanDbGlobalWebClient")
    private WebClient dokkanDbGlobalClient;

    @Value("${dokkan.translation.enabled:true}")
    private boolean enabled;

    @Value("${dokkan.translation.cache-file:./data/dokkan-translation-cache.json}")
    private String cacheFile;

    @Value("${dokkan.translation.glossary-file:classpath:dokkan-translation-glossary.json}")
    private String glossaryFile;

    @PostConstruct
    public void initialize() {
        loadCustomGlossary();
        loadCache();
    }

    private void loadCustomGlossary() {
        try (var input = resourceLoader.getResource(glossaryFile).getInputStream()) {
            Map<String, String> values = objectMapper.readValue(input, new TypeReference<Map<String, String>>() {});
            values.forEach(this::putCustomTerm);
            log.info("Dokkan translation glossary loaded, location={}, size={}", glossaryFile, customGlossary.size());
        } catch (java.io.FileNotFoundException e) {
            log.info("Dokkan translation glossary does not exist, location={}", glossaryFile);
        } catch (Exception e) {
            log.warn("Failed to load Dokkan translation glossary, location={}", glossaryFile, e);
        }
    }

    private void loadCache() {
        Path path = Path.of(cacheFile).toAbsolutePath().normalize();
        if (!Files.isRegularFile(path)) return;
        try {
            JsonNode root = objectMapper.readTree(path.toFile());
            if (root.path("version").asInt() != CACHE_FORMAT_VERSION
                    || root.path("glossaryHash").asInt() != glossaryHash()
                    || !root.path("translations").isObject()) {
                log.info("Ignoring legacy Dokkan translation cache, path={}", path);
                return;
            }
            cache.putAll(objectMapper.convertValue(root.path("translations"), new TypeReference<Map<String, String>>() {}));
            log.info("Dokkan translation cache loaded, path={}, size={}", path, cache.size());
        } catch (Exception e) {
            log.warn("Failed to load Dokkan translation cache, path={}", path, e);
        }
    }

    /** Adds trusted source/Chinese pairs learned from existing localized records. */
    public void registerTrustedTerm(String source, String chinese) {
        if (StringUtils.isAnyBlank(source, chinese) || source.equals(chinese)) return;
        domainGlossary.put(source, ZhConverterUtil.toSimple(chinese));
        cache.keySet().removeIf(cachedSource -> cachedSource.contains(source));
    }

    public String translate(String source) {
        return translateAll(Collections.singletonList(source)).getFirst();
    }

    public List<String> translateAll(Collection<String> sourceTexts) {
        if (sourceTexts == null) return Collections.emptyList();
        List<String> sources = new ArrayList<>(sourceTexts);
        if (!enabled || sources.isEmpty()) return sources;

        ensureDomainGlossary();

        List<String> missing = sources.stream()
                .filter(StringUtils::isNotBlank)
                .filter(source -> trustedTranslation(source) == null)
                .filter(source -> !cache.containsKey(source))
                .distinct()
                .toList();
        if (!missing.isEmpty()) {
            translateMissing(missing);
            persistCache();
        }
        return sources.stream()
                .map(source -> {
                    if (StringUtils.isBlank(source)) return source;
                    String trusted = trustedTranslation(source);
                    return normalizeTerms(trusted == null ? cache.getOrDefault(source, source) : trusted);
                })
                .toList();
    }

    private synchronized void ensureDomainGlossary() {
        if (domainGlossaryLoaded) return;
        try {
            Map<Long, String> categoryNames = categoryRepository.list().stream()
                    .filter(row -> row.getCategoryId() != null && StringUtils.isNotBlank(row.getCategoryName()))
                    .collect(Collectors.toMap(DokkanCategoryPO::getCategoryId, DokkanCategoryPO::getCategoryName,
                            (left, right) -> left));
            Map<Long, String> linkNames = linkRepository.list().stream()
                    .filter(row -> row.getLinkId() != null && StringUtils.isNotBlank(row.getLinkName()))
                    .collect(Collectors.toMap(DokkanLinkPO::getLinkId, DokkanLinkPO::getLinkName,
                            (left, right) -> left));
            registerNamedEntries(fetchNamedEntries(dokkanDbJpClient, "/api/categories"), categoryNames);
            registerNamedEntries(fetchNamedEntries(dokkanDbGlobalClient, "/api/categories"), categoryNames);
            registerNamedEntries(fetchNamedEntries(dokkanDbJpClient, "/api/links"), linkNames);
            registerNamedEntries(fetchNamedEntries(dokkanDbGlobalClient, "/api/links"), linkNames);
            domainGlossaryLoaded = true;
            log.info("Dokkan database translation memory loaded, terms={}", domainGlossary.size());
        } catch (Exception e) {
            // Translation must remain available even if the optional terminology warm-up fails.
            domainGlossaryLoaded = true;
            log.warn("Failed to build Dokkan database translation memory", e);
        }
    }

    private List<NamedEntry> fetchNamedEntries(WebClient sourceClient, String path) {
        List<NamedEntry> rows = sourceClient.get()
                .uri(uri -> uri.path(path).build())
                .retrieve()
                .bodyToFlux(NamedEntry.class)
                .collectList()
                .block(Duration.ofSeconds(30));
        return rows == null ? Collections.emptyList() : rows;
    }

    private void registerNamedEntries(List<NamedEntry> entries, Map<Long, String> localizedNames) {
        for (NamedEntry entry : entries) {
            if (entry != null) registerTrustedTerm(entry.name(), localizedNames.get(entry.id()));
        }
    }

    private String trustedTranslation(String source) {
        String value = customGlossary.get(source);
        if (value != null) return value;
        value = domainGlossary.get(source);
        return value == null ? TERM_GLOSSARY.get(source) : value;
    }

    private void translateMissing(List<String> missing) {
        List<String> batch = new ArrayList<>();
        int chars = 0;
        for (String source : missing) {
            if (!batch.isEmpty() && chars + source.length() > MAX_BATCH_CHARS) {
                translateBatch(batch);
                batch.clear();
                chars = 0;
            }
            batch.add(source);
            chars += source.length();
        }
        if (!batch.isEmpty()) translateBatch(batch);
    }

    private void translateBatch(List<String> sources) {
        boolean acquired = false;
        List<String> retrySources = new ArrayList<>(sources);
        try {
            translationPermits.acquire();
            acquired = true;
            ProtectedText protectedText = buildProtectedText(sources);
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("client", "gtx");
            form.add("sl", detectSourceLanguage(sources));
            form.add("tl", "zh-CN");
            form.add("dt", "t");
            form.add("dj", "1");
            form.add("q", protectedText.text());
            String response = translationClient.post()
                    .uri("/translate_a/single")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(form))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(30));
            Map<Integer, String> translated = parseResponse(response, protectedText);
            for (int i = 0; i < sources.size(); i++) {
                String value = translated.get(i);
                if (StringUtils.isNotBlank(value)) {
                    cache.put(sources.get(i), normalizeTerms(value));
                    retrySources.remove(sources.get(i));
                }
            }
            log.info("Dokkan text translation completed, requested={}, translated={}", sources.size(), translated.size());
        } catch (Exception e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            log.warn("Dokkan text translation failed; source text will be retained, count={}", sources.size(), e);
        } finally {
            if (acquired) translationPermits.release();
        }
        if (sources.size() > 1 && !retrySources.isEmpty()) {
            log.info("Retrying incomplete Dokkan translations individually, count={}", retrySources.size());
            retrySources.forEach(source -> translateBatch(Collections.singletonList(source)));
        }
    }

    private ProtectedText buildProtectedText(List<String> sources) {
        StringBuilder text = new StringBuilder();
        Map<String, String> tokens = new LinkedHashMap<>();
        Map<String, String> sourceTokenKeys = new LinkedHashMap<>();
        Map<Integer, List<String>> rowTokens = new LinkedHashMap<>();
        Map<String, String> glossary = glossarySnapshot();
        Pattern protectedTerms = buildProtectedTermPattern(glossary);
        int tokenIndex = 0;
        for (int i = 0; i < sources.size(); i++) {
            Matcher matcher = protectedTerms.matcher(sources.get(i));
            StringBuilder protectedText = new StringBuilder();
            while (matcher.find()) {
                String sourceToken = matcher.group();
                String key = sourceTokenKeys.get(sourceToken);
                if (key == null) {
                    key = "__DOKKAN_TOKEN_" + tokenIndex++ + "__";
                    sourceTokenKeys.put(sourceToken, key);
                    tokens.put(key, glossary.getOrDefault(sourceToken, sourceToken));
                }
                List<String> expectedTokens = rowTokens.computeIfAbsent(i, ignored -> new ArrayList<>());
                if (!expectedTokens.contains(key)) expectedTokens.add(key);
                matcher.appendReplacement(protectedText, Matcher.quoteReplacement(key));
            }
            matcher.appendTail(protectedText);
            text.append(rowMarker(i)).append('\n').append(protectedText).append('\n');
        }
        text.append("__DOKKAN_ROW_END__");
        return new ProtectedText(text.toString(), tokens, rowTokens, sources.size());
    }

    private Map<Integer, String> parseResponse(String response, ProtectedText protectedText) throws Exception {
        JsonNode root = objectMapper.readTree(response);
        StringBuilder translatedText = new StringBuilder();
        for (JsonNode sentence : root.path("sentences")) {
            translatedText.append(sentence.path("trans").asText(""));
        }
        Map<Integer, String> result = new LinkedHashMap<>();
        String translated = translatedText.toString();
        for (int i = 0; i < protectedText.size(); i++) {
            String startMarker = rowMarker(i);
            String endMarker = i + 1 < protectedText.size() ? rowMarker(i + 1) : "__DOKKAN_ROW_END__";
            int start = translated.indexOf(startMarker);
            int end = start < 0 ? -1 : translated.indexOf(endMarker, start + startMarker.length());
            if (start < 0 || end < 0) continue;
            String value = translated.substring(start + startMarker.length(), end).trim();
            List<String> expectedTokens = protectedText.rowTokens().getOrDefault(i, Collections.emptyList());
            boolean complete = true;
            for (String token : expectedTokens) {
                if (!value.contains(token)) {
                    complete = false;
                    log.warn("Incomplete Dokkan translation row, index={}, missingToken={}", i, token);
                    break;
                }
            }
            if (!complete) continue;
            for (String token : expectedTokens) value = value.replace(token, protectedText.tokens().get(token));
            result.put(i, value);
        }
        return result;
    }

    private String rowMarker(int index) {
        return "__DOKKAN_ROW_" + index + "__";
    }

    private String detectSourceLanguage(List<String> sources) {
        return sources.stream().filter(Objects::nonNull).anyMatch(text -> JAPANESE_KANA.matcher(text).find())
                ? "ja" : "en";
    }

    private String normalizeTerms(String translated) {
        return ZhConverterUtil.toSimple(translated)
                .replace("攻击力", "ATK")
                .replace("防御力", "DEF")
                .replace("生命值", "HP")
                .replaceAll("(?i)\\bKi\\b", "气力")
                .replaceAll("(?m)^\\?", "・")
                .replace("超高概率", "超高机率")
                .replace("高概率", "高机率")
                .replace("中概率", "中等机率")
                .replace("几率", "机率")
                .replace("概率", "机率")
                .replace("大幅度提升", "大幅提升")
                .replaceAll("(\\d+)转", "$1回合")
                .replaceAll("(\\d+)[ \\t]*个?[ \\t]*回合", "$1回合")
                .replaceAll("ATK[ \\t]*(?:和|与|&)[ \\t]*DEF", "ATK与DEF")
                .replaceAll("HP[ \\t]*[、,，][ \\t]*ATK与DEF", "HP、ATK、DEF")
                .replaceAll("[ \\t]+([，。；：、%])", "$1")
                .replace("闪避", "回避")
                .replace("伤害减少率", "伤害减轻率")
                .replace("伤害减免率", "伤害减轻率")
                .replace("防范一切攻击", "防御所有攻击")
                .replace("类别类别", "类别");
    }

    private void persistCache() {
        synchronized (cacheWriteLock) {
            Path target = Path.of(cacheFile).toAbsolutePath().normalize();
            Path temp = target.resolveSibling(target.getFileName() + ".tmp");
            try {
                Files.createDirectories(Objects.requireNonNull(target.getParent()));
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("version", CACHE_FORMAT_VERSION);
                payload.put("glossaryHash", glossaryHash());
                payload.put("translations", cache.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                (left, right) -> left, LinkedHashMap::new)));
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(temp.toFile(), payload);
                try {
                    Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } catch (Exception ignored) {
                    Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (Exception e) {
                log.warn("Failed to persist Dokkan translation cache, path={}", target, e);
            }
        }
    }

    private record ProtectedText(String text, Map<String, String> tokens,
                                 Map<Integer, List<String>> rowTokens, int size) {}
    private record NamedEntry(Long id, String name) {}

    private Map<String, String> glossarySnapshot() {
        Map<String, String> result = new LinkedHashMap<>(TERM_GLOSSARY);
        result.putAll(domainGlossary);
        result.putAll(customGlossary);
        return result;
    }

    private void putCustomTerm(String source, String target) {
        if (StringUtils.isAnyBlank(source, target)) return;
        customGlossary.put(source, ZhConverterUtil.toSimple(target));
        cache.remove(source);
    }

    private int glossaryHash() {
        return 31 * TERM_GLOSSARY.hashCode() + customGlossary.hashCode();
    }

    private static Pattern buildProtectedTermPattern(Map<String, String> glossary) {
        String glossaryTerms = glossary.keySet().stream()
                .filter(StringUtils::isNotBlank)
                .sorted(Comparator.comparingInt(String::length).reversed())
                .map(Pattern::quote)
                .collect(Collectors.joining("|"));
        return Pattern.compile("\\{[^{}]+}|(?i:\\b(?:LR|UR|SSR|HERO|BOSS)\\b)|" + glossaryTerms);
    }

    private static Map<String, String> buildGlossary() {
        Map<String, String> terms = new LinkedHashMap<>();
        terms.put("ダメージ軽減率", "伤害减轻率");
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
        terms.put("ターン", "回合");
        terms.put("味方全員", "我方全体");
        terms.put("自身", "自身");
        terms.put("敵", "敌人");
        terms.put("回避率", "回避率");
        terms.put("会心の一撃", "奋力一击");
        terms.put("必ず追加攻撃", "必可发动追加攻击");
        terms.put("全属性に効果抜群で攻撃", "对全属性造成属性克制伤害");
        terms.put("全ての攻撃をガード", "防御所有攻击");
        terms.put("必殺技を封じる", "封锁必杀技");
        terms.put("気絶させる", "使其晕眩");
        terms.put("極系", "极系");
        terms.put("超系", "超系");
        return Collections.unmodifiableMap(terms);
    }
}
