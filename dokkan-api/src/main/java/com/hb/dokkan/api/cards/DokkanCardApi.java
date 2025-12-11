package com.hb.dokkan.api.cards;

import com.hb.dokkan.api.cards.domain.request.CardQueryRequest;
import com.hb.dokkan.api.cards.domain.response.CardListResponse;
import com.hb.dokkan.common.domain.DokkanResponse;
import com.hb.dokkan.common.domain.PageResponse;
import com.hb.dokkan.service.cards.DokkanCardService;
import com.hb.dokkan.api.convert.DokkanCardConvert;
import com.hb.dokkan.service.domain.cards.vo.CardListVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    private DokkanCardService cardService;

    @Resource
    private DokkanCardConvert dokkanCardConvert;

    /**
     * 卡片列表
     */
    @PostMapping("/list")
    public DokkanResponse<PageResponse<CardListResponse>> cardList(@RequestBody CardQueryRequest request){
        List<CardListVO> cardListVOS = cardService.cardList(dokkanCardConvert.queryRequestToDto(request));
        List<CardListResponse> response = dokkanCardConvert.listVoToResponse(cardListVOS);
        PageResponse<CardListResponse> pageResponse = PageResponse.<CardListResponse>builder()
                .currentPage(request.getPageNum())
                .pageSize(request.getPageSize())
                .total(cardListVOS.size())
                .data(response)
                .build();
        return DokkanResponse.<PageResponse<CardListResponse>>builder().withModel(pageResponse);
    }

    
}
