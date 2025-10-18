package com.hb.dokkan.infrastructure.mysql.cards;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.hb.dokkan.infrastructure.mysql.cards.domain.CardPO;
import com.hb.dokkan.infrastructure.mysql.cards.mapper.DokkanCardMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Description 卡牌数据库层
 * @Author stargazer
 * @Date 2025/3/9 19:35
 **/
@Repository
public class DokkanCardRepository extends ServiceImpl<DokkanCardMapper, CardPO> {

    public static final int BATCH_QUERY_MAX_SIZE = 500;

    /**
     * 根据卡牌id查询卡牌信息
     */
    public CardPO queryByCardId(Long cardId) {
        return this.lambdaQuery().eq(CardPO::getCardId, cardId).one();
    }

    /**
     * 批量查询卡牌Id
     */
    public List<CardPO> batchQueryByCardIds(List<Long> cardIds) {
        if (CollectionUtils.isEmpty(cardIds)) {
            return Lists.newArrayList();
        }
        if (cardIds.size() <= BATCH_QUERY_MAX_SIZE) {
            return this.lambdaQuery().in(CardPO::getCardId, cardIds).list();
        }
        List<CardPO> result = Lists.newArrayList();
        for (int startIndex = 0; startIndex < cardIds.size(); startIndex += BATCH_QUERY_MAX_SIZE) {
            int endIndex = Math.min(cardIds.size(), startIndex + BATCH_QUERY_MAX_SIZE);
            List<Long> batchCardIds = cardIds.subList(startIndex, endIndex);
            List<CardPO> batchResult = this.lambdaQuery().in(CardPO::getCardId, batchCardIds).list();
            if (!CollectionUtils.isEmpty(batchResult)) {
                result.addAll(batchResult);
            }
        }
        return result;
    }

}
