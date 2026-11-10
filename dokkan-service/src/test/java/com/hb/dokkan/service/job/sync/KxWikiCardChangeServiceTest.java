package com.hb.dokkan.service.job.sync;

import com.hb.dokkan.common.constants.CardSyncConstants;
import com.hb.dokkan.common.domain.bo.data.KxWikiCardBO;
import com.hb.dokkan.common.domain.bo.data.KxWikiCardChangesBO;
import com.hb.dokkan.common.domain.dto.data.kx.*;
import com.hb.dokkan.common.domain.po.mysql.cards.CardPO;
import com.hb.dokkan.config.data.WikiCardFilter;
import com.hb.dokkan.config.kx.KxWikiProperties;
import com.hb.dokkan.infrastructure.mysql.cards.DokkanCardRepository;
import com.hb.dokkan.infrastructure.mysql.sync.KxWikiSyncRepository;
import com.hb.dokkan.service.client.KxWikiClient;
import com.hb.dokkan.service.job.sync.strategy.context.WikiContext;
import com.hb.dokkan.service.job.sync.strategy.strategies.WikiCardStrategy;
import org.junit.Test;
import org.springframework.web.reactive.function.client.WebClient;
import java.lang.reflect.Field;
import java.util.*;
import static org.junit.Assert.*;

/**
 * @Description 验证更新信号、时间线追溯、重启水位与失败重试
 * @Author Codex
 * @Date 2026/11/10
 **/
public class KxWikiCardChangeServiceTest {
    /**
     * 首次只读取时间线首页，并将两个入口的新卡和极限记录合并去重。
     **/
    @Test
    public void shouldMergeLatestBatchAndBootstrapFirstTimelinePage() {
        Fixture fixture = new Fixture();
        fixture.latest.setNewUnits(List.of(card(1L), card(2L)));
        fixture.latest.setNewEzas(List.of(card(2L), card(3L)));
        fixture.pages.put(1, timeline(2, group("2026-11-10", 3L, 4L)));
        KxWikiCardChangesBO changes = fixture.service().scan(fixture.scope());
        assertEquals(Set.of(1L, 2L, 3L, 4L), changes.getCardIds());
        assertEquals(List.of("page:1"), fixture.requests);
        assertTrue(changes.isChanged());
        assertEquals(Map.of(fixture.scope(), "same-version"), fixture.saved);
    }

    /**
     * 重建服务后从成功检查点恢复，重复批次和当天重排不再产生候选。
     **/
    @Test
    public void shouldResumeCheckpointsAndIgnoreItemOrderAfterRestart() {
        Fixture fixture = new Fixture();
        fixture.pages.put(1, timeline(2, group("2026-11-10", 1L, 2L)));
        fixture.confirm(fixture.service().scan(fixture.scope()));
        fixture.pages.put(1, timeline(2, group("2026-11-10", 2L, 1L)));
        fixture.requests.clear();
        KxWikiCardChangesBO repeated = fixture.service().scan(fixture.scope());
        assertFalse(repeated.isChanged());
        assertTrue(repeated.getCardIds().isEmpty());
        assertEquals(List.of("page:1"), fixture.requests);
        assertTrue(repeated.getTimelineCheckpoint().length() <= 128);
    }

    /**
     * 停服多日后跨页追溯到上次成功日期，保留边界当天并停止于更早日期。
     **/
    @Test
    public void shouldFollowPagesThroughPersistedBoundary() {
        Fixture fixture = new Fixture();
        fixture.pages.put(1, timeline(1, group("2026-10-01", 1L)));
        fixture.confirm(fixture.service().scan(fixture.scope()));
        fixture.pages.put(1, timeline(3, group("2026-11-10", 2L)));
        fixture.pages.put(2, timeline(3, group("2026-10-12", 3L),
                group("2026-10-01", 1L, 4L), group("2026-09-30", 5L)));
        fixture.requests.clear();
        KxWikiCardChangesBO changes = fixture.service().scan(fixture.scope());
        assertEquals(Set.of(1L, 2L, 3L, 4L), changes.getCardIds());
        assertEquals(List.of("page:1", "page:2"), fixture.requests);
    }

