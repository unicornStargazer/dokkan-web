package com.hb.dokkan.service.facade;

import com.hb.dokkan.common.constants.CardSyncConstants;
import com.hb.dokkan.common.domain.dto.data.dokkandb.DokkanDbCardDTO;
import com.hb.dokkan.common.domain.dto.data.dokkandb.DokkanDbCardStatsDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiCardDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiCategoryDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiLinkDTO;
import com.hb.dokkan.config.http.HttpPoolProperties;
import com.hb.dokkan.config.http.RetryTemplate;
import com.hb.dokkan.service.client.DokkanDbClient;
import com.hb.dokkan.service.convert.DokkanDbCardAssembler;
import com.hb.dokkan.service.translation.DokkanTranslationService;
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
    private DokkanTranslationService translationService;

    @Resource
    private DokkanDbClient dokkanDbClient;

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
            // 卡片详情、被动、队长技等模板统一取 Global 英文源，避免新增卡走日文导致翻译质量不稳定。
            List<DokkanDbCardDTO> cards = dokkanDbClient.listCardsFromGlobal(cardId);
            if (cards == null || cards.isEmpty()) {
                return null;
            }
            List<DokkanDbCardStatsDTO> stats = dokkanDbClient.listCardStats(DokkanDbClient.Source.GLOBAL, cardId);
            return assembler.assemble(cards.getFirst(), stats == null || stats.isEmpty() ? null : stats.getFirst(),
                    forceRetranslate);
        }, httpPoolProperties.getRetry(), "getDokkanDbCard-cardId:" + cardId);
    }

    /**
     * 批量获取卡片详情。
     *
     * @param cardIds 卡片 ID 列表
     * @return wiki 卡片模型列表
     */
    public List<WikiCardDTO> getCards(List<Long> cardIds) {
        return cardIds.parallelStream()
                .map(this::getCard)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * 获取英文版分类列表并翻译分类名称。
     *
     * @return 分类列表
     */
    public List<WikiCategoryDTO> getCategories() {
        // 分类名称统一使用 Global 英文源，保证术语进入翻译链路前语言一致。
        List<WikiCategoryDTO> rows = dokkanDbClient.listCategoriesFromGlobal();
        List<String> translated = translationService.translateAll(rows.stream()
                .map(WikiCategoryDTO::getCategoryName)
                .toList());
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
        List<String> translated = translationService.translateAll(sourceTexts);
        for (int i = 0; i < rows.size(); i++) {
            int offset = i * CardSyncConstants.LINK_TRANSLATION_TEXT_COUNT;
            rows.get(i).setLinkName(translated.get(offset + CardSyncConstants.LINK_NAME_TEXT_OFFSET));
            rows.get(i).setLevel1Description(translated.get(offset + CardSyncConstants.LINK_LEVEL_1_TEXT_OFFSET));
            rows.get(i).setLevel10Description(translated.get(offset + CardSyncConstants.LINK_LEVEL_10_TEXT_OFFSET));
        }
        return rows;
    }
}
