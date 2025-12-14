package com.hb.dokkan.service.cards.impl;

import com.hb.dokkan.api.cards.domain.request.CardQueryRequest;
import com.hb.dokkan.api.cards.domain.response.CardListResponse;
import com.hb.dokkan.common.constants.ExceptionErrorCode;
import com.hb.dokkan.common.domain.DokkanResponse;
import com.hb.dokkan.common.domain.PageResponse;
import com.hb.dokkan.common.exception.domain.DokkanBizException;
import com.hb.dokkan.service.DokkanCardDelegate;
import com.hb.dokkan.service.cards.DokkanCardService;
import com.hb.dokkan.service.convert.DokkanCardConvert;
import com.hb.dokkan.service.domain.cards.query.CardQueryOption;
import com.hb.dokkan.service.domain.cards.vo.CardListVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @Description 卡牌服务实现类
 * @Author stargazer
 * @Date 2025/12/12 23:29
 **/
@Service
public class DokkanCardDelegateImpl implements DokkanCardDelegate {

    @Resource
    private DokkanCardConvert dokkanCardConvert;

    @Resource
    private DokkanCardService cardService;

    /**
     * 查询卡片列表
     *
     */
    @Override
    public DokkanResponse<PageResponse<CardListResponse>> cardList(CardQueryRequest request) {
        if (Objects.isNull(request)) {
            throw new DokkanBizException(ExceptionErrorCode.QUERY_PARAM_ERROR);
        }
        CardQueryOption queryOption = dokkanCardConvert.queryRequestToDto(request);
        //  初始化分页参数 兜底不传分页参数导致查询数据过多
        queryOption.initPageable();
        PageResponse<CardListVO> response = cardService.cardList(queryOption);
        List<CardListResponse> list = dokkanCardConvert.listVoToResponseList(response.getData());
        PageResponse<CardListResponse> pageResponse = PageResponse.<CardListResponse>builder()
                .currentPage(response.getCurrentPage())
                .pageSize(response.getPageSize())
                .total(response.getTotal())
                .data(list)
                .build();
        return DokkanResponse.<PageResponse<CardListResponse>>builder().withModel(pageResponse);
    }
}