    /**
     * 同一天追加开放记录，即使源 status 与极限标记不变也必须重新抓取。
     **/
    @Test
    public void shouldDetectMoreCardsOnSameDate() {
        Fixture fixture = new Fixture();
        fixture.pages.put(1, timeline(1, group("2026-11-10", 1L)));
        fixture.confirm(fixture.service().scan(fixture.scope()));
        fixture.pages.put(1, timeline(1, group("2026-11-10", 1L, 2L)));
        KxWikiCardChangesBO changes = fixture.service().scan(fixture.scope());
        assertTrue(changes.isChanged());
        assertEquals(Set.of(1L, 2L), changes.getCardIds());
    }

    /**
     * 截断的日期必须再读取完整内容，包含首页没有展示的卡片。
     **/
    @Test
    public void shouldFetchTruncatedDayBeforeConfirmingCheckpoint() {
        Fixture fixture = new Fixture();
        KxWikiTimelineGroupDTO truncated = group("2026-11-10", 1L);
        truncated.setTruncated(true);
        truncated.setTotal(2);
        fixture.pages.put(1, timeline(1, truncated));
        fixture.day = timeline(1, group("2026-11-10", 1L, 2L));
        KxWikiCardChangesBO changes = fixture.service().scan(fixture.scope());
        assertEquals(Set.of(1L, 2L), changes.getCardIds());
        assertEquals(List.of("page:1", "day:2026-11-10"), fixture.requests);
    }

    /**
     * 中途分页失败不能确认新批次，重建服务仍需重扫未完成区间。
     **/
    @Test
    public void shouldRetainAllOldCheckpointsWhenTimelinePageFails() throws Exception {
        Fixture fixture = new Fixture();
        fixture.pages.put(1, timeline(1, group("2026-10-01", 1L)));
        fixture.confirm(fixture.service().scan(fixture.scope()));
        Map<String, String> previous = new HashMap<>(fixture.saved);
        fixture.latest.setFingerprint("batch-2");
        fixture.pages.put(1, timeline(2, group("2026-11-10", 2L)));
        WikiCardStrategy strategy = fixture.strategy(List.of(1L, 2L), List.of());
        WikiContext context = new WikiContext();
        assertThrows(IllegalStateException.class, () -> strategy.execute(context));
        strategy.markVersionComplete(context);
        assertEquals(previous, fixture.saved);
        fixture.pages.put(2, timeline(2, group("2026-10-01", 1L)));
        assertEquals(Set.of(1L, 2L), fixture.service().scan(fixture.scope()).getCardIds());
    }

    /**
     * DDL 未发布卡可独立于目录导入，同时仍按稀有度、黑名单筛选并与目录去重。
     **/
    @Test
    public void shouldRefreshSignaledExistingCardsWithinAutomaticScope() throws Exception {
        Fixture fixture = new Fixture();
        KxWikiCardChangeDTO lowRarity = card(4L);
        lowRarity.setRarity(3);
        KxWikiCardChangeDTO unreleased = card(99L);
        unreleased.setReleaseAt(Long.MAX_VALUE);
        fixture.latest.setNewUnits(List.of(card(1L), card(1L), card(3L), lowRarity, unreleased));
        fixture.pages.put(1, timeline(1, group("2026-11-10", 2L)));
        WikiCardStrategy strategy = fixture.strategy(List.of(1L, 2L, 3L, 4L), List.of(3L));
        WikiContext context = new WikiContext();
        List<Long> processed = new ArrayList<>();
        strategy.execute(context, aggregate -> {
            Long id = aggregate.getCards().getFirst().getCardId();
            processed.add(id);
            strategy.markCardComplete(id);
        });
        assertEquals(List.of(1L, 2L, 99L), processed);
        strategy.markVersionComplete(context);
        assertEquals("batch-1", fixture.saved.get(fixture.scope() + CardSyncConstants.KX_WIKI_DDL_CHECKPOINT_SUFFIX));
    }

