package com.hb.dokkan.service;

import com.hb.dokkan.common.constants.ExceptionErrorCode;
import com.hb.dokkan.common.constants.TranslationConstants;
import com.hb.dokkan.common.domain.request.cards.CardIdSyncRequest;
import com.hb.dokkan.common.domain.request.data.SyncStartRequest;
import com.hb.dokkan.common.domain.request.data.TranslationRequest;
import com.hb.dokkan.common.domain.response.base.DokkanResponse;
import com.hb.dokkan.common.domain.response.data.SyncProgressResponse;
import com.hb.dokkan.common.exception.domain.DokkanBizException;
import com.hb.dokkan.service.job.sync.SyncDataService;
import com.hb.dokkan.service.job.sync.SyncProgressService;
import com.hb.dokkan.service.translation.DokkanLlmTranslationService;
import com.hb.dokkan.service.translation.DokkanTranslationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import com.hb.dokkan.common.utils.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @Description 数据服务实现类
 * @Author stargazer
 * @Date 2025/12/12 23:34
 **/
@Service
@Slf4j
public class DokkanDataDelegate {

    @Resource
    private SyncDataService syncDataService;

    @Resource
    private SyncProgressService syncProgressService;

    @Resource
    private DokkanTranslationService translationService;

    @Resource
    private DokkanLlmTranslationService llmTranslationService;

    /**
     * 初始化卡片数据
     */
    public DokkanResponse initCard() {
        try {
            syncDataService.initCard();
            return DokkanResponse.builder().success();
        }catch (DokkanBizException e){
            log.error("biz exception,error:{},e",e.getMessage(),e.getCause());
            return DokkanResponse.builder().fail(e.getError().getErrorCode(), e.getError().getErrorMsg());
        } catch (Exception e) {
            log.error("DokkanCardService#initCard error,",e);
            return DokkanResponse.builder().fail();
        }
    }

    /**
     * 同步es卡片数据
     */
    public DokkanResponse syncEsCardData() {
        try {
            syncDataService.syncEsCardData();
            return DokkanResponse.builder().success();
        } catch (DokkanBizException e){
            log.error("biz exception,error:{},e",e.getMessage(),e.getCause());
            return DokkanResponse.builder().fail(e.getError().getErrorCode(), e.getError().getErrorMsg());
        } catch (Exception e) {
            log.error("DokkanCardService#syncEsCardData error,",e);
            return DokkanResponse.builder().fail();
        }
    }

    public DokkanResponse<Integer> syncCardByIds(CardIdSyncRequest request) {
        if (request == null || request.getCardIds() == null || request.getCardIds().isEmpty()) {
            return DokkanResponse.<Integer>builder().fail(
                    ExceptionErrorCode.QUERY_PARAM_ERROR.getErrorCode(),
                    ExceptionErrorCode.QUERY_PARAM_ERROR.getErrorMsg());
        }
        try {
            int syncCount = syncDataService.syncCardsByIds(request.getCardIds());
            return DokkanResponse.<Integer>builder().withModel(syncCount).success();
        } catch (DokkanBizException e) {
            log.error("manual card sync business exception", e);
            if (e.getError() != null) {
                return DokkanResponse.<Integer>builder().fail(e.getError().getErrorCode(), e.getError().getErrorMsg());
            }
            return DokkanResponse.<Integer>builder().fail();
        } catch (Exception e) {
            log.error("DokkanDataDelegate#syncCardByIds error", e);
            return DokkanResponse.<Integer>builder().fail();
        }
    }

    public DokkanResponse<String> startSync(SyncStartRequest request) {
        try {
            return DokkanResponse.<String>builder().withModel(syncProgressService.start(request)).success();
        } catch (DokkanBizException e) {
            log.error("invalid sync start request", e);
            return DokkanResponse.<String>builder().fail(
                    e.getError() == null ? ExceptionErrorCode.QUERY_PARAM_ERROR.getErrorCode() : e.getError().getErrorCode(),
                    e.getError() == null ? e.getMessage() : e.getError().getErrorMsg());
        } catch (Exception e) {
            log.error("DokkanDataDelegate#startSync error", e);
            return DokkanResponse.<String>builder().fail();
        }
    }

    public DokkanResponse<SyncProgressResponse> syncProgress(String jobId) {
        SyncProgressResponse progress = syncProgressService.get(jobId);
        if (progress == null) {
            return DokkanResponse.<SyncProgressResponse>builder().fail(
                    ExceptionErrorCode.QUERY_PARAM_ERROR.getErrorCode(), "同步任务不存在或已过期");
        }
        return DokkanResponse.<SyncProgressResponse>builder().withModel(progress).success();
    }

