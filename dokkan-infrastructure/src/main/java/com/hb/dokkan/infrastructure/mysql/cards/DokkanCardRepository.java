package com.hb.dokkan.infrastructure.mysql.cards;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.hb.dokkan.common.domain.po.mysql.cards.CardPO;
import com.hb.dokkan.infrastructure.mysql.cards.mapper.DokkanCardMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.List;
import com.hb.dokkan.common.domain.request.cards.CardTagBatchRequest;

/**
 * @Description 卡牌数据库层
 * @Author stargazer
 * @Date 2025/3/9 19:35
 **/
@Repository
public class DokkanCardRepository extends ServiceImpl<DokkanCardMapper, CardPO> {

    /**
     * 单次卡片批量查询的最大 ID 数，避免 SQL 参数过多。
     **/
    public static final int BATCH_QUERY_MAX_SIZE = 500;

    /**
     * 根据卡牌id查询卡牌信息
     **/
    public CardPO queryByCardId(Long cardId) {
        return this.lambdaQuery().eq(CardPO::getCardId, cardId).one();
    }

    /**
     * 批量查询卡牌Id
     **/
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

    /**
     * 只读取候选判定所需字段，避免扫描全部文本和 JSON 列。
     **/
    public List<CardPO> listSyncFlags() {
        return lambdaQuery().select(CardPO::getCardId, CardPO::getEzaFlag, CardPO::getSezaFlag,
                CardPO::getSourceHash).list();
    }

    /**
     * 事务内锁定本批已有行，防止同步覆盖并发人工标签。
     **/
    public List<CardPO> lockByCardIds(List<Long> cardIds) {
        List<CardPO> cards = new java.util.ArrayList<>();
        if (cardIds == null || cardIds.isEmpty()) {
            return cards;
        }
        for (List<Long> batch : com.google.common.collect.Lists.partition(cardIds, BATCH_QUERY_MAX_SIZE)) {
            cards.addAll(lambdaQuery().in(CardPO::getCardId, batch).orderByAsc(CardPO::getCardId)
                    .last("FOR UPDATE").list());
        }
        return cards;
    }

    /**
     * 仅修改显式传入的标签并锁定，返回实际匹配的卡数；调用方负责事务与 ES 刷新。
     **/
    public int updateTags(List<Long> cardIds, CardTagBatchRequest request) {
        List<CardPO> cards = lockByCardIds(cardIds);
        if (cards.isEmpty()) {
            return 0;
        }
        lambdaUpdate().in(CardPO::getCardId, cardIds)
                .set(request.getDokkanFesFlag() != null, CardPO::getDokkanFesFlag, request.getDokkanFesFlag())
                .set(request.getCarnivalFlag() != null, CardPO::getCarnivalFlag, request.getCarnivalFlag())
                .set(request.getFreeCardFlag() != null, CardPO::getFreeCardFlag, request.getFreeCardFlag())
                .set(CardPO::getTagLocked, true).update();
        return cards.size();
    }
}
