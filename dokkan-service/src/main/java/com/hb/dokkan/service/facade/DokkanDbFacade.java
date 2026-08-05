package com.hb.dokkan.service.facade;

import com.hb.dokkan.common.domain.dto.data.dokkandb.DokkanDbCardDTO;
import com.hb.dokkan.common.domain.dto.data.dokkandb.DokkanDbCardStatsDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiCardDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiCategoryDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiLinkDTO;
import com.hb.dokkan.config.http.HttpPoolProperties;
import com.hb.dokkan.config.http.RetryTemplate;
import com.hb.dokkan.service.convert.DokkanDbCardAssembler;
import com.hb.dokkan.service.translation.DokkanTranslationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Structured DokkanDB client. Global English is preferred and JP is the fallback. */
@Slf4j
@Service
public class DokkanDbFacade {
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    @Resource
    @Qualifier("dokkanDbWebClient")
    private WebClient jpClient;

    @Resource
    @Qualifier("dokkanDbGlobalWebClient")
    private WebClient globalClient;

    @Resource
    private HttpPoolProperties httpPoolProperties;

    @Resource
    private DokkanDbCardAssembler assembler;

    @Resource
    private DokkanTranslationService translationService;

    public List<DokkanDbCardDTO> getRecentCatalog(int size) {
        return RetryTemplate.executeWithRetrySliently(() -> {
            List<DokkanDbCardDTO> rows;
            try {
                rows = fetchCatalog(globalClient, size);
            } catch (Exception e) {
                log.warn("DokkanDB Global catalog unavailable, falling back to JP", e);
                rows = Collections.emptyList();
            }
            if (rows == null || rows.isEmpty()) rows = fetchCatalog(jpClient, size);
            return rows == null ? Collections.emptyList() : rows;
        }, httpPoolProperties.getRetry(), "getDokkanDbRecentCatalog");
    }

    public WikiCardDTO getCard(Long cardId) {
        return RetryTemplate.executeWithRetrySliently(() -> {
            WebClient selectedClient = globalClient;
            List<DokkanDbCardDTO> cards;
            try {
                cards = fetchCard(globalClient, cardId);
            } catch (Exception e) {
                log.warn("DokkanDB Global card unavailable, falling back to JP, cardId:{}", cardId, e);
                cards = Collections.emptyList();
            }
            if (cards == null || cards.isEmpty()) {
                selectedClient = jpClient;
                cards = fetchCard(jpClient, cardId);
            }
            if (cards == null || cards.isEmpty()) return null;
            List<DokkanDbCardStatsDTO> stats = selectedClient.get()
                    .uri(uri -> uri.path("/api/card-stats-with-hp").queryParam("p_card_id", cardId).build())
                    .retrieve()
                    .bodyToFlux(DokkanDbCardStatsDTO.class)
                    .collectList()
                    .block(REQUEST_TIMEOUT);
            return assembler.assemble(cards.getFirst(), stats == null || stats.isEmpty() ? null : stats.getFirst());
        }, httpPoolProperties.getRetry(), "getDokkanDbCard-cardId:" + cardId);
    }

    public List<WikiCardDTO> getCards(List<Long> cardIds) {
        return cardIds.parallelStream()
                .map(this::getCard)
                .filter(Objects::nonNull)
                .toList();
    }

    public List<WikiCategoryDTO> getCategories() {
        List<WikiCategoryDTO> rows = getNamedRows(globalClient, "/api/categories", WikiCategoryDTO.class);
        if (rows.isEmpty()) rows = getNamedRows(jpClient, "/api/categories", WikiCategoryDTO.class);
        List<String> translated = translationService.translateAll(rows.stream()
                .map(WikiCategoryDTO::getCategoryName).toList());
        for (int i = 0; i < rows.size(); i++) rows.get(i).setCategoryName(translated.get(i));
        return rows;
    }

    public List<WikiLinkDTO> getLinks() {
        List<WikiLinkDTO> rows = getNamedRows(globalClient, "/api/links", WikiLinkDTO.class);
        if (rows.isEmpty()) rows = getNamedRows(jpClient, "/api/links", WikiLinkDTO.class);
        Map<Long, LinkEffect> effects = getLinkEffects(rows.stream().map(WikiLinkDTO::getLinkId).toList()).stream()
                .filter(effect -> effect.id() != null)
                .collect(Collectors.toMap(LinkEffect::id, Function.identity(), (left, right) -> left));
        List<String> sourceTexts = new java.util.ArrayList<>();
        for (WikiLinkDTO row : rows) {
            LinkEffect effect = effects.get(row.getLinkId());
            sourceTexts.add(row.getLinkName());
            sourceTexts.add(effect == null ? null : effect.description());
            sourceTexts.add(effect == null ? null : effect.description10());
        }
        List<String> translated = translationService.translateAll(sourceTexts);
        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).setLinkName(translated.get(i * 3));
            rows.get(i).setLevel1Description(translated.get(i * 3 + 1));
            rows.get(i).setLevel10Description(translated.get(i * 3 + 2));
        }
        return rows;
    }

    private List<LinkEffect> getLinkEffects(List<Long> linkIds) {
        String ids = linkIds.stream().filter(Objects::nonNull).map(String::valueOf).collect(Collectors.joining(","));
        if (ids.isEmpty()) return Collections.emptyList();
        return RetryTemplate.executeWithRetrySliently(() -> {
            List<LinkEffect> rows = globalClient.get()
                    .uri(uri -> uri.path("/api/link-skill-effects-by-ids").queryParam("ids", ids).build())
                    .retrieve()
                    .bodyToFlux(LinkEffect.class)
                    .collectList()
                    .block(REQUEST_TIMEOUT);
            return rows == null ? Collections.emptyList() : rows;
        }, httpPoolProperties.getRetry(), "getDokkanDbLinkEffects");
    }

    private List<DokkanDbCardDTO> fetchCatalog(WebClient sourceClient, int size) {
        return sourceClient.get()
                .uri(uri -> uri.path("/api/cards-catalog-with-transformations")
                        .queryParam("chunk", 1)
                        .queryParam("chunk_size", size)
                        .build())
                .retrieve()
                .bodyToFlux(DokkanDbCardDTO.class)
                .collectList()
                .block(REQUEST_TIMEOUT);
    }

    private List<DokkanDbCardDTO> fetchCard(WebClient sourceClient, Long cardId) {
        return sourceClient.get()
                .uri(uri -> uri.path("/api/card").queryParam("code", cardId).build())
                .retrieve()
                .bodyToFlux(DokkanDbCardDTO.class)
                .collectList()
                .block(REQUEST_TIMEOUT);
    }

    private <T> List<T> getNamedRows(WebClient sourceClient, String path, Class<T> type) {
        return RetryTemplate.executeWithRetrySliently(() -> {
            List<T> rows = sourceClient.get()
                    .uri(uri -> uri.path(path).build())
                    .retrieve()
                    .bodyToFlux(type)
                    .collectList()
                    .block(REQUEST_TIMEOUT);
            return rows == null ? Collections.emptyList() : rows;
        }, httpPoolProperties.getRetry(), "getDokkanDbNamedRows-path:" + path);
    }

    private record LinkEffect(Long id, String description,
                              @com.fasterxml.jackson.annotation.JsonProperty("description_10") String description10) {}
}
