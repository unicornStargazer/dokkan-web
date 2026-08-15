package com.hb.dokkan.service.convert;

import com.hb.dokkan.common.domain.po.mysql.translation.TranslationMappingPO;
import com.hb.dokkan.common.domain.request.translation.TranslationMappingSaveRequest;
import com.hb.dokkan.common.domain.response.translation.TranslationMappingResponse;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * @Description 翻译映射转换类
 * @Author stargazer
 * @Date 2026/8/15 23:30
 **/
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface TranslationMappingConvert {

    /**
     * 翻译映射保存请求转换为数据库对象。
     *
     * @param request 保存请求
     * @return 翻译映射数据库对象
     */
    TranslationMappingPO saveRequestToPo(TranslationMappingSaveRequest request);

    /**
     * 翻译映射数据库对象转换为响应对象。
     *
     * @param mapping 翻译映射数据库对象
     * @return 翻译映射响应对象
     */
    TranslationMappingResponse poToResponse(TranslationMappingPO mapping);

    /**
     * 翻译映射数据库对象列表转换为响应对象列表。
     *
     * @param mappings 翻译映射数据库对象列表
     * @return 翻译映射响应对象列表
     */
    List<TranslationMappingResponse> poListToResponseList(List<TranslationMappingPO> mappings);
}
