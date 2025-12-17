package com.hb.dokkan.service.convert;

import com.google.common.collect.Lists;
import com.hb.dokkan.common.domain.dto.cards.CardQueryConditionDTO;
import com.hb.dokkan.common.domain.dto.cards.CardQueryOptionDTO;
import com.hb.dokkan.common.domain.po.es.cards.CardEsPO;
import com.hb.dokkan.common.domain.request.cards.CardQueryRequest;
import com.hb.dokkan.common.domain.response.cards.CardDetailResponse;
import com.hb.dokkan.common.domain.response.cards.CardListResponse;
import com.hb.dokkan.common.domain.vo.cards.CardDetailVO;
import com.hb.dokkan.common.domain.vo.cards.CardListVO;
import org.mapstruct.*;

import java.util.List;
import java.util.Objects;

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
    @Mappings({
            @Mapping(source = "categories", target = "categoryList", qualifiedByName = "convertStringToList"),
            @Mapping(source = "links", target = "linkList", qualifiedByName = "convertStringToList")
            })
    CardListResponse listVoToResponse(CardListVO cardListVO);

    /**
     * 卡片列表查询参数转换为es查询条件
     */
    CardQueryConditionDTO convertToEsQueryCondition(CardQueryOptionDTO queryOption);

    /**
     * es卡片列表PO转换为VO
     */
    List<CardListVO> convertToCardListVO(List<CardEsPO> cardEsPOS);

    @Named("convertStringToList")
    default List<String> convertStringToList(String str) {
        if (Objects.isNull(str)) {
            return Lists.newArrayList();
        }
        return Lists.newArrayList(str.split(","));
    }

    /**
     * 卡片详情VO转换为响应
     */
    CardDetailResponse detailVoToResponse(CardDetailVO cardDetailVO);

    /**
     * es卡片详情PO转换为VO
     */
    CardDetailVO convertToCardDetailVO(CardEsPO cardEsPO);
}
