package com.hb.dokkan.service.convert;

import com.hb.dokkan.common.domain.po.mysql.category.DokkanCategoryPO;
import com.hb.dokkan.common.domain.request.category.CategorySaveRequest;
import com.hb.dokkan.common.domain.response.category.CategoryResponse;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * @Description 分类管理转换类
 * @Author stargazer
 * @Date 2026/8/16 00:30
 **/
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface DokkanCategoryConvert {

    /**
     * 分类数据库对象转换为响应对象。
     *
     * @param category 分类数据库对象
     * @return 分类响应对象
     */
    CategoryResponse poToResponse(DokkanCategoryPO category);

    /**
     * 分类保存请求转换为数据库对象。
     *
     * @param request 分类保存请求
     * @return 分类数据库对象
     */
    DokkanCategoryPO saveRequestToPo(CategorySaveRequest request);

    /**
     * 分类数据库对象列表转换为响应对象列表。
     *
     * @param categories 分类数据库对象列表
     * @return 分类响应对象列表
     */
    List<CategoryResponse> poListToResponseList(List<DokkanCategoryPO> categories);
}
