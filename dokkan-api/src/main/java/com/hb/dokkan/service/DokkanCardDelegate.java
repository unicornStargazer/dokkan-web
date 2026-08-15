package com.hb.dokkan.service;

import com.hb.dokkan.common.constants.ExceptionErrorCode;
import com.hb.dokkan.common.domain.dto.cards.CardQueryOptionDTO;
import com.hb.dokkan.common.domain.request.cards.CardQueryRequest;
import com.hb.dokkan.common.domain.response.base.DokkanResponse;
import com.hb.dokkan.common.domain.response.base.PageResponse;
import com.hb.dokkan.common.domain.response.cards.CardDetailResponse;
import com.hb.dokkan.common.domain.response.cards.CardListResponse;
import com.hb.dokkan.common.domain.vo.cards.CardDetailVO;
import com.hb.dokkan.common.domain.vo.cards.CardListVO;
import com.hb.dokkan.common.exception.domain.DokkanBizException;
import com.hb.dokkan.service.cards.DokkanCardService;
import com.hb.dokkan.service.convert.DokkanCardConvert;
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
public class DokkanCardDelegate {

    @Resource
    private DokkanCardConvert dokkanCardConvert;

    @Resource
    private DokkanCardService cardService;

    /**
     * 查询卡片分页列表，负责参数校验、请求转换和响应封装。
     *
     * @param request 卡片查询请求
     * @return 卡片分页列表响应
     */
    public DokkanResponse<PageResponse<CardListResponse>> cardList(CardQueryRequest request) {
        if (Objects.isNull(request)) {
            throw new DokkanBizException(ExceptionErrorCode.QUERY_PARAM_ERROR);
        }
        // 初始化分页参数，兜底不传分页参数导致查询数据过多。
        CardQueryOptionDTO queryOption = dokkanCardConvert.queryRequestToDto(request);
        queryOption.initPageable();
        // 查询业务数据后统一转换为 API 响应模型。
        PageResponse<CardListVO> response = cardService.cardList(queryOption);
        List<CardListResponse> list = dokkanCardConvert.listVoToResponseList(response.getData());
        PageResponse<CardListResponse> pageResponse = PageResponse.<CardListResponse>builder()
                .currentPage(response.getCurrentPage())
                .pageSize(response.getPageSize())
                .total(response.getTotal())
                .data(list)
                .build();
        return DokkanResponse.<PageResponse<CardListResponse>>builder()
                .withModel(pageResponse)
                .success();
    }

    /**
     * 查询卡片详情，负责参数校验、请求转换和响应封装。
     *
     * @param request 卡片查询请求
     * @return 卡片详情响应
     */
    public DokkanResponse<CardDetailResponse> cardDetail(CardQueryRequest request) {
        if (Objects.isNull(request) || Objects.isNull(request.getCardId())) {
            throw new DokkanBizException(ExceptionErrorCode.QUERY_PARAM_ERROR);
        }
        CardQueryOptionDTO queryOption = dokkanCardConvert.queryRequestToDto(request);
        CardDetailVO cardDetailVO = cardService.cardDetail(queryOption);
        CardDetailResponse response = dokkanCardConvert.detailVoToResponse(cardDetailVO);
        return DokkanResponse.<CardDetailResponse>builder()
                .withModel(response)
                .success();
    }
}
