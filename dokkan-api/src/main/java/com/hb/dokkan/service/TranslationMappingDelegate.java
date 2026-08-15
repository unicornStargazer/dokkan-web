package com.hb.dokkan.service;

import com.hb.dokkan.common.constants.ExceptionErrorCode;
import com.hb.dokkan.common.domain.request.translation.TranslationMappingDeleteRequest;
import com.hb.dokkan.common.domain.request.translation.TranslationMappingQueryRequest;
import com.hb.dokkan.common.domain.request.translation.TranslationMappingSaveRequest;
import com.hb.dokkan.common.domain.request.translation.TranslationMappingStatusRequest;
import com.hb.dokkan.common.domain.response.base.DokkanResponse;
import com.hb.dokkan.common.domain.response.base.PageResponse;
import com.hb.dokkan.common.domain.response.translation.TranslationMappingResponse;
import com.hb.dokkan.common.exception.domain.DokkanBizException;
import com.hb.dokkan.service.translation.TranslationMappingService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Description 翻译映射管理委托服务
 * @Author stargazer
 * @Date 2026/8/15 23:30
 **/
@Slf4j
@Service
public class TranslationMappingDelegate {

    @Resource
    private TranslationMappingService translationMappingService;

    /**
     * 分页查询翻译映射。
     *
     * @param request 查询请求
     * @return 翻译映射分页响应
     */
    public DokkanResponse<PageResponse<TranslationMappingResponse>> list(TranslationMappingQueryRequest request) {
        try {
            PageResponse<TranslationMappingResponse> response = translationMappingService.queryPage(request);
            return DokkanResponse.<PageResponse<TranslationMappingResponse>>builder()
                    .withModel(response)
                    .success();
        } catch (Exception e) {
            log.error("TranslationMappingDelegate#list error", e);
            return DokkanResponse.<PageResponse<TranslationMappingResponse>>builder().fail();
        }
    }

    /**
     * 新增或编辑翻译映射。
     *
     * @param request 保存请求
     * @return 保存后的翻译映射响应
     */
    public DokkanResponse<TranslationMappingResponse> save(TranslationMappingSaveRequest request) {
        try {
            TranslationMappingResponse response = translationMappingService.save(request);
            return DokkanResponse.<TranslationMappingResponse>builder()
                    .withModel(response)
                    .success();
        } catch (DokkanBizException e) {
            log.warn("TranslationMappingDelegate#save invalid request", e);
            return DokkanResponse.<TranslationMappingResponse>builder()
                    .fail(ExceptionErrorCode.QUERY_PARAM_ERROR.getErrorCode(), ExceptionErrorCode.QUERY_PARAM_ERROR.getErrorMsg());
        } catch (Exception e) {
            log.error("TranslationMappingDelegate#save error", e);
            return DokkanResponse.<TranslationMappingResponse>builder().fail();
        }
    }

    /**
     * 删除翻译映射。
     *
     * @param request 删除请求
     * @return 删除响应
     */
    public DokkanResponse delete(TranslationMappingDeleteRequest request) {
        try {
            translationMappingService.delete(request);
            return DokkanResponse.builder().success();
        } catch (DokkanBizException e) {
            log.warn("TranslationMappingDelegate#delete invalid request", e);
            return DokkanResponse.builder()
                    .fail(ExceptionErrorCode.QUERY_PARAM_ERROR.getErrorCode(), ExceptionErrorCode.QUERY_PARAM_ERROR.getErrorMsg());
        } catch (Exception e) {
            log.error("TranslationMappingDelegate#delete error", e);
            return DokkanResponse.builder().fail();
        }
    }

    /**
     * 更新翻译映射启用状态。
     *
     * @param request 状态变更请求
     * @return 更新响应
     */
    public DokkanResponse status(TranslationMappingStatusRequest request) {
        try {
            translationMappingService.updateStatus(request);
            return DokkanResponse.builder().success();
        } catch (DokkanBizException e) {
            log.warn("TranslationMappingDelegate#status invalid request", e);
            return DokkanResponse.builder()
                    .fail(ExceptionErrorCode.QUERY_PARAM_ERROR.getErrorCode(), ExceptionErrorCode.QUERY_PARAM_ERROR.getErrorMsg());
        } catch (Exception e) {
            log.error("TranslationMappingDelegate#status error", e);
            return DokkanResponse.builder().fail();
        }
    }

    /**
     * 刷新翻译映射缓存。
     *
     * @return 刷新响应
     */
    public DokkanResponse refresh() {
        try {
            translationMappingService.refreshMappingCache();
            return DokkanResponse.builder().success();
        } catch (Exception e) {
            log.error("TranslationMappingDelegate#refresh error", e);
            return DokkanResponse.builder().fail();
        }
    }
}
