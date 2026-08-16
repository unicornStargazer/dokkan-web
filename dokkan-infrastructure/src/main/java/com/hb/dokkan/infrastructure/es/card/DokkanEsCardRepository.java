package com.hb.dokkan.infrastructure.es.card;

import com.hb.dokkan.common.domain.dto.cards.CardQueryConditionDTO;
import com.hb.dokkan.common.domain.po.es.cards.CardEsPO;
import com.hb.dokkan.common.utils.CollectionUtils;
import com.hb.dokkan.common.utils.MapUtils;
import com.hb.dokkan.common.utils.StringUtils;
import com.hb.dokkan.infrastructure.es.card.mapper.DokkanEsCardMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.dromara.easyes.core.biz.EsPageInfo;
import org.dromara.easyes.core.conditions.select.LambdaEsQueryWrapper;
import org.dromara.easyes.core.kernel.EsWrappers;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @Description es卡片持久层服务类
 * @Author stargazer
 * @Date 2025/12/11 23:00
 **/
@Component
@Slf4j
public class DokkanEsCardRepository {

    @Resource
    private DokkanEsCardMapper dokkanEsCardMapper;

    /**
     * 查询卡片列表
     */
    public Optional<EsPageInfo<CardEsPO>> queryCardList(CardQueryConditionDTO queryOption) {
        LambdaEsQueryWrapper<CardEsPO> wrapper = EsWrappers.lambdaQuery(CardEsPO.class);
        wrapper
                .eq(Objects.nonNull(queryOption.getCardId()), CardEsPO::getCardId, queryOption.getCardId())
                .eq(StringUtils.isNotBlank(queryOption.getType()), CardEsPO::getType, queryOption.getType())
                .eq(StringUtils.isNotBlank(queryOption.getPropType()), CardEsPO::getPropType, queryOption.getPropType())
                .eq(StringUtils.isNotBlank(queryOption.getRarity()), CardEsPO::getRarity, queryOption.getRarity())
                .like(StringUtils.isNotBlank(queryOption.getCardName()), CardEsPO::getCardName, queryOption.getCardName());
        if (MapUtils.isEmpty(queryOption.getOrderBy())) {
            wrapper.orderByDesc(CardEsPO::getOrderByTime);
        } else {
            queryOption.getOrderBy().forEach((k, v) -> {
                if (v) {
                    wrapper.orderByDesc(k);
                } else {
                    wrapper.orderByAsc(k);
                }
            });
        }
        if (CollectionUtils.isNotEmpty(queryOption.getLinks())) {
            if (queryOption.getLinkMatchType().equals("0")) {
                queryOption.getLinks().forEach(link -> wrapper.and(w -> w.like(CardEsPO::getLinks, link)));
            }else {
                queryOption.getLinks().forEach(link -> wrapper.or(w -> w.like(CardEsPO::getLinks, link)));
            }
        }
        if (CollectionUtils.isNotEmpty(queryOption.getCategories())) {
            if (queryOption.getCategoryMatchType().equals("0")) {
                queryOption.getCategories().forEach(category -> wrapper.and(w -> w.like(CardEsPO::getCategories, category)));
            }else {
                queryOption.getCategories().forEach(category -> wrapper.or(w -> w.like(CardEsPO::getCategories, category)));

            }
        }
        EsPageInfo<CardEsPO> esPage = dokkanEsCardMapper.pageQuery(wrapper, queryOption.getPageNum(), queryOption.getPageSize());
        return Optional.ofNullable(esPage);
    }


    /**
     * 查询卡片详情
     */
    public Optional<CardEsPO> queryCardDetail(CardQueryConditionDTO cardQueryConditionDTO) {
        LambdaEsQueryWrapper<CardEsPO> wrapper = EsWrappers.lambdaQuery(CardEsPO.class);
        wrapper.eq(CardEsPO::getCardId, cardQueryConditionDTO.getCardId());
        List<CardEsPO> cardEsPOS = dokkanEsCardMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(cardEsPOS)) {
            return Optional.empty();
        }
        return Optional.ofNullable(cardEsPOS.getFirst());
    }

    /**
     * 批量查询已有卡片文档。
     *
     * @param cardIds 卡片 ID 列表
     * @return 已存在的 ES 文档列表
     */
    public List<CardEsPO> queryByCardIds(List<Long> cardIds) {
        if (CollectionUtils.isEmpty(cardIds)) {
            return List.of();
        }
        if (cardIds.size() <= 500) {
            LambdaEsQueryWrapper<CardEsPO> wrapper = EsWrappers.lambdaQuery(CardEsPO.class);
            wrapper.in(CardEsPO::getCardId, cardIds);
            List<CardEsPO> esCards = dokkanEsCardMapper.selectList(wrapper);
            return Objects.isNull(esCards) ? List.of() : esCards;
        }
        Map<Long, CardEsPO> result = new HashMap<>();
        for (int start = 0; start < cardIds.size(); start += 500) {
            int end = Math.min(cardIds.size(), start + 500);
            List<Long> batchIds = cardIds.subList(start, end);
            LambdaEsQueryWrapper<CardEsPO> wrapper = EsWrappers.lambdaQuery(CardEsPO.class);
            wrapper.in(CardEsPO::getCardId, batchIds);
            List<CardEsPO> batch = dokkanEsCardMapper.selectList(wrapper);
            if (!CollectionUtils.isEmpty(batch)) {
                batch.forEach(cardEsPO -> result.put(cardEsPO.getCardId(), cardEsPO));
            }
        }
        return List.copyOf(result.values());
    }
}
