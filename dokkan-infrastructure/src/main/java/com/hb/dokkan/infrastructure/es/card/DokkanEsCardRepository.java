package com.hb.dokkan.infrastructure.es.card;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch._types.Refresh;
import com.hb.dokkan.common.constants.ExceptionErrorCode;
import com.hb.dokkan.common.exception.domain.DokkanBizException;
import com.hb.dokkan.common.domain.dto.cards.CardQueryConditionDTO;
import com.hb.dokkan.common.domain.po.es.cards.CardEsPO;
import com.hb.dokkan.common.utils.CollectionUtils;
import com.hb.dokkan.common.utils.MapUtils;
import com.hb.dokkan.common.utils.StringUtils;
import com.hb.dokkan.infrastructure.es.card.mapper.DokkanEsCardMapper;
import org.springframework.http.HttpStatusCode;
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
import java.io.IOException;

/**
 * @Description es卡片持久层服务类
 * @Author stargazer
 * @Date 2025/12/11 23:00
 **/
@Component
@Slf4j
public class DokkanEsCardRepository {

    /**
     * 卡片 ES 索引查询及索引管理 Mapper。
     **/
    private final DokkanEsCardMapper dokkanEsCardMapper;
    /**
     * ES 批量文档读写客户端。
     **/
    private final ElasticsearchClient elasticsearchClient;

    /**
     * 注入查询 Mapper 与批量整文档写入客户端。
     **/
    public DokkanEsCardRepository(DokkanEsCardMapper dokkanEsCardMapper, ElasticsearchClient elasticsearchClient) {
        this.dokkanEsCardMapper = dokkanEsCardMapper;
        this.elasticsearchClient = elasticsearchClient;
    }

    /**
     * 按业务主键替换完整文档；创建和覆盖均视为成功，避免部分更新遗留已清空字段。
     * 任一 item 失败均抛错，调用方可安全重放整个批次。
     * @param documents 本批完整卡片文档
     * @return 成功写入数量
     **/
    public int saveDocuments(List<CardEsPO> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return 0;
        }
        BulkRequest.Builder request = new BulkRequest.Builder().refresh(Refresh.WaitFor);
        documents.forEach(document -> request.operations(operation -> operation.index(index -> index
                .index(DokkanEsCardMapper.INDEX_NAME).id(document.getId()).document(document))));
        try {
            BulkResponse response = elasticsearchClient.bulk(request.build());
            if (!isSuccessfulBulkResponse(response, documents.size())) {
                log.error("ES card bulk write partially failed, cardIds={}, response={}",
                        documents.stream().map(CardEsPO::getCardId).toList(), response);
                throw new DokkanBizException(ExceptionErrorCode.INSERT_PARAM_ERROR);
            }
            return documents.size();
        } catch (IOException exception) {
            log.error("ES card bulk write failed, cardIds={}", documents.stream().map(CardEsPO::getCardId).toList(), exception);
            throw new DokkanBizException(ExceptionErrorCode.INSERT_PARAM_ERROR, exception);
        }
    }

    /**
     * 先检查批次标记及回包数量，再逐项确认写入成功，避免部分失败被忽略。
     **/
    private boolean isSuccessfulBulkResponse(BulkResponse response, int expectedCount) {
        if (response.errors()) {
            return false;
        } else if (response.items().size() != expectedCount) {
            return false;
        } else {
            return response.items().stream().allMatch(item -> item.error() == null
                    && HttpStatusCode.valueOf(item.status()).is2xxSuccessful());
        }
    }

    /**
     * 查询卡片列表
     **/
    public Optional<EsPageInfo<CardEsPO>> queryCardList(CardQueryConditionDTO queryOption) {
        LambdaEsQueryWrapper<CardEsPO> wrapper = EsWrappers.lambdaQuery(CardEsPO.class);
        wrapper
                .eq(Objects.nonNull(queryOption.getCardId()), CardEsPO::getCardId, queryOption.getCardId())
                .eq(StringUtils.isNotBlank(queryOption.getType()), CardEsPO::getType, queryOption.getType())
                .eq(StringUtils.isNotBlank(queryOption.getPropType()), CardEsPO::getPropType, queryOption.getPropType())
                .eq(StringUtils.isNotBlank(queryOption.getRarity()), CardEsPO::getRarity, queryOption.getRarity())
                .eq(Objects.nonNull(queryOption.getCost()), CardEsPO::getCost, queryOption.getCost())
                .eq(Objects.nonNull(queryOption.getDokkanFesFlag()), CardEsPO::getDokkanFesFlag, queryOption.getDokkanFesFlag())
                .eq(Objects.nonNull(queryOption.getCarnivalFlag()), CardEsPO::getCarnivalFlag, queryOption.getCarnivalFlag())
                .eq(Objects.nonNull(queryOption.getFreeCardFlag()), CardEsPO::getFreeCardFlag, queryOption.getFreeCardFlag())
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
     **/
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
     **/
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
