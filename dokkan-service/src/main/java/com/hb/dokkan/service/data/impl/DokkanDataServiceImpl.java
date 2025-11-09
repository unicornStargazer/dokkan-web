package com.hb.dokkan.service.data.impl;

import com.hb.dokkan.common.domain.DokkanResponse;
import com.hb.dokkan.common.exception.domain.DokkanBizException;
import com.hb.dokkan.service.data.DokkanDataService;
import com.hb.dokkan.service.job.sync.SyncDataService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Description 数据服务
 * @Author stargazer
 * @Date 2025/6/2 21:41
 **/
@Service
@Slf4j
public class DokkanDataServiceImpl implements DokkanDataService {

    @Resource
    private SyncDataService syncDataService;

    /**
     * 初始化卡片数据
     */
    @Override
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
    @Override
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
     * 初始化分类数据
     */
    @Override
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


}
