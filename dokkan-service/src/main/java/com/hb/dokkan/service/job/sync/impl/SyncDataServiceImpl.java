package com.hb.dokkan.service.job.sync.impl;

import com.alibaba.fastjson.JSON;
import com.hb.dokkan.common.constants.ResponseErrorCode;
import com.hb.dokkan.common.exception.domain.DokkanBizException;
import com.hb.dokkan.infrastructure.cards.DokkanCardRepository;
import com.hb.dokkan.infrastructure.cards.DokkanEzaCardRepository;
import com.hb.dokkan.infrastructure.cards.DokkanSkillRepository;
import com.hb.dokkan.infrastructure.cards.DokkanSpecialRepository;
import com.hb.dokkan.infrastructure.cards.domain.CardPO;
import com.hb.dokkan.infrastructure.cards.domain.EzaCardPO;
import com.hb.dokkan.infrastructure.cards.domain.SkillPO;
import com.hb.dokkan.infrastructure.cards.domain.SpecialPO;
import com.hb.dokkan.service.convert.DokkanSyncConvert;
import com.hb.dokkan.service.domain.bo.WikiCardBO;
import com.hb.dokkan.service.domain.dto.base.CardBaseInfoDTO;
import com.hb.dokkan.service.domain.dto.base.EzaCardInfoDTO;
import com.hb.dokkan.service.domain.dto.base.SkillDTO;
import com.hb.dokkan.service.domain.dto.base.SpecialAttackDTO;
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
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

/**
 * @Description 同步数据服务
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
    private DokkanEzaCardRepository ezaCardRepository;

    @Resource
    private DokkanSkillRepository skillRepository;

    @Resource
    private DokkanSpecialRepository specialRepository;

    @Resource
    private DokkanSyncConvert convert;

    @Resource(name = "defaultTransactionTemplate")
    private TransactionTemplate transactionTemplate;

    /**
     * 初始化卡片数据
     */
    @Override
    public void initCard() {
        WikiInfoStrategy strategy = strategyFactory.getWikiStrategy(WikiInfoTypeEnum.CARD);
        WikiContext context = new WikiContext();
        strategy.execute(context);
        WikiCardBO cardData = context.getCardData();
        if (Objects.isNull(cardData)) {
            log.error("初始化失败，获取卡片为空");
            return;
        }
        transactionTemplate.execute(status -> {
            try{
                insertCardBaseInfo(cardData.getCardBaseData());
                insertDownPullSkillInfo(cardData.getDownPullSkills());
                insertEzaCardInfo(cardData.getEzaCardInfos());
                insertSpecialInfo(cardData.getSpecialAttacks());
            }catch (Exception e){
                log.error("SyncDataService#initCard error :{}", e.getMessage(),e);
                status.setRollbackOnly();
            }
            return null;
        });
    }

    private void insertSpecialInfo(List<SpecialAttackDTO> specialAttacks) {
        if (CollectionUtils.isEmpty(specialAttacks)) {
            return;
        }
        List<SpecialPO> specialPOS = convert.wikiSpecial2POList(specialAttacks);
        specialRepository.saveBatch(specialPOS);
    }

    private void insertEzaCardInfo(List<EzaCardInfoDTO> ezaCardInfos) {
        if (CollectionUtils.isEmpty(ezaCardInfos)) {
            return;
        }
        List<EzaCardPO> ezaCardPOS = convert.wikiEza2POList(ezaCardInfos);
        ezaCardRepository.saveBatch(ezaCardPOS);
    }

    private void insertDownPullSkillInfo(List<SkillDTO> downPullSkills) {
        List<SkillPO> skillPOS = convert.wikiSkill2POList(downPullSkills);
        if (CollectionUtils.isEmpty(skillPOS)) {
            return;
        }
        skillRepository.saveBatch(skillPOS);
    }

    private void insertCardBaseInfo(List<CardBaseInfoDTO> cards) {
        List<CardPO> cardModel = convert.wikiCard2POList(cards);
        checkParam(cardModel);
        cardRepository.saveBatch(cardModel, 500);
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
