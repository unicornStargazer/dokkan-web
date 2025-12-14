package com.hb.dokkan.infrastructure.es.card;

import com.hb.dokkan.common.utils.CollectionUtils;
import com.hb.dokkan.common.utils.MapUtils;
import com.hb.dokkan.common.utils.StringUtils;
import com.hb.dokkan.infrastructure.es.card.condition.CardQueryCondition;
import com.hb.dokkan.infrastructure.es.card.domain.CardEsPO;
import com.hb.dokkan.infrastructure.es.card.mapper.DokkanEsCardMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.dromara.easyes.core.biz.EsPageInfo;
import org.dromara.easyes.core.conditions.select.LambdaEsQueryChainWrapper;
import org.dromara.easyes.core.kernel.EsWrappers;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;

/**
 * @Description es卡片持久层服务类
 * @Author stargazer
 * @Date 2025/12/11 23:00
 **/
@Repository
@Slf4j
public class DokkanEsCardRepository {

    @Resource
    private DokkanEsCardMapper dokkanEsCardMapper;

    /**
     * 查询卡片列表
     */
    public Optional<EsPageInfo<CardEsPO>> queryCardList(CardQueryCondition queryOption) {
        LambdaEsQueryChainWrapper<CardEsPO> wrapper = EsWrappers.lambdaChainQuery(dokkanEsCardMapper);
        wrapper.in(CollectionUtils.isNotEmpty(queryOption.getCategories()), CardEsPO::getCategories, queryOption.getCategories())
                .in(CollectionUtils.isNotEmpty(queryOption.getLinks()), CardEsPO::getLinks, queryOption.getLinks())
                .eq(Objects.nonNull(queryOption.getCardId()), CardEsPO::getCardId, queryOption.getCardId())
                .eq(StringUtils.isNotBlank(queryOption.getType()), CardEsPO::getType, queryOption.getType())
                .eq(StringUtils.isNotBlank(queryOption.getPropType()), CardEsPO::getPropType, queryOption.getPropType())
                .eq(StringUtils.isNotBlank(queryOption.getRarity()), CardEsPO::getRarity, queryOption.getRarity())
                .like(StringUtils.isNotBlank(queryOption.getCardName()), CardEsPO::getCardName, queryOption.getCardName());
        if (MapUtils.isEmpty(queryOption.getOrderBy())) {
            wrapper.orderByDesc(CardEsPO::getPublishTime, CardEsPO::getEzaPublishTime, CardEsPO::getEzaPublishTime);
        } else {
            queryOption.getOrderBy().forEach((k, v) -> {
                if (v) {
                    wrapper.orderByDesc(k);
                } else {
                    wrapper.orderByAsc(k);
                }
            });
        }
        EsPageInfo<CardEsPO> esPage = dokkanEsCardMapper.pageQuery(wrapper, queryOption.getPageNum(), queryOption.getPageSize());
        return Optional.ofNullable(esPage);
    }


}
