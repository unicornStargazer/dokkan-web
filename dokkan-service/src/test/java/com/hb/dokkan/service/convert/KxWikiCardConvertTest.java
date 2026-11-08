package com.hb.dokkan.service.convert;

import com.hb.dokkan.common.domain.bo.data.KxWikiCardBO;
import com.hb.dokkan.common.domain.dto.data.kx.KxWikiCardDetailDTO;
import com.hb.dokkan.common.domain.dto.data.kx.KxWikiEnrichmentDTO;
import com.hb.dokkan.common.domain.dto.data.kx.KxWikiExtrasDTO;
import com.hb.dokkan.common.domain.po.mysql.cards.CardPO;
import com.hb.dokkan.common.domain.po.mysql.cards.CardStatePO;
import com.hb.dokkan.common.utils.JsonUtils;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

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
