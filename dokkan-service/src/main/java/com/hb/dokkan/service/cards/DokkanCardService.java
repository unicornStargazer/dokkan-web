package com.hb.dokkan.service.cards;

import com.hb.dokkan.service.domain.cards.query.CardQueryOption;
import com.hb.dokkan.service.domain.cards.vo.CardListVO;

import java.util.List;

/**
 * @Description 卡片服务
 * @Author stargazer
 * @Date 2025/12/10 22:32
 **/
public interface DokkanCardService {
    /**
     * 查询卡片列表
     */
    List<CardListVO> cardList(CardQueryOption queryOption);
}
