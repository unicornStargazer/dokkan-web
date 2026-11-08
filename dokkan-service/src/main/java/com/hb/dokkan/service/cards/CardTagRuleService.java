package com.hb.dokkan.service.cards;

import com.hb.dokkan.common.constants.CardSyncConstants;
import com.hb.dokkan.common.domain.po.mysql.cards.CardPO;
import org.springframework.stereotype.Component;

/**
 * 卡片限定标记规则：人工值优先，自动规则仅设置 UR cost 48/58 的限定。
 **/
@Component
public class CardTagRuleService {
    /**
     * 合并已有人工/历史标记后应用自动规则，其他标记保持旧值。
     **/
    public void apply(CardPO incoming, CardPO existing) {
        incoming.setDokkanFesFlag(existing != null && Boolean.TRUE.equals(existing.getDokkanFesFlag()));
        incoming.setCarnivalFlag(existing != null && Boolean.TRUE.equals(existing.getCarnivalFlag()));
        incoming.setFreeCardFlag(existing != null && Boolean.TRUE.equals(existing.getFreeCardFlag()));
        incoming.setTagLocked(existing != null && Boolean.TRUE.equals(existing.getTagLocked()));
        if (shouldApplyDokkanFesTag(incoming)) {
            incoming.setDokkanFesFlag(true);
        }
    }

    /**
     * 按人工锁定、UR 稀有度及限定 cost 依次判断是否自动设置限定。
     **/
    private boolean shouldApplyDokkanFesTag(CardPO card) {
        if (Boolean.TRUE.equals(card.getTagLocked())) {
            return false;
        } else if (!Integer.valueOf(CardSyncConstants.LIMITED_RULE_UR_RARITY).equals(card.getRarity())) {
            return false;
        } else if (Integer.valueOf(CardSyncConstants.LIMITED_RULE_COST_48).equals(card.getCost())) {
            return true;
        } else {
            return Integer.valueOf(CardSyncConstants.LIMITED_RULE_COST_58).equals(card.getCost());
        }
    }
}
