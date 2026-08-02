package com.hb.dokkan.api.cards;

import com.alibaba.fastjson.JSON;
import com.hb.dokkan.common.domain.request.cards.CardQueryRequest;
import com.hb.dokkan.common.domain.response.base.DokkanResponse;
import com.hb.dokkan.common.domain.response.base.PageResponse;
import com.hb.dokkan.common.domain.response.cards.CardDetailResponse;
import com.hb.dokkan.common.domain.response.cards.CardListResponse;
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
     * 卡片列表
     */
    @PostMapping("/list")
    public DokkanResponse<PageResponse<CardListResponse>> cardList(@RequestBody CardQueryRequest request){
        log.info("cardList request:{}", JSON.toJSONString(request));
        return cardDelegate.cardList(request);
    }

    /**
     * 卡片详情
     */
    @PostMapping("/detail")
    public DokkanResponse<CardDetailResponse> cardDetail(@RequestBody CardQueryRequest request){
        log.info("cardDetail request:{}",JSON.toJSONString(request));
        return cardDelegate.cardDetail(request);
    }

    
}
