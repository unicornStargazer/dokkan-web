package com.hb.dokkan.service;

import com.hb.dokkan.common.constants.CardSyncConstants;
import com.hb.dokkan.common.constants.ExceptionErrorCode;
import com.hb.dokkan.common.domain.dto.cards.CardQueryOptionDTO;
import com.hb.dokkan.common.domain.request.cards.CardIdSyncRequest;
import com.hb.dokkan.common.domain.request.cards.CardQueryRequest;
import com.hb.dokkan.common.domain.request.cards.CardRetranslateRequest;
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
     * 重新翻译单张卡片，负责参数校验、请求转换和响应封装。
     *
     * @param request 单卡重新翻译请求
     * @return 最新卡片详情响应
     */
    public DokkanResponse<CardDetailResponse> retranslate(CardRetranslateRequest request) {
        if (Objects.isNull(request) || Objects.isNull(request.getCardId())
                || request.getCardId() <= CardSyncConstants.MIN_VALID_CARD_ID) {
            throw new DokkanBizException(ExceptionErrorCode.QUERY_PARAM_ERROR);
        }
        CardDetailVO cardDetailVO = cardService.retranslateCard(request.getCardId());
        CardDetailResponse response = dokkanCardConvert.detailVoToResponse(cardDetailVO);
        return DokkanResponse.<CardDetailResponse>builder()
                .withModel(response)
                .success();
    }

    /**
     * 批量重新翻译卡片，负责参数校验和响应封装。
     *
     * @param request 批量卡片 ID 请求
     * @return 成功重新翻译数量响应
     */
    public DokkanResponse<Integer> retranslateBatch(CardIdSyncRequest request) {
        if (Objects.isNull(request) || Objects.isNull(request.getCardIds())) {
            throw new DokkanBizException(ExceptionErrorCode.QUERY_PARAM_ERROR);
        }
        // 过滤非法 ID 并按原始勾选顺序去重，避免同一张卡重复触发外部 LLM。
        List<Long> cardIds = request.getCardIds().stream()
                .filter(Objects::nonNull)
                .filter(cardId -> cardId > CardSyncConstants.MIN_VALID_CARD_ID)
                .distinct()
                .toList();
        if (cardIds.isEmpty() || cardIds.size() > CardSyncConstants.MANUAL_SYNC_MAX_CARD_COUNT) {
            throw new DokkanBizException(ExceptionErrorCode.QUERY_PARAM_ERROR);
        }
        Integer count = cardService.retranslateCards(cardIds);
        return DokkanResponse.<Integer>builder()
                .withModel(count)
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
