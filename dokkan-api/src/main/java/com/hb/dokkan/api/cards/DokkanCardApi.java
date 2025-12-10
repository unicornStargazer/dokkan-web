package com.hb.dokkan.api.cards;

import com.hb.dokkan.api.cards.domain.request.CardQueryRequest;
import com.hb.dokkan.api.cards.domain.response.CardListResponse;
import com.hb.dokkan.common.domain.DokkanResponse;
import com.hb.dokkan.common.domain.PageResponse;
import lombok.extern.slf4j.Slf4j;
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

    public DokkanResponse<PageResponse<CardListResponse>> cardList(@RequestBody CardQueryRequest request){
        return null;
    }

    
}
