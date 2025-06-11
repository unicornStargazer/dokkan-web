package com.hb.dokkan.service.job.sync.impl;

import com.alibaba.fastjson.JSON;
import com.hb.dokkan.common.constants.ResponseErrorCode;
import com.hb.dokkan.common.exception.domain.DokkanBizException;
import com.hb.dokkan.infrastructure.cards.DokkanCardRepository;
import com.hb.dokkan.infrastructure.cards.domain.CardPO;
import com.hb.dokkan.service.convert.DokkanSyncConvert;
import com.hb.dokkan.service.domain.CardBaseInfoDTO;
import com.hb.dokkan.service.domain.sync.WikiCardDTO;
import com.hb.dokkan.service.job.sync.SyncDataService;
import com.hb.dokkan.service.job.sync.factory.WikiInfoStrategyFactory;
import com.hb.dokkan.service.job.sync.strategy.WikiInfoStrategy;
import com.hb.dokkan.service.job.sync.strategy.context.WikiContext;
import com.hb.dokkan.service.job.sync.strategy.enums.WikiInfoTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Description xxxxx
 * @Author stargazer
 * @Date 2025/6/2 0:13
 **/
@Slf4j
@Component
public class SyncDataServiceImpl implements SyncDataService {
    @Resource
    private WikiInfoStrategyFactory strategyFactory;

    @Resource
    private DokkanCardRepository cardRepository;

    @Resource
    private DokkanSyncConvert convert;

    /**
     * 初始化卡片数据
     */
    @Override
    public void initCard() {
        WikiInfoStrategy strategy = strategyFactory.getWikiStrategy(WikiInfoTypeEnum.CARD);
        WikiContext context = new WikiContext();
        strategy.execute(context);
        List<WikiCardDTO> wikiCards = context.getWikiCards();
        if (CollectionUtils.isEmpty(wikiCards)) {
            log.error("初始化失败，获取卡片为空");
            return;
        }

        List<CardBaseInfoDTO> cards = wikiCards.stream()
                .map(WikiCardDTO::getCard)
                .toList();
        List<CardPO> cardPOS = convert.wikiCard2POList(cards);
        checkParam(cardPOS);
        cardRepository.saveBatch(cardPOS);
    }

    private void checkParam(List<CardPO> cardPOS) {
        cardPOS.forEach(card -> {
            if (StringUtils.isAnyBlank(card.getTitle(), card.getCardName()) || !ObjectUtils.allNotNull(
                    card.getCardId(), card.getHpValue(), card.getAtkValue(), card.getDefValue())) {
                log.error("param error,card:{}", JSON.toJSONString(card));
                throw new DokkanBizException(ResponseErrorCode.INSERT_PARAM_ERROR);
            }
        });
    }
}
