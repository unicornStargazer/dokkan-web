package com.hb.dokkan.api.data;

import com.hb.dokkan.common.domain.request.cards.CardIdSyncRequest;
import com.hb.dokkan.common.domain.request.data.SyncStartRequest;
import com.hb.dokkan.common.domain.response.data.SyncProgressResponse;
import com.hb.dokkan.common.domain.response.base.DokkanResponse;
import com.hb.dokkan.service.DokkanDataDelegate;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * @Description card服务
 * @Author stargazer
 * @Date 2025/6/2 21:38
 **/
@RestController
@RequestMapping("/data")
public class DokkanDataApi {

    @Resource
    private DokkanDataDelegate dataDelegate;

    /**
     * 同步wiki卡牌数据
     * @return
     */
    @PostMapping("/init-card")
    public DokkanResponse initCard(){
        return dataDelegate.initCard();
    }

    /**
     * 同步es卡牌数据
     */
    @PostMapping("/sync-es-card")
    public DokkanResponse syncEsCardData(){
        return dataDelegate.syncEsCardData();
    }

    /**
     * Manually synchronize specified card IDs to MySQL, ES and object storage.
     */
    @PostMapping("/sync-card-by-ids")
    public DokkanResponse<Integer> syncCardByIds(@RequestBody CardIdSyncRequest request) {
        return dataDelegate.syncCardByIds(request);
    }

    /** Starts a background sync job. The returned job id can be polled for progress. */
    @PostMapping("/sync-start")
    public DokkanResponse<String> startSync(@RequestBody SyncStartRequest request) {
        return dataDelegate.startSync(request);
    }

    /** Returns the current progress snapshot of a background sync job. */
    @GetMapping("/sync-progress/{jobId}")
    public DokkanResponse<SyncProgressResponse> syncProgress(@PathVariable String jobId) {
        return dataDelegate.syncProgress(jobId);
    }

    /**
     * 初始化分类数据
     */
    @PostMapping("/init-category")
    public DokkanResponse initCategories(){
        return dataDelegate.initCategories();
    }


    /**
     * 初始化链接数据
     */
    @PostMapping("/init-link")
    public DokkanResponse initLinks(){
        return dataDelegate.initLinks();
    }

    /**
     * 修复数据库数据
     */
    @GetMapping("/fix-db-data/{fixType}")
    public DokkanResponse fixDbData(@PathVariable("fixType") Integer fixType) {
        return dataDelegate.fixDbData(fixType);
    }



}
