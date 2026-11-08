package com.hb.dokkan.service.job.sync;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.hb.dokkan.common.constants.CardSyncConstants;
import com.hb.dokkan.config.kx.KxWikiProperties;
import com.hb.dokkan.infrastructure.mysql.sync.KxWikiSyncRepository;
import com.hb.dokkan.infrastructure.mysql.sync.mapper.KxWikiSyncMapper;
import com.hb.dokkan.service.client.KxWikiClient;
import com.hb.dokkan.service.job.sync.strategy.context.WikiContext;
import com.hb.dokkan.service.job.sync.strategy.strategies.WikiCardStrategy;
import com.hb.dokkan.common.domain.dto.data.kx.KxWikiStatusDTO;
import com.hb.dokkan.common.domain.dto.data.kx.KxWikiPageResponse;
import com.hb.dokkan.common.domain.dto.data.kx.KxWikiCardListItemDTO;
import org.apache.ibatis.session.SqlSessionFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.web.reactive.function.client.WebClient;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * @Description 验证整个数据源版本持久化，单卡待重试集合保留在内存
 * @Author Codex
 * @Date 2026/11/8
 **/
public class KxWikiSyncPersistenceTest {
    /**
     * 各测试独享的文件数据库目录，结束后自动清理。
     **/
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    /**
     * 默认国际服中文数据源的全局版本范围。
     **/
    private final String syncKey = String.format(CardSyncConstants.KX_WIKI_CARD_SYNC_KEY_FORMAT, "global", "zh");

    /**
     * 文件数据库连接配置，重建仓储后仍读取同一磁盘文件。
     **/
    private JdbcDataSource dataSource;

    /**
     * 执行实际上线DDL，并验证增量脚本可重复执行。
     **/
    @Before
    public void setUp() throws Exception {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:file:" + temporaryFolder.newFolder("database").toPath().resolve("sync")
                + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_ON_EXIT=FALSE");
        Path script = Path.of("..", "dokkan-starter", "src", "main", "resources", "db", "kx-wiki-sync-state.sql");
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(new FileSystemResource(script));
        populator.setSqlScriptEncoding("UTF-8");
        populator.execute(dataSource);
        populator.execute(dataSource);
    }

    /**
     * 服务重建后保留全局版本，源站版本未变时不再读取卡片目录。
     **/
    @Test
    public void shouldKeepGlobalVersionAfterRestart() throws Exception {
        repository().confirmVersion(syncKey, "version-1");
        KxWikiSyncRepository restarted = repository();
        assertEquals("version-1", restarted.findLastUpdatedAt(syncKey));
        WikiCardStrategy strategy = strategy(restarted);
        WikiContext context = new WikiContext();
        strategy.execute(context);
        assertNull(context.getSourceUpdatedAt());
        assertTrue(context.getCardData().isEmpty());
    }

    /**
     * 全局版本更新覆盖原值，并按地区及语言隔离。
     **/
    @Test
    public void shouldUpdateGlobalVersionAndIsolateScopes() throws Exception {
        String otherScope = String.format(CardSyncConstants.KX_WIKI_CARD_SYNC_KEY_FORMAT, "japan", "en");
        repository().confirmVersion(syncKey, "old-version");
        repository().confirmVersion(otherScope, "japan-version");
        repository().confirmVersion(syncKey, "new-version");
        KxWikiSyncRepository restarted = repository();
        assertEquals("new-version", restarted.findLastUpdatedAt(syncKey));
        assertEquals("japan-version", restarted.findLastUpdatedAt(otherScope));
    }

    /**
     * 当前进程仍有失败卡时不确认全局版本，卡片成功后才更新。
     **/
    @Test
    @SuppressWarnings("unchecked")
    public void shouldConfirmGlobalVersionOnlyAfterPendingCardsComplete() throws Exception {
        repository().confirmVersion(syncKey, "old-version");
        WikiCardStrategy strategy = strategy(repository());
        Field field = WikiCardStrategy.class.getDeclaredField("pendingCardIds");
        field.setAccessible(true);
        Set<Long> pending = (Set<Long>) field.get(strategy);
        pending.add(1L);
        strategy.markVersionComplete("new-version");
        assertEquals("old-version", repository().findLastUpdatedAt(syncKey));
        strategy.markCardComplete(1L);
        strategy.markVersionComplete("new-version");
        assertEquals("new-version", repository().findLastUpdatedAt(syncKey));
        Set<Long> restartedPending = (Set<Long>) field.get(strategy(repository()));
        assertTrue(restartedPending.isEmpty());
    }

    /**
     * 首次部署仅创建全局版本表，单卡待重试记录不建表。
     **/
    @Test
    public void shouldCreateOnlyGlobalCheckpointTable() throws Exception {
        assertNull(repository().findLastUpdatedAt(syncKey));
        List<String> tables = new JdbcTemplate(dataSource).queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'", String.class);
        assertEquals(List.of("kx_wiki_sync_checkpoint"), tables);
    }

    /**
     * 重新创建MyBatis工厂及仓储，验证版本来自持久化文件数据库。
     **/
    private KxWikiSyncRepository repository() throws Exception {
        MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        factory.setConfiguration(configuration);
        factory.setDataSource(dataSource);
        SqlSessionFactory sessions = Objects.requireNonNull(factory.getObject());
        sessions.getConfiguration().addMapper(KxWikiSyncMapper.class);
        return new KxWikiSyncRepository(new SqlSessionTemplate(sessions).getMapper(KxWikiSyncMapper.class));
    }

    /**
     * 使用真实全局版本判断和受控源站状态；触发目录请求即说明短路失效。
     **/
    private WikiCardStrategy strategy(KxWikiSyncRepository repository) throws Exception {
        WikiCardStrategy strategy = new WikiCardStrategy();
        inject(strategy, "syncRepository", repository);
        inject(strategy, "properties", new KxWikiProperties());
        inject(strategy, "client", new KxWikiClient(WebClient.builder().build(), new KxWikiProperties()) {
            /**
             * 返回固定的整个数据源版本。
             **/
            @Override
            public Optional<KxWikiStatusDTO> getStatus() {
                KxWikiStatusDTO status = new KxWikiStatusDTO();
                status.setUpdatedAt("version-1");
                return Optional.of(status);
            }

            /**
             * 版本未变时不应扫描卡片目录。
             **/
            @Override
            public KxWikiPageResponse<KxWikiCardListItemDTO> listCards(int page) {
                throw new AssertionError("Unchanged global version must skip catalog requests");
            }
        });
        return strategy;
    }

    /**
     * 无Spring容器时注入仓储和外部依赖。
     **/
    private void inject(WikiCardStrategy target, String name, Object value) throws Exception {
        Field field = WikiCardStrategy.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}