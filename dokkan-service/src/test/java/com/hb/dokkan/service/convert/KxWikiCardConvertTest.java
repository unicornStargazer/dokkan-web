package com.hb.dokkan.service.convert;

import com.hb.dokkan.common.domain.bo.data.KxWikiCardBO;
import com.hb.dokkan.common.domain.dto.data.kx.KxWikiCardDetailDTO;
import com.hb.dokkan.common.domain.dto.data.kx.KxWikiEnrichmentDTO;
import com.hb.dokkan.common.domain.dto.data.kx.KxWikiExtrasDTO;
import com.hb.dokkan.common.domain.dto.data.kx.KxWikiTextDTO;
import com.hb.dokkan.common.domain.po.mysql.cards.CardPO;
import com.hb.dokkan.common.domain.po.mysql.cards.CardSkillPO;
import com.hb.dokkan.common.domain.po.mysql.cards.CardStatePO;
import com.hb.dokkan.common.domain.po.es.cards.CardEsPO;
import com.hb.dokkan.common.constants.CardSyncConstants;
import org.mapstruct.factory.Mappers;
import com.hb.dokkan.common.utils.JsonUtils;
import com.hb.dokkan.common.exception.domain.DokkanBizException;
import org.junit.Test;
import org.h2.jdbcx.JdbcDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * 使用已保存的 KX Wiki 响应验证状态、形态及技能转换，无需访问外部服务。
 **/
public class KxWikiCardConvertTest {
    /**
     * 被测 KX Wiki 卡片聚合转换器。
     **/
    private final KxWikiCardConvert convert = new KxWikiCardConvert();

    /**
     * 本体没有被动仍保留全部状态和技能，经过 ES 及详情响应转换后被动和领域保持为空。
     **/
    @Test
    public void missingPassiveShouldKeepCardStatesAndDetail() throws Exception {
        KxWikiCardBO baseline = convertSample(1017421L);
        KxWikiCardDetailDTO source = detail(1017421L);
        source.getStates().getFirst().setPassive(null);
        KxWikiCardBO aggregate = convert.toAggregate(source, enrichment(1017421L), extras(1017421L));
        assertEquals(baseline.getStates().size(), aggregate.getStates().size());
        assertEquals(baseline.getSkills().size(), aggregate.getSkills().size());
        CardStatePO base = state(aggregate, 1017421L, "base");
        assertNull(base.getPassiveSkillId());
        assertNull(base.getPassiveSkillName());
        assertNull(base.getPassiveSkillDesc());
        assertEquals(state(baseline, 1017421L, "eza").getPassiveSkillDesc(),
                state(aggregate, 1017421L, "eza").getPassiveSkillDesc());
        var esConvert = Mappers.getMapper(KxWikiEsConvert.class);
        CardEsPO document = new CardEsPO();
        document.setStates(esConvert.statesToEs(
                aggregate.getStates().stream().filter(row -> row.getCardId() == 1017421L).toList(),
                aggregate.getSkills().stream().filter(row -> row.getCardId() == 1017421L).toList()));
        var cardConvert = Mappers.getMapper(DokkanCardConvert.class);
        var response = cardConvert.detailVoToResponse(cardConvert.convertToCardDetailVO(document));
        assertNull(response.getStates().getFirst().getPassiveSkill());
        assertTrue(response.getStates().getFirst().getPassiveDomains().isEmpty());
        assertFalse(response.getStates().getFirst().getSpecialSkills().isEmpty());
    }

    /**
     * 形态的极限状态没有被动时保持为空，不继承本体或该形态极限前的被动。
     **/
    @Test
    public void missingFormPassiveShouldNotInheritFromOtherStates() throws Exception {
        KxWikiCardDetailDTO source = detail(1017421L);
        var form = source.getForms().getFirst();
        form.getStates().stream().filter(row -> "eza".equals(row.getKey())).findFirst().orElseThrow().setPassive(null);
        KxWikiCardBO aggregate = convert.toAggregate(source, enrichment(1017421L), extras(1017421L));
        assertNull(state(aggregate, form.getCardId(), "eza").getPassiveSkillDesc());
        assertNotNull(state(aggregate, form.getCardId(), "base").getPassiveSkillDesc());
        assertNotNull(state(aggregate, source.getId(), "eza").getPassiveSkillDesc());
        var esConvert = Mappers.getMapper(KxWikiEsConvert.class);
        assertTrue(esConvert.storedDomains(state(aggregate, form.getCardId(), "eza").getAttributes()).isEmpty());
    }

