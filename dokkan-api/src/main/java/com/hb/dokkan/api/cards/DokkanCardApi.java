package com.hb.dokkan.api.cards;

import com.hb.dokkan.common.domain.request.cards.CardQueryRequest;
import com.hb.dokkan.common.domain.request.cards.CardRetranslateRequest;
import com.hb.dokkan.common.domain.response.base.DokkanResponse;
import com.hb.dokkan.common.domain.response.base.PageResponse;
import com.hb.dokkan.common.domain.response.cards.CardDetailResponse;
import com.hb.dokkan.common.domain.response.cards.CardListResponse;
import com.hb.dokkan.common.utils.JsonUtils;
import com.hb.dokkan.service.DokkanCardDelegate;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description card api
 * @Author stargazer
 * @Date 2025/12/10 21:56
 **/
@Slf4j
@RestController
@RequestMapping("/cards")
public class DokkanCardApi {

    @Resource
    private DokkanCardDelegate cardDelegate;

    /**
     * 查询卡片列表。
     *
     * @param request 卡片查询请求
     * @return 卡片分页列表响应
     */
    @PostMapping("/list")
    public DokkanResponse<PageResponse<CardListResponse>> cardList(@RequestBody CardQueryRequest request) {
        log.info("cardList request:{}", JsonUtils.object2Json(request));
        return cardDelegate.cardList(request);
    }

    /**
     * 重新翻译单张卡片。
     *
     * @param request 单卡重新翻译请求
     * @return 最新卡片详情响应
     */
    @PostMapping("/retranslate")
    public DokkanResponse<CardDetailResponse> retranslate(@RequestBody CardRetranslateRequest request) {
        log.info("retranslate request:{}", JsonUtils.object2Json(request));
        return cardDelegate.retranslate(request);
    }

    /**
     * 查询卡片详情。
     *
     * @param request 卡片查询请求
     * @return 卡片详情响应
     */
    @PostMapping("/detail")
    public DokkanResponse<CardDetailResponse> cardDetail(@RequestBody CardQueryRequest request) {
        log.info("cardDetail request:{}", JsonUtils.object2Json(request));
        return cardDelegate.cardDetail(request);
    }
}