    /**
     * 任一单卡写入失败都阻止整个检查点推进，重启后从旧水位重新发现该卡。
     **/
    @Test
    public void shouldRediscoverFailedBatchAfterStrategyRestart() throws Exception {
        Fixture fixture = new Fixture();
        fixture.latest.setNewEzas(List.of(card(1L)));
        fixture.pages.put(1, timeline(1, group("2026-11-10", 2L)));
        WikiCardStrategy strategy = fixture.strategy(List.of(2L), List.of());
        WikiContext failed = new WikiContext();
        strategy.execute(failed, aggregate -> {
            Long id = aggregate.getCards().getFirst().getCardId();
            if (id.equals(1L)) {
                throw new IllegalStateException("Simulated ES write failure");
            }
            strategy.markCardComplete(id);
        });
        strategy.markVersionComplete(failed);
        assertEquals(List.of(1L), failed.getFailedCardIds());
        assertEquals(Map.of(fixture.scope(), "same-version"), fixture.saved);
        WikiCardStrategy restarted = fixture.strategy(List.of(2L), List.of());
        WikiContext successful = new WikiContext();
        restarted.execute(successful, aggregate -> restarted.markCardComplete(aggregate.getCards().getFirst().getCardId()));
        restarted.markVersionComplete(successful);
        assertNotNull(fixture.saved.get(fixture.scope() + CardSyncConstants.KX_WIKI_TIMELINE_CHECKPOINT_SUFFIX));
    }

    /**
     * 旧版已确认同一批次和 status，但未发布卡未入库时仍须补录，不能继续短路。
     **/
    @Test
    public void shouldRecoverMissingDdlCardEvenWhenAllCheckpointsAreUnchanged() throws Exception {
        Fixture fixture = new Fixture();
        fixture.latest.setNewUnits(List.of(card(99L)));
        fixture.pages.put(1, timeline(1, group("2026-11-10", 2L)));
        fixture.confirm(fixture.service().scan(fixture.scope()));
        WikiCardStrategy strategy = fixture.strategy(List.of(2L), List.of());
        WikiContext context = new WikiContext();
        List<Long> processed = new ArrayList<>();
        strategy.execute(context, aggregate -> {
            Long id = aggregate.getCards().getFirst().getCardId();
            processed.add(id);
            strategy.markCardComplete(id);
        });
        assertFalse(context.getCardChanges().isChanged());
        assertEquals(List.of(99L), processed);
    }

    /**
     * 同一批次卡片已在本地完整保存时不再重复抓取，包括未出现在目录的卡片。
     **/
    @Test
    public void shouldSkipUnchangedBatchWhenDdlCardAlreadyExists() throws Exception {
        Fixture fixture = new Fixture();
        fixture.latest.setNewUnits(List.of(card(99L)));
        fixture.pages.put(1, timeline(1, group("2026-11-10", 2L)));
        fixture.confirm(fixture.service().scan(fixture.scope()));
        WikiCardStrategy strategy = fixture.strategy(List.of(2L, 99L), List.of());
        WikiContext context = new WikiContext();
        strategy.execute(context, aggregate -> fail("Unchanged complete DDL card must not be fetched again"));
        assertNull(context.getSourceUpdatedAt());
    }

    /**
     * 其他地区只使用其时间线，不读取来自国际服的数据库批次。
     **/
    @Test
    public void shouldUseOnlyRegionalTimelineOutsideGlobal() {
        Fixture fixture = new Fixture();
        fixture.properties.setRegion("japan");
        fixture.latest = null;
        fixture.pages.put(1, timeline(1, group("2026-11-10", 2L)));
        KxWikiCardChangesBO changes = fixture.service().scan(fixture.scope());
        assertNull(changes.getDdlFingerprint());
        assertEquals(Set.of(2L), changes.getCardIds());
    }

    /**
     * 历史暂时消失时不能把成功日期倒退。
     **/
    @Test
    public void shouldRejectTimelineDateRegression() {
        Fixture fixture = new Fixture();
        fixture.pages.put(1, timeline(1, group("2026-11-10", 1L)));
        fixture.confirm(fixture.service().scan(fixture.scope()));
        fixture.pages.put(1, timeline(1, group("2026-10-01", 2L)));
        assertThrows(RuntimeException.class, () -> fixture.service().scan(fixture.scope()));
    }

