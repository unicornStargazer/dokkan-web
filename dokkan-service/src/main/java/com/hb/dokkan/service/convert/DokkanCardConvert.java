package com.hb.dokkan.service.convert;

import com.hb.dokkan.common.domain.request.cards.CardQueryRequest;
import com.hb.dokkan.common.domain.response.cards.CardListResponse;
import com.hb.dokkan.common.domain.dto.cards.CardQueryConditionDTO;
import com.hb.dokkan.common.domain.po.es.cards.CardEsPO;
import com.hb.dokkan.common.domain.dto.cards.CardQueryOptionDTO;
import com.hb.dokkan.common.domain.vo.cards.CardListVO;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * @Description 卡片转换类
 * @Author stargazer
 * @Date 2025/12/11 22:36
 **/
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface DokkanCardConvert {
    /**
     * 卡片列表查询参数转换为DTO
     */
    CardQueryOptionDTO queryRequestToDto(CardQueryRequest request);

    /**
     * 卡片列表VO转换为响应
     */
    List<CardListResponse> listVoToResponseList(List<CardListVO> cardListVOS);

    /**
     * 卡片列表VO转换为响应
     */
    CardListResponse listVoToResponse(CardListVO cardListVO);

    /**
     * 卡片列表查询参数转换为es查询条件
     */
    CardQueryConditionDTO convertToEsQueryCondition(CardQueryOptionDTO queryOption);

    /**
     * es卡片列表PO转换为VO
     */
    List<CardListVO> convertToCardListVO(List<CardEsPO> cardEsPOS);
}
