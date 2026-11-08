package com.hb.dokkan.service.cards;

import com.hb.dokkan.common.domain.po.mysql.cards.CardPO;
import com.hb.dokkan.common.utils.JsonUtils;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 验证限定规则的人工优先级和源 hash 的稳定性。
 **/
public class CardTagRuleServiceTest {
    /**
     * 自动规则只命中 UR cost 48/58，不能自动标记 LR。
     **/
    @Test
    public void autoTagShouldOnlyMatchUrCosts() {
        CardTagRuleService service = new CardTagRuleService();
        for (int cost : new int[]{48, 58}) {
            CardPO incoming = card(4, cost);
            service.apply(incoming, null);
            assertTrue(incoming.getDokkanFesFlag());
        }
        CardPO lr = card(5, 58);
        service.apply(lr, null);
        assertFalse(lr.getDokkanFesFlag());
        CardPO ordinary = card(4, 42);
        service.apply(ordinary, null);
        assertFalse(ordinary.getDokkanFesFlag());
    }

    /**
     * 人工明确取消祭限定后，自动同步不得重新打开标记。
     **/
    @Test
    public void lockedTagsShouldWinOverAutomaticRule() {
        CardPO existing = card(4, 58);
        existing.setTagLocked(true);
        existing.setDokkanFesFlag(false);
        existing.setCarnivalFlag(true);
        existing.setFreeCardFlag(true);
        CardPO incoming = card(4, 58);
        new CardTagRuleService().apply(incoming, existing);
        assertFalse(incoming.getDokkanFesFlag());
        assertTrue(incoming.getCarnivalFlag());
        assertTrue(incoming.getFreeCardFlag());
        assertTrue(incoming.getTagLocked());
    }

    /**
     * 只有抓取时间和对象字段顺序改变时 hash 相同，技能或数组顺序改变时不同。
     **/
    @Test
    public void hashShouldIgnoreTimestampsButDetectContentChanges() {
        String original = "{\"id\":1,\"states\":[{\"as_of\":\"old\",\"value\":2}],\"as_of\":\"old\"}";
        String reordered = "{\"as_of\":\"new\",\"states\":[{\"value\":2,\"as_of\":\"new\"}],\"id\":1}";
        assertEquals(JsonUtils.sourceHash(original), JsonUtils.sourceHash(reordered));
        assertNotEquals(JsonUtils.sourceHash(original), JsonUtils.sourceHash(reordered.replace("value\":2", "value\":3")));
        assertNotEquals(JsonUtils.sourceHash("{\"ids\":[1,2]}"), JsonUtils.sourceHash("{\"ids\":[2,1]}"));
    }

    /**
     * 构建仅包含限定判断字段的卡片。
     **/
    private CardPO card(int rarity, int cost) {
        CardPO card = new CardPO();
        card.setRarity(rarity);
        card.setCost(cost);
        return card;
    }
}
