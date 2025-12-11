package com.hb.dokkan.service.cards.impl;

import com.hb.dokkan.common.constants.ExceptionErrorCode;
import com.hb.dokkan.common.exception.domain.DokkanBizException;
import com.hb.dokkan.infrastructure.es.card.DokkanEsCardRepository;
import com.hb.dokkan.service.cards.DokkanCardService;
import com.hb.dokkan.service.domain.cards.query.CardQueryOption;
import com.hb.dokkan.service.domain.cards.vo.CardListVO;
import jakarta.annotation.Resource;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

/**
 * @Description 卡片服务实现类
 * @Author stargazer
 * @Date 2025/12/10 22:32
 **/
public class DokkanCardServiceImpl implements DokkanCardService {

    @Resource
    private DokkanEsCardRepository dokkanEsCardRepository;


    /**
     * 查询卡片列表
     *
     */
    @Override
    public List<CardListVO> cardList(CardQueryOption queryOption) {
        if (Objects.isNull(queryOption)) {
            throw new DokkanBizException(ExceptionErrorCode.QUERY_PARAM_ERROR);
        }
        //  初始化分页参数 兜底不传分页参数导致查询数据过多
        queryOption.initPageable();
        if (CollectionUtils.isEmpty(queryOption.getCategoryIds()) || CollectionUtils.isEmpty(queryOption.getLinkIds())) {
            return dokkanEsCardRepository.cardList(queryOption);
        }
    }
}
