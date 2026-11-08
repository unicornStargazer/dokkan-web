package com.hb.dokkan.api.cards;

import com.hb.dokkan.common.domain.request.cards.CardQueryRequest;
import com.hb.dokkan.common.domain.request.cards.CardTagBatchRequest;
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

    /**
     * 卡片接口参数校验及响应编排服务。
     **/
    @Resource
    private DokkanCardDelegate cardDelegate;

    /**
     * 查询卡片列表。
     *
     * @param request 卡片查询请求
     * @return 卡片分页列表响应
     **/
    @PostMapping("/list")
    public DokkanResponse<PageResponse<CardListResponse>> cardList(@RequestBody CardQueryRequest request) {
        log.info("cardList request:{}", JsonUtils.object2Json(request));
        DokkanResponse<PageResponse<CardListResponse>> response = cardDelegate.cardList(request);
        log.info("cardList response:{}", JsonUtils.object2Json(response));
        return response;
    }

    /**
     * 查询卡片详情。
     *
     * @param request 卡片查询请求
     * @return 卡片详情响应
     **/
    @PostMapping("/detail")
    public DokkanResponse<CardDetailResponse> cardDetail(@RequestBody CardQueryRequest request) {
        log.info("cardDetail request:{}", JsonUtils.object2Json(request));
        DokkanResponse<CardDetailResponse> response = cardDelegate.cardDetail(request);
        log.info("cardDetail response:{}", JsonUtils.object2Json(response));
        return response;
    }
    /**
     * 批量设置卡片标记，返回实际更新的卡数。
     **/
    @PostMapping("/tags/batch")
    public DokkanResponse<Integer> batchUpdateTags(@RequestBody CardTagBatchRequest request) {
        return cardDelegate.batchUpdateTags(request);
    }
}
