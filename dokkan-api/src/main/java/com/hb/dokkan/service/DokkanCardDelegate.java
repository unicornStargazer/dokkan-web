package com.hb.dokkan.service;

import com.hb.dokkan.common.domain.request.cards.CardQueryRequest;
import com.hb.dokkan.common.domain.response.cards.CardListResponse;
import com.hb.dokkan.common.domain.response.base.DokkanResponse;
import com.hb.dokkan.common.domain.response.base.PageResponse;

/**
 * @Description 控制层-卡牌服务
 * @Author stargazer
 * @Date 2025/12/12 23:22
 **/
public interface DokkanCardDelegate {
    /**
     * 查询卡片列表
     */
    DokkanResponse<PageResponse<CardListResponse>> cardList(CardQueryRequest request);
}
