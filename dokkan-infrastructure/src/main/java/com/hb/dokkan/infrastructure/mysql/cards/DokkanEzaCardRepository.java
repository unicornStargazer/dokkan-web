package com.hb.dokkan.infrastructure.mysql.cards;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.hb.dokkan.common.domain.po.mysql.cards.EzaCardPO;
import com.hb.dokkan.infrastructure.mysql.cards.mapper.DokkanEzaCardMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Description 极限卡牌数据库层
 * @Author stargazer
 * @Date 2025/3/9 19:35
 **/
@Repository
public class DokkanEzaCardRepository extends ServiceImpl<DokkanEzaCardMapper, EzaCardPO> {

    public static final int BATCH_QUERY_MAX_SIZE = 500;

    /**
     * 批量查询卡牌Id
     */
    public List<EzaCardPO> batchQueryByCardIds(List<Long> cardIds) {
        if (CollectionUtils.isEmpty(cardIds)) {
            return Lists.newArrayList();
        }
        if (cardIds.size() <= BATCH_QUERY_MAX_SIZE) {
            return this.lambdaQuery().in(EzaCardPO::getCardId, cardIds).list();
        }
        return this.lambdaQuery().in(EzaCardPO::getCardId, cardIds).list();
    }

}
