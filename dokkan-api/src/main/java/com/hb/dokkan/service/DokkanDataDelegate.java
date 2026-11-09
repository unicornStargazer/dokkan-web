package com.hb.dokkan.service;

import com.hb.dokkan.common.constants.ExceptionErrorCode;
import com.hb.dokkan.common.domain.request.cards.CardIdSyncRequest;
import com.hb.dokkan.common.domain.request.data.SyncStartRequest;
import com.hb.dokkan.common.domain.response.base.DokkanResponse;
import com.hb.dokkan.common.domain.response.data.SyncProgressResponse;
import com.hb.dokkan.common.exception.domain.DokkanBizException;
import com.hb.dokkan.service.job.sync.SyncDataService;
import com.hb.dokkan.service.job.sync.SyncProgressService;
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

    /**
     * KX Wiki 与 MySQL、ES 的同步编排服务。
     **/
    @Resource
    private SyncDataService syncDataService;

    /**
     * 同步任务进度管理服务。
     **/
    @Resource
    private SyncProgressService syncProgressService;


    /**
     * 初始化卡片数据
     **/
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
     **/
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

    /**
     * 校验指定卡片同步入参并返回同步数量。
     **/
    public DokkanResponse<Integer> syncCardByIds(CardIdSyncRequest request) {
        if (!hasRequestedCardIds(request)) {
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

    /**
     * 启动异步同步任务并返回任务 ID。
     **/
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

    /**
     * 查询同步任务进度，任务不存在时返回参数错误。
     **/
    public DokkanResponse<SyncProgressResponse> syncProgress(String jobId) {
        SyncProgressResponse progress = syncProgressService.get(jobId);
        if (progress == null) {
            return DokkanResponse.<SyncProgressResponse>builder().fail(
                    ExceptionErrorCode.QUERY_PARAM_ERROR.getErrorCode(), "同步任务不存在或已过期");
        }
        return DokkanResponse.<SyncProgressResponse>builder().withModel(progress).success();
    }


    /**
     * 依次判空请求及卡片集合，避免空请求触发字段访问。
     **/
    private boolean hasRequestedCardIds(CardIdSyncRequest request) {
        if (request == null) {
            return false;
        } else if (request.getCardIds() == null) {
            return false;
        } else {
            return !request.getCardIds().isEmpty();
        }
    }

    /**
     * 初始化分类数据
     **/
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
     **/
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

}
