package com.hb.dokkan.service.facade;

import com.hb.dokkan.common.constants.CardSyncConstants;
import com.hb.dokkan.common.domain.dto.data.dokkandb.DokkanDbCardDTO;
import com.hb.dokkan.common.domain.dto.data.dokkandb.DokkanDbCardStatsDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiCardDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiCategoryDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiLinkDTO;
import com.hb.dokkan.config.http.HttpPoolProperties;
import com.hb.dokkan.config.http.RetryTemplate;
import com.hb.dokkan.config.thread.DokkanThreadPoolExecutor;
import com.hb.dokkan.service.client.DokkanDbClient;
import com.hb.dokkan.service.convert.DokkanDbCardAssembler;
import com.hb.dokkan.translation.BusinessTranslationService;
import com.hb.dokkan.translation.domain.TranslationBatchRequest;
import com.hb.dokkan.translation.domain.TranslationItem;
import com.hb.dokkan.translation.domain.TranslationProviderType;
import com.hb.dokkan.translation.domain.TranslationScene;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

/**
 * @Description DokkanDB外部数据门面
 * @Author stargazer
 * @Date 2026/8/15 21:20
 **/
@Slf4j
@Service
public class DokkanDbFacade {

    @Resource
    private HttpPoolProperties httpPoolProperties;

    @Resource
    private DokkanDbCardAssembler assembler;

    @Resource
    private BusinessTranslationService businessTranslationService;

    @Resource
    private DokkanDbClient dokkanDbClient;

    @Resource
    private DokkanThreadPoolExecutor dokkanThreadPoolExecutor;

    /**
     * 获取英文版最近卡片目录。
     *
     * @param size 查询数量
     * @return 最近卡片目录
     */
    public List<DokkanDbCardDTO> getRecentCatalog(int size) {
        return RetryTemplate.executeWithRetrySliently(() -> {
            // 卡片主数据统一使用 Global 英文源，后续再走统一翻译链路。
            Map<Long, DokkanDbCardDTO> rows = new LinkedHashMap<>();
            for (int chunk = CardSyncConstants.DOKKAN_DB_DEFAULT_CHUNK;
                 chunk <= CardSyncConstants.DOKKAN_DB_CATALOG_MAX_CHUNK; chunk++) {
                List<DokkanDbCardDTO> chunkRows = dokkanDbClient.listRecentCatalogFromGlobal(chunk, size);
                if (CollectionUtils.isEmpty(chunkRows)) {
                    break;
                }
                chunkRows.stream()
                        .filter(row -> Objects.nonNull(row.getId()))
                        .forEach(row -> rows.putIfAbsent(row.getId(), row));
                if (chunkRows.size() < size) {
                    break;
                }
            }
            return new ArrayList<>(rows.values());
        }, httpPoolProperties.getRetry(), "getDokkanDbRecentCatalog");
    }

    /**
     * 获取英文版单张卡片详情并转换为 wiki 持久化模型。
     *
     * @param cardId 卡片 ID
     * @return wiki 卡片模型，查不到时返回 null
     */
    public WikiCardDTO getCard(Long cardId) {
        return getCard(cardId, false);
    }

    /**
     * 获取英文版单张卡片详情并转换为 wiki 持久化模型。
     *
     * @param cardId           卡片 ID
     * @param forceRetranslate 是否强制绕过旧翻译缓存重新翻译
     * @return wiki 卡片模型，查不到时返回 null
     */
    public WikiCardDTO getCard(Long cardId, boolean forceRetranslate) {
        return RetryTemplate.executeWithRetrySliently(() -> {
            CardSourceData raw = fetchRawCard(cardId);
            return raw == null ? null : assembler.assemble(raw.card(), raw.stats(), forceRetranslate);
        }, httpPoolProperties.getRetry(), "getDokkanDbCard-cardId:" + cardId);
    }

    /**
     * 获取单张卡片原始数据，供已受并发控制的调用方执行批量翻译。
     *
     * @param cardId 卡片 ID
     * @return 原始卡片及数值数据，获取失败时返回 null
     */
    public CardSourceData fetchCardSource(Long cardId) {
        return fetchRawCard(cardId);
    }

    /**
     * 将原始卡片数据集合统一翻译并组装为 wiki 模型。
     *
     * @param sources          原始卡片数据
     * @param forceRetranslate 是否强制重翻
     * @return wiki 卡片模型
     */
    public List<WikiCardDTO> assembleCards(List<CardSourceData> sources, boolean forceRetranslate) {
        if (CollectionUtils.isEmpty(sources)) {
            return List.of();
        }
        List<DokkanDbCardDTO> cards = sources.stream().map(CardSourceData::card).toList();
        List<DokkanDbCardStatsDTO> stats = sources.stream().map(CardSourceData::stats).toList();
        return assembler.assembleAll(cards, stats, forceRetranslate);
    }

