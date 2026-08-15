package com.hb.dokkan.api.categories;

import com.hb.dokkan.common.domain.request.category.CategoryQueryRequest;
import com.hb.dokkan.common.domain.request.category.CategorySaveRequest;
import com.hb.dokkan.common.domain.response.base.DokkanResponse;
import com.hb.dokkan.common.domain.response.base.PageResponse;
import com.hb.dokkan.common.domain.response.category.CategoryResponse;
import com.hb.dokkan.service.DokkanCategoryDelegate;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description 分类管理 API
 * @Author stargazer
 * @Date 2026/8/16 00:30
 **/
@RestController
@RequestMapping("/categories")
public class DokkanCategoryApi {

    @Resource
    private DokkanCategoryDelegate categoryDelegate;

    /**
     * 分页查询分类列表。
     *
     * @param request 查询请求
     * @return 分类分页响应
     */
    @PostMapping("/list")
    public DokkanResponse<PageResponse<CategoryResponse>> list(@RequestBody CategoryQueryRequest request) {
        return categoryDelegate.list(request);
    }

    /**
     * 新增或编辑分类。
     *
     * @param request 分类保存请求
     * @return 保存后的分类响应
     */
    @PostMapping("/save")
    public DokkanResponse<CategoryResponse> save(@RequestBody CategorySaveRequest request) {
        return categoryDelegate.save(request);
    }
}
