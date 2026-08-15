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

import java.util.Collections;
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
     * 获取最近卡片目录，Global 优先，空结果时降级 JP。
     *
     * @param size 查询数量
     * @return 最近卡片目录
     */
    public List<DokkanDbCardDTO> getRecentCatalog(int size) {
        return RetryTemplate.executeWithRetrySliently(() -> {
            // Global 优先，失败或空结果时使用 JP 数据源兜底。
            List<DokkanDbCardDTO> rows = dokkanDbClient.listRecentCatalogFromGlobal(size);
            if (rows == null || rows.isEmpty()) {
                rows = dokkanDbClient.listRecentCatalogFromJp(size);
            }
            return rows == null ? Collections.emptyList() : rows;
        }, httpPoolProperties.getRetry(), "getDokkanDbRecentCatalog");
    }

    /**
     * 获取单张卡片详情并转换为 wiki 持久化模型。
     *
     * @param cardId 卡片 ID
     * @return wiki 卡片模型，查不到时返回 null
     */
    public WikiCardDTO getCard(Long cardId) {
        return RetryTemplate.executeWithRetrySliently(() -> {
            // 先查 Global，查不到再查 JP，并记录最终命中的数据源用于查询数值。
            DokkanDbClient.Source selectedSource = DokkanDbClient.Source.GLOBAL;
            List<DokkanDbCardDTO> cards = dokkanDbClient.listCardsFromGlobal(cardId);
            if (cards == null || cards.isEmpty()) {
                selectedSource = DokkanDbClient.Source.JP;
                cards = dokkanDbClient.listCardsFromJp(cardId);
            }
            if (cards == null || cards.isEmpty()) {
                return null;
            }
            List<DokkanDbCardStatsDTO> stats = dokkanDbClient.listCardStats(selectedSource, cardId);
            return assembler.assemble(cards.getFirst(), stats == null || stats.isEmpty() ? null : stats.getFirst());
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
     * 获取分类列表并翻译分类名称。
     *
     * @return 分类列表
     */
    public List<WikiCategoryDTO> getCategories() {
        List<WikiCategoryDTO> rows = dokkanDbClient.listCategoriesFromGlobal();
        if (rows.isEmpty()) {
            rows = dokkanDbClient.listCategoriesFromJp();
        }
        List<String> translated = translationService.translateAll(rows.stream()
                .map(WikiCategoryDTO::getCategoryName)
                .toList());
        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).setCategoryName(translated.get(i));
        }
        return rows;
    }

    /**
     * 获取链接列表并翻译链接名称与效果描述。
     *
     * @return 链接列表
     */
    public List<WikiLinkDTO> getLinks() {
        List<WikiLinkDTO> rows = dokkanDbClient.listLinksFromGlobal();
        if (rows.isEmpty()) {
            rows = dokkanDbClient.listLinksFromJp();
        }
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