    /**
     * 获取单张卡片的原始详情和数值，远程失败時返回 null。
     *
     * @param cardId 卡片 ID
     * @return 原始卡片数据
     */
    private CardSourceData fetchRawCard(Long cardId) {
        return RetryTemplate.executeWithRetrySliently(() -> {
            List<DokkanDbCardDTO> cards = dokkanDbClient.listCardsFromGlobal(cardId);
            if (CollectionUtils.isEmpty(cards)) {
                return null;
            }
            List<DokkanDbCardStatsDTO> stats = dokkanDbClient.listCardStats(DokkanDbClient.Source.GLOBAL, cardId);
            return new CardSourceData(cards.getFirst(), CollectionUtils.isEmpty(stats) ? null : stats.getFirst());
        }, httpPoolProperties.getRetry(), "getDokkanDbCard-cardId:" + cardId);
    }

    /**
     * 获取并发详情查询结果，单卡异常不影响同批其他卡片。
     *
     * @param future 卡片详情查询任务
     * @return 原始卡片数据，失败时返回 null
     */
    private CardSourceData joinCardResult(CompletableFuture<CardSourceData> future) {
        try {
            return future.join();
        } catch (CompletionException e) {
            log.warn("DokkanDB card batch fetch failed", e.getCause() == null ? e : e.getCause());
            return null;
        }
    }

    /** 保留单卡原始数据与数值的关联。 */
    public record CardSourceData(DokkanDbCardDTO card, DokkanDbCardStatsDTO stats) {
    }

    /**
     * 批量获取卡片详情。
     *
     * @param cardIds 卡片 ID 列表
     * @return wiki 卡片模型列表
     */
    public List<WikiCardDTO> getCards(List<Long> cardIds) {
        if (CollectionUtils.isEmpty(cardIds)) {
            return List.of();
        }
        List<CompletableFuture<CardSourceData>> futures = cardIds.stream()
                .map(cardId -> CompletableFuture.supplyAsync(() -> fetchRawCard(cardId), dokkanThreadPoolExecutor))
                .toList();
        List<CardSourceData> rawCards = futures.stream()
                .map(this::joinCardResult)
                .filter(Objects::nonNull)
                .toList();
        return assembleCards(rawCards, false);
    }

    /**
     * 获取英文版分类列表并翻译分类名称。
     *
     * @return 分类列表
     */
    public List<WikiCategoryDTO> getCategories() {
        // 分类名称统一使用 Global 英文源，保证术语进入翻译链路前语言一致。
        List<WikiCategoryDTO> rows = dokkanDbClient.listCategoriesFromGlobal();
        List<String> translated = businessTranslationService.translate(new TranslationBatchRequest(
                        TranslationScene.CATEGORY_NAME, TranslationProviderType.GOOGLE,
                        rows.stream().map(row -> new TranslationItem(String.valueOf(row.getCategoryId()),
                                row.getCategoryName())).toList(), false))
                .stream().map(result -> result.translatedText()).toList();
        for (int i = 0; i < rows.size(); i++) {
            String englishName = rows.get(i).getCategoryName();
            rows.get(i).setCategoryName(translated.get(i));
            rows.get(i).setCategoryNameEn(englishName);
        }
        return rows;
    }

    /**
     * 获取英文版链接列表并翻译链接名称与效果描述。
     *
     * @return 链接列表
     */
    public List<WikiLinkDTO> getLinks() {
        // 链接名称和效果统一使用 Global 英文源，便于按卡牌模板翻译为中文。
        List<WikiLinkDTO> rows = dokkanDbClient.listLinksFromGlobal();
        Map<Long, DokkanDbClient.LinkEffect> effects = dokkanDbClient.listLinkEffects(
                        rows.stream().map(WikiLinkDTO::getLinkId).toList())
                .stream()
                .filter(effect -> effect.id() != null)
                .collect(Collectors.toMap(DokkanDbClient.LinkEffect::id, Function.identity(), (left, right) -> left));
        List<String> sourceTexts = new java.util.ArrayList<>();
        for (WikiLinkDTO row : rows) {
            DokkanDbClient.LinkEffect effect = effects.get(row.getLinkId());
            sourceTexts.add(row.getLinkName());
            sourceTexts.add(effect == null ? null : effect.description());
            sourceTexts.add(effect == null ? null : effect.description10());
        }
        List<String> translated = businessTranslationService.translate(new TranslationBatchRequest(
                        TranslationScene.LINK_TEXT, TranslationProviderType.GOOGLE,
                        java.util.stream.IntStream.range(0, sourceTexts.size())
                                .mapToObj(index -> new TranslationItem(String.valueOf(index), sourceTexts.get(index)))
                                .toList(), false))
                .stream().map(result -> result.translatedText()).toList();
        for (int i = 0; i < rows.size(); i++) {
            int offset = i * CardSyncConstants.LINK_TRANSLATION_TEXT_COUNT;
            rows.get(i).setLinkName(translated.get(offset + CardSyncConstants.LINK_NAME_TEXT_OFFSET));
            rows.get(i).setLevel1Description(translated.get(offset + CardSyncConstants.LINK_LEVEL_1_TEXT_OFFSET));
            rows.get(i).setLevel10Description(translated.get(offset + CardSyncConstants.LINK_LEVEL_10_TEXT_OFFSET));
        }
        return rows;
    }
}
