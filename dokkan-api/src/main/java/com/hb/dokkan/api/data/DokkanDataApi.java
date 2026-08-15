package com.hb.dokkan.api.data;

import com.hb.dokkan.common.domain.request.cards.CardIdSyncRequest;
import com.hb.dokkan.common.domain.request.data.SyncStartRequest;
import com.hb.dokkan.common.domain.request.data.TranslationRequest;
import com.hb.dokkan.common.domain.response.base.DokkanResponse;
import com.hb.dokkan.common.domain.response.data.SyncProgressResponse;
import com.hb.dokkan.service.DokkanDataDelegate;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
     * 同步 wiki 卡牌数据。
     *
     * @return 同步响应
     */
    @PostMapping("/init-card")
    public DokkanResponse initCard() {
        return dataDelegate.initCard();
    }

    /**
     * 同步 ES 卡牌数据。
     *
     * @return 同步响应
     */
    @PostMapping("/sync-es-card")
    public DokkanResponse syncEsCardData() {
        return dataDelegate.syncEsCardData();
    }

    /**
     * 手动同步指定卡片 ID 到 MySQL、ES 和对象存储。
     *
     * @param request 卡片 ID 同步请求
     * @return 同步数量响应
     */
    @PostMapping("/sync-card-by-ids")
    public DokkanResponse<Integer> syncCardByIds(@RequestBody CardIdSyncRequest request) {
        return dataDelegate.syncCardByIds(request);
    }

    /**
     * 启动后台同步任务。
     *
     * @param request 同步启动请求
     * @return 后台任务 ID 响应
     */
    @PostMapping("/sync-start")
    public DokkanResponse<String> startSync(@RequestBody SyncStartRequest request) {
        return dataDelegate.startSync(request);
    }

    /**
     * 查询后台同步任务进度。
     *
     * @param jobId 后台任务 ID
     * @return 同步进度响应
     */
    @GetMapping("/sync-progress/{jobId}")
    public DokkanResponse<SyncProgressResponse> syncProgress(@PathVariable String jobId) {
        return dataDelegate.syncProgress(jobId);
    }

    /**
     * 使用 DokkanDB 同步链路的普通翻译能力翻译单条文本。
     *
     * @param request 翻译请求
     * @return 翻译结果响应
     */
    @PostMapping("/translate")
    public DokkanResponse<String> translate(@RequestBody TranslationRequest request) {
        return dataDelegate.translate(request);
    }

    /**
     * 使用已配置的 OpenAI 兼容 LLM 翻译单条文本。
     *
     * @param request 翻译请求
     * @return 翻译结果响应
     */
    @PostMapping("/translate/llm")
    public DokkanResponse<String> translateByLlm(@RequestBody TranslationRequest request) {
        return dataDelegate.translateByLlm(request);
    }

    /**
     * 初始化分类数据。
     *
     * @return 初始化响应
     */
    @PostMapping("/init-category")
    public DokkanResponse initCategories() {
        return dataDelegate.initCategories();
    }

    /**
     * 初始化链接数据。
     *
     * @return 初始化响应
     */
    @PostMapping("/init-link")
    public DokkanResponse initLinks() {
        return dataDelegate.initLinks();
    }

    /**
     * 修复数据库数据。
     *
     * @param fixType 修复类型
     * @return 修复响应
     */
    @GetMapping("/fix-db-data/{fixType}")
    public DokkanResponse fixDbData(@PathVariable("fixType") Integer fixType) {
        return dataDelegate.fixDbData(fixType);
    }
}
