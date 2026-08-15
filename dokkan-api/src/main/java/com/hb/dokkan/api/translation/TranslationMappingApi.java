package com.hb.dokkan.api.translation;

import com.hb.dokkan.common.domain.request.translation.TranslationMappingDeleteRequest;
import com.hb.dokkan.common.domain.request.translation.TranslationMappingQueryRequest;
import com.hb.dokkan.common.domain.request.translation.TranslationMappingSaveRequest;
import com.hb.dokkan.common.domain.request.translation.TranslationMappingStatusRequest;
import com.hb.dokkan.common.domain.response.base.DokkanResponse;
import com.hb.dokkan.common.domain.response.base.PageResponse;
import com.hb.dokkan.common.domain.response.translation.TranslationMappingResponse;
import com.hb.dokkan.service.TranslationMappingDelegate;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description 翻译映射管理 API
 * @Author stargazer
 * @Date 2026/8/15 23:30
 **/
@RestController
@RequestMapping("/translation-mapping")
public class TranslationMappingApi {

    @Resource
    private TranslationMappingDelegate translationMappingDelegate;

    /**
     * 分页查询翻译映射。
     *
     * @param request 查询请求
     * @return 翻译映射分页响应
     */
    @PostMapping("/list")
    public DokkanResponse<PageResponse<TranslationMappingResponse>> list(
            @RequestBody TranslationMappingQueryRequest request) {
        return translationMappingDelegate.list(request);
    }

    /**
     * 新增或编辑翻译映射。
     *
     * @param request 保存请求
     * @return 保存后的翻译映射响应
     */
    @PostMapping("/save")
    public DokkanResponse<TranslationMappingResponse> save(@RequestBody TranslationMappingSaveRequest request) {
        return translationMappingDelegate.save(request);
    }

    /**
     * 删除翻译映射。
     *
     * @param request 删除请求
     * @return 删除响应
     */
    @PostMapping("/delete")
    public DokkanResponse delete(@RequestBody TranslationMappingDeleteRequest request) {
        return translationMappingDelegate.delete(request);
    }

    /**
     * 更新翻译映射启用状态。
     *
     * @param request 状态变更请求
     * @return 更新响应
     */
    @PostMapping("/status")
    public DokkanResponse status(@RequestBody TranslationMappingStatusRequest request) {
        return translationMappingDelegate.status(request);
    }

    /**
     * 刷新翻译映射缓存。
     *
     * @return 刷新响应
     */
    @PostMapping("/refresh")
    public DokkanResponse refresh() {
        return translationMappingDelegate.refresh();
    }
}
