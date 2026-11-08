package com.hb.dokkan.service.job.sync;

import com.hb.dokkan.common.domain.bo.data.KxWikiCardBO;
import com.hb.dokkan.common.domain.dto.data.kx.KxWikiCardListItemDTO;
import com.hb.dokkan.common.domain.dto.data.kx.KxWikiPageResponse;
import com.hb.dokkan.common.domain.dto.data.kx.KxWikiStatusDTO;
import com.hb.dokkan.common.domain.po.mysql.cards.CardPO;
import com.hb.dokkan.config.data.WikiCardFilter;
import com.hb.dokkan.config.kx.KxWikiProperties;
import com.hb.dokkan.infrastructure.mysql.sync.KxWikiSyncRepository;
import com.hb.dokkan.service.client.KxWikiClient;
import com.hb.dokkan.service.job.sync.strategy.context.WikiContext;
import com.hb.dokkan.service.job.sync.strategy.strategies.WikiCardStrategy;
import org.junit.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * @Description 验证强制回填绕过版本和候选短路，并逐卡处理及隔离失败
 * @Author Codex
 * @Date 2026/11/8
 **/
public class WikiCardFullRefreshTest {
    /**
     * 即使源版本没有变化，也逐张重新抓取全部候选而不缓存整库详情。
     **/
    @Test
    public void shouldRefetchAllCardsWhenVersionIsUnchanged() throws Exception {
        WikiCardStrategy strategy = strategy();
        WikiContext context = new WikiContext();
        context.setForce(true);
        List<Long> processed = new ArrayList<>();
        strategy.execute(context, aggregate -> {
            Long cardId = aggregate.getCards().getFirst().getCardId();
            processed.add(cardId);
            strategy.markCardComplete(cardId);
        });
        assertEquals(List.of(1033941L, 1031181L), processed);
        assertEquals("same-version", context.getSourceUpdatedAt());
        assertTrue(context.getCardData().isEmpty());
        assertTrue(context.getFailedCardIds().isEmpty());
    }

    /**
     * 单卡写入失败后继续处理后续卡片，并在上下文保留失败 ID。
     **/
    @Test
    public void shouldContinueAfterOneCardFailsToPersist() throws Exception {
        WikiCardStrategy strategy = strategy();
        WikiContext context = new WikiContext();
        context.setForce(true);
        List<Long> processed = new ArrayList<>();
        strategy.execute(context, aggregate -> {
            Long cardId = aggregate.getCards().getFirst().getCardId();
            if (cardId.equals(1033941L)) {
                throw new IllegalStateException("Simulated persistence failure");
            }
            processed.add(cardId);
            strategy.markCardComplete(cardId);
        });
        assertEquals(List.of(1031181L), processed);
        assertEquals(List.of(1033941L), context.getFailedCardIds());
    }

    /**
     * 使用受控源站和相同版本的仓储，保留真实候选扫描和强制模式逻辑。
     **/
    private WikiCardStrategy strategy() throws Exception {
        WikiCardStrategy strategy = new WikiCardStrategy() {
            /**
             * 构建最小聚合，以验证逐卡回调而不访问外部详情。
             **/
            @Override
            public KxWikiCardBO fetchCard(Long cardId, boolean manual) {
                CardPO card = new CardPO();
                card.setCardId(cardId);
                KxWikiCardBO aggregate = new KxWikiCardBO();
                aggregate.getCards().add(card);
                return aggregate;
            }
        };
        inject(strategy, "properties", new KxWikiProperties());
        inject(strategy, "cardFilter", new WikiCardFilter());
        inject(strategy, "syncRepository", new KxWikiSyncRepository(null) {
            /**
             * 模拟上一次已经成功处理过相同源版本。
             **/
            @Override
            public String findLastUpdatedAt(String syncKey) {
                return "same-version";
            }
        });
        inject(strategy, "client", new KxWikiClient(WebClient.builder().build(), new KxWikiProperties()) {
            /**
             * 源版本不变化，必须由强制模式绕过短路。
             **/
            @Override
            public Optional<KxWikiStatusDTO> getStatus() {
                KxWikiStatusDTO status = new KxWikiStatusDTO();
                status.setUpdatedAt("same-version");
                return Optional.of(status);
            }

            /**
             * 返回两张有效卡片，未注入卡片仓储以确保强制模式不查增量标记。
             **/
            @Override
            public KxWikiPageResponse<KxWikiCardListItemDTO> listCards(int page) {
                KxWikiCardListItemDTO first = new KxWikiCardListItemDTO();
                first.setId(1033941L);
                first.setRarity(5);
                KxWikiCardListItemDTO second = new KxWikiCardListItemDTO();
                second.setId(1031181L);
                second.setRarity(4);
                KxWikiPageResponse<KxWikiCardListItemDTO> response = new KxWikiPageResponse<>();
                response.setPages(1);
                response.setItems(List.of(first, second));
                return response;
            }
        });
        return strategy;
    }

    /**
     * 为不启动 Spring 的策略测试注入受控依赖。
     **/
    private void inject(WikiCardStrategy target, String name, Object value) throws Exception {
        Field field = WikiCardStrategy.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
