package com.hb.dokkan.service;

import com.hb.dokkan.api.cards.domain.request.CardQueryRequest;
import com.hb.dokkan.api.cards.domain.response.CardListResponse;
import com.hb.dokkan.common.domain.DokkanResponse;
import com.hb.dokkan.common.domain.PageResponse;

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
