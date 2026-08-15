package com.hb.dokkan.service;

import com.hb.dokkan.common.constants.ExceptionErrorCode;
import com.hb.dokkan.common.domain.request.category.CategoryQueryRequest;
import com.hb.dokkan.common.domain.request.category.CategorySaveRequest;
import com.hb.dokkan.common.domain.response.base.DokkanResponse;
import com.hb.dokkan.common.domain.response.base.PageResponse;
import com.hb.dokkan.common.domain.response.category.CategoryResponse;
import com.hb.dokkan.common.exception.domain.DokkanBizException;
import com.hb.dokkan.service.categories.DokkanCategoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Description 分类管理委托服务
 * @Author stargazer
 * @Date 2026/8/16 00:30
 **/
@Slf4j
@Service
public class DokkanCategoryDelegate {

    @Resource
    private DokkanCategoryService categoryService;

    /**
     * 分页查询分类列表。
     *
     * @param request 查询请求
     * @return 分类分页响应
     */
    public DokkanResponse<PageResponse<CategoryResponse>> list(CategoryQueryRequest request) {
        try {
            PageResponse<CategoryResponse> response = categoryService.queryPage(request);
            return DokkanResponse.<PageResponse<CategoryResponse>>builder()
                    .withModel(response)
                    .success();
        } catch (Exception e) {
            log.error("DokkanCategoryDelegate#list error", e);
            return DokkanResponse.<PageResponse<CategoryResponse>>builder().fail();
        }
    }

    /**
     * 新增或编辑分类。
     *
     * @param request 分类保存请求
     * @return 保存后的分类响应
     */
    public DokkanResponse<CategoryResponse> save(CategorySaveRequest request) {
        try {
            CategoryResponse response = categoryService.save(request);
            return DokkanResponse.<CategoryResponse>builder()
                    .withModel(response)
                    .success();
        } catch (DokkanBizException e) {
            log.warn("DokkanCategoryDelegate#save invalid request", e);
            ExceptionErrorCode error = e.getError() == null ? ExceptionErrorCode.CATEGORY_PARAM_ERROR : e.getError();
            return DokkanResponse.<CategoryResponse>builder()
                    .fail(error.getErrorCode(), error.getErrorMsg());
        } catch (Exception e) {
            log.error("DokkanCategoryDelegate#save error", e);
            return DokkanResponse.<CategoryResponse>builder().fail();
        }
    }
}