    /**
     * 构建最小卡片开放信号。
     **/
    private static KxWikiCardChangeDTO card(Long id) {
        KxWikiCardChangeDTO item = new KxWikiCardChangeDTO();
        item.setId(id);
        item.setRarity(4);
        return item;
    }

    /**
     * 构建声明完整的日期分组。
     **/
    private static KxWikiTimelineGroupDTO group(String date, Long... ids) {
        KxWikiTimelineGroupDTO group = new KxWikiTimelineGroupDTO();
        group.setDate(date);
        group.setItems(Arrays.stream(ids).map(KxWikiCardChangeServiceTest::card).toList());
        group.setTotal(ids.length);
        group.setTruncated(false);
        return group;
    }

    /**
     * 构建可用时间线页，保留真实客户端的日期完整性校验。
     **/
    private static KxWikiTimelineDTO timeline(int pages, KxWikiTimelineGroupDTO... groups) {
        KxWikiTimelineDTO response = new KxWikiTimelineDTO();
        response.setAvailable(true);
        response.setPages(pages);
        response.setGroups(List.of(groups));
        return response;
    }

    /**
     * @Description 测试用源站与持久化快照，重建业务对象时保留成功状态
     * @Author Codex
     * @Date 2026/11/10
     **/
    private static class Fixture {
        /**
         * 已确认的全局版本、批次指纹和时间线检查点。
         **/
        private final Map<String, String> saved = new HashMap<>();
        /**
         * 源站地区和语言配置。
         **/
        private final KxWikiProperties properties = new KxWikiProperties();
        /**
         * 每次查询时返回的时间线分页。
         **/
        private final Map<Integer, KxWikiTimelineDTO> pages = new HashMap<>();
        /**
         * 真实发生的分页及按日期补取调用。
         **/
        private final List<String> requests = new ArrayList<>();
        /**
         * 最新数据库批次；非国际服用例为空以禁止误用。
         **/
        private KxWikiLatestDdlDTO latest = new KxWikiLatestDdlDTO();
        /**
         * 截断日期的完整补取结果。
         **/
        private KxWikiTimelineDTO day;

        /**
         * 初始化空候选批次和已存在的 status 版本。
         **/
        private Fixture() {
            latest.setFingerprint("batch-1");
            latest.setNewUnits(List.of());
            latest.setNewEzas(List.of());
            saved.put(scope(), "same-version");
        }

        /**
         * 生成与真实业务一致的同步范围。
         **/
        private String scope() {
            return String.format(CardSyncConstants.KX_WIKI_CARD_SYNC_KEY_FORMAT, properties.getRegion(), properties.getLang());
        }

        /**
         * 在共享成功快照上重建扫描服务。
         **/
        private KxWikiCardChangeService service() {
            return new KxWikiCardChangeService(client(), properties, repository());
        }

        /**
         * 模拟整轮成功确认，不在扫描时修改快照。
         **/
        private void confirm(KxWikiCardChangesBO changes) {
            repository().confirmCardVersion(scope(), "same-version", changes.getDdlFingerprint(), changes.getTimelineCheckpoint());
        }

        /**
         * 构造仅替换存取介质的仓储。
         **/
        private KxWikiSyncRepository repository() {
            return new KxWikiSyncRepository(null) {
                /**
                 * 从上轮成功快照读取水位。
                 **/
                @Override
                public String findLastUpdatedAt(String key) {
                    return saved.get(key);
                }

                /**
                 * 模拟三个检查点同时写入。
                 **/
                @Override
                public void confirmCardVersion(String key, String version, String fingerprint, String timeline) {
                    saved.put(key, version);
                    if (fingerprint != null) {
                        saved.put(key + CardSyncConstants.KX_WIKI_DDL_CHECKPOINT_SUFFIX, fingerprint);
                    }
                    if (timeline != null) {
                        saved.put(key + CardSyncConstants.KX_WIKI_TIMELINE_CHECKPOINT_SUFFIX, timeline);
                    }
                }
            };
        }