    /**
     * 使用普通翻译链路翻译文本。
     *
     * @param request 翻译请求
     * @return 翻译响应
     */
    public DokkanResponse<String> translate(TranslationRequest request) {
        // 参数校验失败时直接返回统一错误响应，不进入远程翻译链路。
        if (!validTranslationRequest(request)) {
            return translationParamError();
        }
        try {
            // 调用普通翻译服务，内部负责缓存、术语保护和远程失败兜底。
            String translated = translationService.translate(request.getText());
            return DokkanResponse.<String>builder().withModel(translated).success();
        } catch (Exception e) {
            log.error("DokkanDataDelegate#translate error", e);
            return DokkanResponse.<String>builder().fail();
        }
    }

    /**
     * 使用 LLM 翻译链路翻译文本。
     *
     * @param request 翻译请求
     * @return 翻译响应
     */
    public DokkanResponse<String> translateByLlm(TranslationRequest request) {
        // 参数校验失败时直接返回统一错误响应，避免无效文本请求外部 LLM。
        if (!validTranslationRequest(request)) {
            return translationParamError();
        }
        try {
            // 调用 LLM 翻译服务，配置校验和 HTTP 细节由服务与 Client 分层处理。
            String translated = llmTranslationService.translate(request.getText());
            return DokkanResponse.<String>builder()
                    .withModel(translated)
                    .success();
        } catch (IllegalStateException e) {
            log.warn("DokkanDataDelegate#translateByLlm failed, error={}", e.getMessage());
            return DokkanResponse.<String>builder()
                    .fail(ExceptionErrorCode.QUERY_PARAM_ERROR.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            log.error("DokkanDataDelegate#translateByLlm error", e);
            return DokkanResponse.<String>builder().fail();
        }
    }

    /**
     * 校验翻译请求文本是否非空且不超过最大长度。
     *
     * @param request 翻译请求
     * @return 校验是否通过
     */
    private boolean validTranslationRequest(TranslationRequest request) {
        return request != null
                && StringUtils.isNotBlank(request.getText())
                && request.getText().length() <= TranslationConstants.MAX_TRANSLATION_TEXT_LENGTH;
    }

    /**
     * 构建翻译参数错误响应。
     *
     * @return 翻译参数错误响应
     */
    private DokkanResponse<String> translationParamError() {
        return DokkanResponse.<String>builder().fail(
                ExceptionErrorCode.QUERY_PARAM_ERROR.getErrorCode(),
                TranslationConstants.TRANSLATION_TEXT_ERROR_MESSAGE);
    }

    /**
     * 初始化分类数据
     */
    public DokkanResponse initCategories() {
        try {
            syncDataService.initCategories();
        }catch (DokkanBizException e){
            log.error("biz exception,error:{},e",e.getMessage(),e.getCause());
            return DokkanResponse.builder().fail(e.getError().getErrorCode(), e.getError().getErrorMsg());
        } catch (Exception e) {
            log.error("DokkanDataServiceImpl#initCategories error,",e);
            return DokkanResponse.builder().fail();
        }
        return DokkanResponse.builder().success();
    }

    /**
     * 初始化链接数据
     */
    public DokkanResponse initLinks() {
        try {
            syncDataService.initLinks();
        }catch (DokkanBizException e){
            log.error("biz exception,error:{},e",e.getMessage(),e.getCause());
            return DokkanResponse.builder().fail(e.getError().getErrorCode(), e.getError().getErrorMsg());
        } catch (Exception e) {
            log.error("DokkanDataServiceImpl#initLinks error,",e);
            return DokkanResponse.builder().fail();
        }
        return DokkanResponse.builder().success();
    }

    /**
     * 修复数据库数据
     */
    public DokkanResponse fixDbData(Integer fixType) {
        try {
            syncDataService.fixDbData(fixType);
        }catch (DokkanBizException e){
            log.error("biz exception,error:{},e",e.getMessage(),e.getCause());
            return DokkanResponse.builder().fail(e.getError().getErrorCode(), e.getError().getErrorMsg());
        } catch (Exception e) {
            log.error("DokkanDataServiceImpl#fixDbData error,",e);
            return DokkanResponse.builder().fail();
        }
        return DokkanResponse.builder().success();
    }
}
