package com.hb.dokkan.service.job.sync.strategy.strategies;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hb.dokkan.common.domain.dto.cards.CardAttributeDTO;
import com.hb.dokkan.common.domain.po.mysql.cards.CardPO;
import com.hb.dokkan.common.domain.po.mysql.cards.EzaCardPO;
import com.hb.dokkan.common.enums.CardTransformationTypeEnum;
import com.hb.dokkan.common.enums.FixDataTypeEnum;
import com.hb.dokkan.common.utils.CollectionUtils;
import com.hb.dokkan.common.utils.JsonUtils;
import com.hb.dokkan.common.utils.StringUtils;
import com.hb.dokkan.infrastructure.mysql.cards.DokkanCardRepository;
import com.hb.dokkan.infrastructure.mysql.cards.DokkanEzaCardRepository;
import com.hb.dokkan.service.job.sync.strategy.FixDataStrategy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @description 修复数据库eza发布时间
 * @author huangbiao
 * @date 2026/1/5 13:56
 **/
@Component
@Slf4j
public class FixDbEzaPublishTimeStrategy implements FixDataStrategy {

    @Resource
    private DokkanEzaCardRepository ezaCardRepository;

    @Resource
    private DokkanCardRepository cardRepository;

    @Resource(name = "defaultTransactionTemplate")
    private TransactionTemplate transactionTemplate;

    @Override
    public FixDataTypeEnum getFixDataType() {
        return FixDataTypeEnum.FIX_DB_EZA_PUBLISH_TIME;
    }

    /**
     * 先从card里查出最初变身前cardId
     * 再拿最初变身前cardId查询ezaCard获取发布时间
     */
    @Override
    public void fixData() {
        LambdaQueryWrapper<EzaCardPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.isNull(EzaCardPO::getPublishTime);
        // 查询极限发布时间为空的数据
        List<EzaCardPO> nullList = ezaCardRepository.list(queryWrapper);
        if (CollectionUtils.isEmpty(nullList)) {
            log.warn("has no null publishTime data");
            return;
        }
        List<Long> ezaCardIds = nullList.stream().map(EzaCardPO::getCardId).toList();
        // 查询最初变身前cardId
        List<CardPO> cardPOS = cardRepository.batchQueryByCardIds(ezaCardIds);
        if (CollectionUtils.isEmpty(cardPOS)) {
            log.warn("has no card data, ezaCardIds:{}", ezaCardIds);
            return;
        }
        Map<Long, CardPO> cardMap = cardPOS
                .stream()
                .collect(Collectors.toMap(CardPO::getCardId, Function.identity(), (oldValue, newValue) -> newValue));
        // key:ezaCardId, value:初始变身id
        Map<Long, Long> initCardMap = Maps.newHashMap();
        ezaCardIds.forEach(ezaCardId -> {
            if (cardMap.containsKey(ezaCardId)) {
                getInitCardId(cardMap.get(ezaCardId),initCardMap);
            }
        });
        List<EzaCardPO> initEzaInfos = ezaCardRepository.batchQueryByCardIds(Lists.newArrayList(initCardMap.values()));
        if (CollectionUtils.isEmpty(initEzaInfos)) {
            log.warn("has no init card data, ezaCardIds:{}", ezaCardIds);
            return;
        }
        Map<Long, EzaCardPO> initCardInfoMap = initEzaInfos.stream()
                .collect(Collectors.toMap(EzaCardPO::getCardId, Function.identity(), (oldValue, newValue) -> newValue));
        Map<Long, Date> initPublishTimeMap = Maps.newHashMap();
        initCardMap.forEach((ezaCardId, initCardId) -> {
            if (initCardInfoMap.containsKey(initCardId)) {
                initPublishTimeMap.put(ezaCardId, initCardInfoMap.get(initCardId).getPublishTime());
            }
        });
        //todo 似乎没法解决超极限的情况-目前不存在可变身的超极限
        nullList.forEach(
                ezaCardPO -> {
                    if (initPublishTimeMap.containsKey(ezaCardPO.getCardId())) {
                        ezaCardPO.setPublishTime(initPublishTimeMap.get(ezaCardPO.getCardId()));
                    }
                }
        );
        transactionTemplate.execute(status -> {
            try {
                ezaCardRepository.saveOrUpdateBatch(nullList);
            } catch (Exception e) {
                status.setRollbackOnly();
                log.error("FixDbEzaPublishTimeStrategy#fixData error :{}", e.getMessage(), e);
            }
            return null;
        });
    }

    /**
     * 获取最初变身前cardId
     */
    private void getInitCardId(CardPO card, Map<Long, Long> initCardMap) {
        if (Objects.isNull(card) || StringUtils.isBlank(card.getAttributes())) {
            return;
        }
        String attributes = card.getAttributes();
        CardAttributeDTO cardAttr = JsonUtils.json2Object(attributes, CardAttributeDTO.class);
        if (Objects.isNull(cardAttr) || CollectionUtils.isEmpty(cardAttr.getNextCards())) {
            return;
        }
        cardAttr.getNextCards().forEach(wikiCardTransformationDTO -> {
            if (Objects.nonNull(wikiCardTransformationDTO.getNextCard()) && CardTransformationTypeEnum.INIT.getType() == wikiCardTransformationDTO.getNextCard().getType()) {
                initCardMap.put(card.getCardId(), wikiCardTransformationDTO.getNextCard().getId());
            }
        });
    }
}
