package com.hb.dokkan.service.cards;

import com.hb.dokkan.common.domain.PageResponse;

/**
 * @Description 卡片服务
 * @Author stargazer
 * @Date 2025/12/10 22:32
 **/
public interface DokkanCardService {
    PageResponse<com.hb.dokkan.api.cards.domain.response.CardListResponse> cardList(com.hb.dokkan.api.cards.domain.request.CardQueryRequest request);
}