        /**
         * 返回受控响应，未配置页模拟源请求失败。
         **/
        private KxWikiClient client() {
            return new KxWikiClient(WebClient.builder().build(), properties) {
                /**
                 * 源 status 保持不变，验证额外信号可绕过短路。
                 **/
                @Override
                public Optional<KxWikiStatusDTO> getStatus() {
                    KxWikiStatusDTO status = new KxWikiStatusDTO();
                    status.setUpdatedAt("same-version");
                    return Optional.of(status);
                }

                /**
                 * 返回当前批次，其他地区误调用会直接失败。
                 **/
                @Override
                public KxWikiLatestDdlDTO getLatestDdl() {
                    return Objects.requireNonNull(latest);
                }

                /**
                 * 按页码或日期返回时间线，记录补取路径。
                 **/
                @Override
                public KxWikiTimelineDTO getCardTimeline(int page, String date) {
                    requests.add(date == null ? "page:" + page : "day:" + date);
                    KxWikiTimelineDTO response = date == null ? pages.get(page) : day;
                    if (response == null) {
                        throw new IllegalStateException("Simulated source failure");
                    }
                    return response;
                }
            };
        }

        /**
         * 创建真实候选策略，详情用最小聚合替代以聚焦调度和失败行为。
         **/
        private WikiCardStrategy strategy(List<Long> ids, List<Long> blacklist) throws Exception {
            WikiCardStrategy strategy = new WikiCardStrategy() {
                /**
                 * 构造可被消费的最小卡片聚合。
                 **/
                @Override
                public KxWikiCardBO fetchCard(Long cardId, boolean manual) {
                    KxWikiCardBO aggregate = new KxWikiCardBO();
                    CardPO card = new CardPO();
                    card.setCardId(cardId);
                    aggregate.getCards().add(card);
                    return aggregate;
                }
            };
            WikiCardFilter filter = new WikiCardFilter();
            filter.setBlackCardId(blacklist.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(",")));
            KxWikiClient catalogClient = new KxWikiClient(WebClient.builder().build(), properties) {
                /**
                 * 固定已成功的源版本。
                 **/
                @Override
                public Optional<KxWikiStatusDTO> getStatus() {
                    return client().getStatus();
                }

                /**
                 * 第四张卡稀有度过低，验证额外信号不绕过原同步范围。
                 **/
                @Override
                public KxWikiPageResponse<KxWikiCardListItemDTO> listCards(int page) {
                    KxWikiPageResponse<KxWikiCardListItemDTO> response = new KxWikiPageResponse<>();
                    response.setPages(1);
                    response.setItems(ids.stream().map(id -> {
                        KxWikiCardListItemDTO item = new KxWikiCardListItemDTO();
                        item.setId(id);
                        item.setRarity(id.equals(4L) ? 3 : 4);
                        return item;
                    }).toList());
                    return response;
                }
            };
            DokkanCardRepository cards = new DokkanCardRepository() {
                /**
                 * 所有卡已有 hash 且极限标记相同，只能由额外更新信号触发。
                 **/
                @Override
                public List<CardPO> listSyncFlags() {
                    return ids.stream().map(id -> {
                        CardPO card = new CardPO();
                        card.setCardId(id);
                        card.setSourceHash("existing-hash");
                        return card;
                    }).toList();
                }
            };
            inject(strategy, "client", catalogClient);
            inject(strategy, "properties", properties);
            inject(strategy, "syncRepository", repository());
            inject(strategy, "cardFilter", filter);
            inject(strategy, "cardRepository", cards);
            inject(strategy, "changeService", service());
            return strategy;
        }

        /**
         * 不启动 Spring 容器时注入策略依赖。
         **/
        private void inject(WikiCardStrategy strategy, String name, Object value) throws Exception {
            Field field = WikiCardStrategy.class.getDeclaredField(name);
            field.setAccessible(true);
            field.set(strategy, value);
        }
    }
}
