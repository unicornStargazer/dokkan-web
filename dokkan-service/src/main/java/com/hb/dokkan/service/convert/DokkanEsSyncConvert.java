package com.hb.dokkan.service.convert;

import com.hb.dokkan.common.domain.po.es.cards.CardEsPotentialDTO;
import com.hb.dokkan.common.domain.dto.data.cards.PotentialDTO;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * @Description es同步转换类
 * @Author stargazer
 * @Date 2025/11/30 16:28
 **/
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface DokkanEsSyncConvert {
    /**
     * 转换为es卡片潜在属性dto
     */
    List<CardEsPotentialDTO> convert2PotentialDTO(List<PotentialDTO> potentials);
}
