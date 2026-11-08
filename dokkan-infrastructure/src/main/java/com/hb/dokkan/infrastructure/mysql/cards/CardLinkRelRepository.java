package com.hb.dokkan.infrastructure.mysql.cards;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import com.hb.dokkan.common.constants.CardSyncConstants;
import com.hb.dokkan.common.domain.po.mysql.cards.CardLinkRelPO;
import com.hb.dokkan.infrastructure.mysql.cards.mapper.CardLinkRelMapper;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;

/**
 * CardLinkRel 仓储，统一限制批量 SQL 的卡片数。
 **/
@Repository
public class CardLinkRelRepository extends ServiceImpl<CardLinkRelMapper, CardLinkRelPO> {
    /**
     * 按卡片 ID 分批读取子表，空参数返回空集合。
     **/
    public List<CardLinkRelPO> queryByCardIds(List<Long> cardIds) {
        List<CardLinkRelPO> rows = new ArrayList<>();
        if (cardIds == null || cardIds.isEmpty()) {
            return rows;
        }
        for (List<Long> batch : Lists.partition(cardIds, CardSyncConstants.CARD_SAVE_BATCH_SIZE)) {
            rows.addAll(lambdaQuery().in(CardLinkRelPO::getCardId, batch).list());
        }
        return rows;
    }

    /**
     * 按卡片 ID 分批删除，必须与后续批量插入放在同一个事务中。
     **/
    public void deleteByCardIds(List<Long> cardIds) {
        if (cardIds == null || cardIds.isEmpty()) {
            return;
        }
        for (List<Long> batch : Lists.partition(cardIds, CardSyncConstants.CARD_SAVE_BATCH_SIZE)) {
            remove(Wrappers.<CardLinkRelPO>lambdaQuery().in(CardLinkRelPO::getCardId, batch));
        }
    }

    /**
     * 分批插入子表记录；空集合不发 SQL。
     **/
    public void insertBatch(List<CardLinkRelPO> rows) {
        if (!hasRowsToInsert(rows)) {
            return;
        }
        if (!saveBatch(rows, CardSyncConstants.CARD_SAVE_BATCH_SIZE)) {
            throw new IllegalStateException("CardLinkRel batch insert failed");
        }
    }

    /**
     * 判断是否有需要批量插入的子表记录。
     **/
    private boolean hasRowsToInsert(List<CardLinkRelPO> rows) {
        if (rows == null) {
            return false;
        } else {
            return !rows.isEmpty();
        }
    }
}
