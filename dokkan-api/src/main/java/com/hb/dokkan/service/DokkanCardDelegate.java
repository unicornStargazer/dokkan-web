package com.hb.dokkan.service;

import com.hb.dokkan.common.constants.CardSyncConstants;
import com.hb.dokkan.common.constants.ExceptionErrorCode;
import com.hb.dokkan.common.domain.dto.cards.CardQueryOptionDTO;
import com.hb.dokkan.common.domain.request.cards.CardQueryRequest;
import com.hb.dokkan.common.domain.request.cards.CardTagBatchRequest;
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

    /**
     * 卡片查询参数及展示响应转换器。
     **/
    @Resource
    private DokkanCardConvert dokkanCardConvert;

    /**
     * 卡片查询及人工标签业务服务。
     **/
    @Resource
    private DokkanCardService cardService;

    /**
     * 查询卡片分页列表，负责参数校验、请求转换和响应封装。
     *
     * @param request 卡片查询请求
     * @return 卡片分页列表响应
     **/
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
     **/
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
    /**
     * 校验至少一个非空标签以及去重后 1–500 个正整数卡片 ID。
     **/
    public DokkanResponse<Integer> batchUpdateTags(CardTagBatchRequest request) {
        if (!isValidTagRequest(request)) {
            throw new DokkanBizException(ExceptionErrorCode.QUERY_PARAM_ERROR);
        }
        return DokkanResponse.<Integer>builder().withModel(cardService.batchUpdateTags(request)).success();
    }

    /**
     * 逐项检查卡片 ID 与标签，保持先判空再访问字段的短路顺序。
     **/
    private boolean isValidTagRequest(CardTagBatchRequest request) {
        if (request == null) {
            return false;
        } else if (request.getCardIds() == null) {
            return false;
        } else if (request.getCardIds().isEmpty()) {
            return false;
        } else if (request.getCardIds().stream().anyMatch(id -> id == null || id <= CardSyncConstants.MIN_VALID_CARD_ID)) {
            return false;
        } else if (request.getCardIds().stream().distinct().count() > CardSyncConstants.CARD_TAG_BATCH_MAX_COUNT) {
            return false;
        } else {
            return hasTagChanges(request);
        }
    }

    /**
     * 至少一个标签显式传值才允许更新，false 同样代表有效修改。
     **/
    private boolean hasTagChanges(CardTagBatchRequest request) {
        if (request.getDokkanFesFlag() != null) {
            return true;
        } else if (request.getCarnivalFlag() != null) {
            return true;
        } else {
            return request.getFreeCardFlag() != null;
        }
    }
}
