package com.hb.dokkan.api.convert;

import com.hb.dokkan.api.cards.domain.request.CardQueryRequest;
import com.hb.dokkan.api.cards.domain.response.CardListResponse;
import com.hb.dokkan.service.domain.cards.query.CardQueryOption;
import com.hb.dokkan.service.domain.cards.vo.CardListVO;
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
    CardQueryOption queryRequestToDto(CardQueryRequest request);

    /**
     * 卡片列表VO转换为响应
     */
    List<CardListResponse> listVoToResponse(List<CardListVO> cardListVOS);
}