    /**
     * 源站以空对象表示没有被动时，同样允许导入且不制造领域记录。
     **/
    @Test
    public void emptyPassiveObjectShouldAllowImport() throws Exception {
        KxWikiCardDetailDTO source = detail(1017421L);
        source.getStates().getFirst().setPassive(new KxWikiTextDTO());
        KxWikiCardBO aggregate = convert.toAggregate(source, enrichment(1017421L), extras(1017421L));
        CardStatePO base = state(aggregate, 1017421L, "base");
        assertNull(base.getPassiveSkillId());
        assertNull(base.getPassiveSkillName());
        assertNull(base.getPassiveSkillDesc());
        assertTrue(Mappers.getMapper(KxWikiEsConvert.class).storedDomains(base.getAttributes()).isEmpty());
    }

    /**
     * 放宽可选被动不影响三围完整性要求，缺少三围对象或任一数值仍拒绝导入。
     **/
    @Test
    public void missingStatsShouldStillRejectCardWithoutPassive() throws Exception {
        for (String field : List.of("stats", "hp", "atk", "def")) {
            KxWikiCardDetailDTO source = detail(1017421L);
            var base = source.getStates().getFirst();
            base.setPassive(null);
            switch (field) {
                case "stats" -> base.setStats(null);
                case "hp" -> base.getStats().setHp(null);
                case "atk" -> base.getStats().setAtk(null);
                case "def" -> base.getStats().setDef(null);
                default -> throw new AssertionError(field);
            }
            KxWikiEnrichmentDTO supplement = enrichment(1017421L);
            assertThrows(DokkanBizException.class, () -> convert.toAggregate(source, supplement, null));
        }
    }

    /**
     * 同一效果的不同气力必杀必须保留独立源记录，并能通过实际表结构的唯一键校验。
     **/
    @Test
    public void sharedEffectShouldKeepAllAttackRecordsAndPersist() throws Exception {
        KxWikiCardBO aggregate = convert.toAggregate(detail(1008821L), enrichment(1008821L), null);
        assertEquals(6, aggregate.getSkills().size());
        assertEquals(510L, ((Number) JsonUtils.json2Map(aggregate.getSkills().getFirst().getAttributes(),
                String.class, Object.class).get(CardSyncConstants.SKILL_EFFECT_ID_FIELD)).longValue());
        assertEquals(510L, ((Number) JsonUtils.json2Map(aggregate.getSkills().get(2).getAttributes(),
                String.class, Object.class).get(CardSyncConstants.SKILL_EFFECT_ID_FIELD)).longValue());
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        // 直接读取上线脚本的 card_skill 定义，避免测试中的唯一键与生产库不一致。
        String script = Files.readString(Path.of("..", "dokkan-starter", "src", "main", "resources",
                "db", "kx-wiki-card-refactor.sql"));
        int tableStart = script.indexOf("CREATE TABLE `card_skill`");
        jdbc.execute(script.substring(tableStart, script.indexOf(';', tableStart)));
        try {
            for (CardSkillPO skill : aggregate.getSkills()) {
                jdbc.update("INSERT INTO card_skill (id, card_id, state_key, skill_type, skill_id, name, ki, sort) "
                                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)", UUID.randomUUID().toString(), skill.getCardId(),
                        skill.getStateKey(), skill.getSkillType(), skill.getSkillId(), skill.getName(),
                        skill.getKi(), skill.getSort());
            }
            assertEquals(Integer.valueOf(6), jdbc.queryForObject("SELECT COUNT(*) FROM card_skill", Integer.class));
        } finally {
            jdbc.execute("SHUTDOWN");
        }
        var esConvert = Mappers.getMapper(KxWikiEsConvert.class);
        var states = esConvert.statesToEs(aggregate.getStates(), aggregate.getSkills());
        assertEquals(List.of(1751L, 1696L, 1697L), states.getFirst().getSpecialSkills().stream()
                .map(skill -> skill.getSpecialId()).toList());
        assertEquals(List.of(10, 11, 12), states.getFirst().getSpecialSkills().stream()
                .map(skill -> skill.getEballNumStart()).toList());
        assertEquals(List.of(5718L, 5719L, 5720L), states.get(1).getSpecialSkills().stream()
                .map(skill -> skill.getSpecialId()).toList());
    }

    /**
     * 共用效果的记录按源 id 对齐倍率；打乱 enrichment 顺序后仍匹配各自的伤害信息。
     **/
    @Test
    public void sharedEffectShouldMatchEnrichmentByAttackRecordId() throws Exception {
        KxWikiEnrichmentDTO supplement = enrichment(1008821L);
        var attacks = supplement.getStates().get("base").getSuperAttacks();
        int changedIncreaseRate = 320;
        attacks.get(2).getDamage().setIncreaseRate(changedIncreaseRate);
        Collections.reverse(attacks);
        KxWikiCardBO aggregate = convert.toAggregate(detail(1008821L), supplement, null);
        assertEquals(Integer.valueOf(160), aggregate.getSkills().getFirst().getIncreaseRate());
        assertEquals(Integer.valueOf(changedIncreaseRate), aggregate.getSkills().get(2).getIncreaseRate());
    }

    /**
     * 缺少必杀记录 id 时禁止退回共用效果编号，避免再次制造唯一键冲突。
     **/
    @Test
    public void missingAttackRecordIdShouldRejectAggregate() throws Exception {
        KxWikiCardDetailDTO source = detail(1008821L);
        source.getStates().getFirst().getSuperAttacks().getFirst().setId(null);
        KxWikiEnrichmentDTO supplement = enrichment(1008821L);
        assertThrows(DokkanBizException.class, () -> convert.toAggregate(source, supplement, null));
    }

    /**
     * 真实领域卡样本从源响应经过数据库扩展属性及 ES，仍区分被动依赖与主动展开。
     **/
    @Test
    public void domainCardShouldPreservePassiveRequirementAndActiveCreation() throws Exception {
        KxWikiCardDetailDTO source = detail(1033941L);
        KxWikiEnrichmentDTO enrichment = new KxWikiEnrichmentDTO();
        enrichment.setCardId(source.getId());
        KxWikiCardBO aggregate = convert.toAggregate(source, enrichment, null);
        CardStatePO state = aggregate.getStates().getFirst();
        assertTrue(state.getAttributes().contains(CardSyncConstants.SKILL_DOMAINS_FIELD));
        var esConvert = Mappers.getMapper(KxWikiEsConvert.class);
        CardEsPO document = new CardEsPO();
        document.setStates(esConvert.statesToEs(aggregate.getStates(), aggregate.getSkills()));
        CardEsPO restored = JsonUtils.json2Object(JsonUtils.object2Json(document), CardEsPO.class);
        var cardConvert = Mappers.getMapper(DokkanCardConvert.class);
        var response = cardConvert.detailVoToResponse(cardConvert.convertToCardDetailVO(restored));
        var base = response.getStates().getFirst();
        var passiveDomain = base.getPassiveDomains().getFirst();
        var activeDomain = base.getActiveSkills().getFirst().getDomains().getFirst();
        assertEquals(Long.valueOf(12L), passiveDomain.getId());
        assertEquals(passiveDomain.getId(), activeDomain.getId());
        assertEquals(Boolean.TRUE, passiveDomain.getNeeded());
        assertEquals(Boolean.FALSE, passiveDomain.getCreates());
        assertEquals(Boolean.FALSE, activeDomain.getNeeded());
        assertEquals(Boolean.TRUE, activeDomain.getCreates());
        assertEquals(Integer.valueOf(3), activeDomain.getTurns());
        assertEquals(Long.valueOf(3011L), activeDomain.getResourceId());
        assertEquals(source.getStates().getFirst().getActiveSkill().getDomains().getFirst().getName(), activeDomain.getName());
        assertEquals(source.getStates().getFirst().getActiveSkill().getDomains().getFirst().getDescription(), activeDomain.getDescription());
        assertNotNull(activeDomain.getThumbnail());
    }

    /**
     * 普通变身卡保留四个状态、继承队长技/分类并对齐极限必杀倍率。
     **/
    @Test
    public void transformationShouldInheritLeaderAndCategories() throws Exception {
        KxWikiCardBO aggregate = convertSample(1017421L);
        assertEquals(2, aggregate.getCards().size());
        assertEquals(4, aggregate.getStates().size());
        CardPO form = aggregate.getCards().get(1);
        assertEquals(Long.valueOf(1017421L), form.getBaseCardId());
        assertEquals(Long.valueOf(4018781L), form.getCardId());
        CardStatePO base = state(aggregate, 1017421L, "base");
        assertEquals(base.getLeaderSkillDesc(), state(aggregate, 4018781L, "base").getLeaderSkillDesc());
        assertEquals(categoryIds(aggregate, 1017421L), categoryIds(aggregate, 4018781L));
        assertEquals(Instant.parse("2024-12-10T05:00:00Z"), state(aggregate, 1017421L, "eza").getPublishTime().toInstant());
        assertTrue(aggregate.getSkills().stream().filter(skill -> "super".equals(skill.getSkillType()))
                .allMatch(skill -> skill.getIncreaseRate() != null && skill.getLvBonus() != null));
    }

    /**
     * LR 的多种必杀不丢失，倍率通过技能 ID 对齐。
     **/
    @Test
    public void lrShouldKeepAllSuperAttacks() throws Exception {
        KxWikiCardBO aggregate = convertSample(1028891L);
        assertEquals(1, aggregate.getCards().size());
        assertEquals(1, aggregate.getStates().size());
        assertTrue(aggregate.getSkills().stream().filter(skill -> "super".equals(skill.getSkillType())).count() >= 2);
        assertTrue(aggregate.getSkills().stream().filter(skill -> "super".equals(skill.getSkillType()))
                .allMatch(skill -> skill.getIncreaseRate() != null));
    }

    /**
     * 预备/终结技能和形态卡同时保留，模板中的图片占位符保持原样。
     **/
    @Test
    public void standbyShouldKeepFinishAndTemplate() throws Exception {
        KxWikiCardBO aggregate = convertSample(1031181L);
        assertTrue(aggregate.getCards().stream().anyMatch(card -> Long.valueOf(4031191L).equals(card.getCardId())));
        assertTrue(aggregate.getSkills().stream().anyMatch(skill -> "standby".equals(skill.getSkillType())));
        assertTrue(aggregate.getSkills().stream().anyMatch(skill -> "finish".equals(skill.getSkillType())));
        assertTrue(state(aggregate, 1031181L, "base").getPassiveSkillDesc().contains("{passiveImg:once}"));
        assertEquals(Integer.valueOf(100), aggregate.getCards().getFirst().getType());
    }

    /**
     * 超极限保留三个状态，不以 SEZA 覆盖 EZA 数据。
     **/
    @Test
    public void sezaShouldKeepThreeStates() throws Exception {
        KxWikiCardBO aggregate = convertSample(1032521L);
        assertEquals(3, aggregate.getStates().size());
        assertTrue(aggregate.getCards().getFirst().getSezaFlag());
        assertNotNull(state(aggregate, 1032521L, "seza").getPublishTime());
        assertNotEquals(state(aggregate, 1032521L, "eza").getPassiveSkillDesc(),
                state(aggregate, 1032521L, "seza").getPassiveSkillDesc());
    }

    /**
     * 标题缺失时取本体 base 队长技名称。
     **/
    @Test
    public void blankTitleShouldUseLeaderName() throws Exception {
        KxWikiCardDetailDTO detail = detail(1017421L);
        detail.setTitle(null);
        KxWikiCardBO aggregate = convert.toAggregate(detail, enrichment(1017421L), extras(1017421L));
        assertEquals(detail.getStates().getFirst().getLeader().getName(), aggregate.getCards().getFirst().getTitle());
    }

    /**
     * 按 ID 转换三个端点的本地快照。
     **/
    private KxWikiCardBO convertSample(long id) throws Exception {
        return convert.toAggregate(detail(id), enrichment(id), extras(id));
    }

    /**
     * 读取并保留详情 hash 源文本。
     **/
    private KxWikiCardDetailDTO detail(long id) throws Exception {
        String json = sample("cards_" + id + ".json");
        KxWikiCardDetailDTO detail = JsonUtils.json2Object(json, KxWikiCardDetailDTO.class);
        detail.setSourceJson(json);
        return detail;
    }

    /**
     * 读取并保留 enrichment hash 源文本。
     **/
    private KxWikiEnrichmentDTO enrichment(long id) throws Exception {
        String json = sample("cards_" + id + "_enrichment.json");
        KxWikiEnrichmentDTO enrichment = JsonUtils.json2Object(json, KxWikiEnrichmentDTO.class);
        enrichment.setSourceJson(json);
        return enrichment;
    }

    /**
     * 读取并保留 extras hash 源文本。
     **/
    private KxWikiExtrasDTO extras(long id) throws Exception {
        String json = sample("cards_" + id + "_extras.json");
        KxWikiExtrasDTO extras = JsonUtils.json2Object(json, KxWikiExtrasDTO.class);
        extras.setSourceJson(json);
        return extras;
    }

    /**
     * 从仓库保存的样本读取 UTF-8 文本，兼容根项目和模块工作目录。
     **/
    private String sample(String name) throws Exception {
        // 新增样本放在测试资源内，避免依赖被 Git 忽略的本地文档目录。
        try (var input = getClass().getResourceAsStream("/kx-wiki/" + name)) {
            if (input != null) {
                return JsonUtils.simplifyJsonText(new String(input.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
            }
        }
        Path root = Path.of("").toAbsolutePath();
        while (root != null) {
            Path file = root.resolve(".codex/changes/kx-wiki-card-refactor/samples").resolve(name);
            if (Files.exists(file)) {
                return JsonUtils.simplifyJsonText(Files.readString(file));
            }
            root = root.getParent();
        }
        throw new IllegalStateException("Missing KX Wiki fixture: " + name);
    }

    /**
     * 按业务键读取状态，缺失时使测试失败。
     **/
    private CardStatePO state(KxWikiCardBO aggregate, long id, String key) {
        return aggregate.getStates().stream().filter(row -> row.getCardId() == id && key.equals(row.getStateKey()))
                .findFirst().orElseThrow();
    }

    /**
     * 保序读取分类用于比较形态继承。
     **/
    private List<Long> categoryIds(KxWikiCardBO aggregate, long id) {
        return aggregate.getCategories().stream().filter(row -> row.getCardId() == id).map(row -> row.getCategoryId()).toList();
    }
}
