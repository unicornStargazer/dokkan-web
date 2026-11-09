package com.hb.dokkan.api.challenges;

import com.hb.dokkan.common.domain.request.challenges.ChallengeEventRequest;
import com.hb.dokkan.common.domain.request.challenges.ChallengeListRequest;
import com.hb.dokkan.common.domain.request.challenges.ChallengeMapRequest;
import com.hb.dokkan.common.domain.response.base.DokkanResponse;
import com.hb.dokkan.common.domain.response.base.PageResponse;
import com.hb.dokkan.service.ChallengeDelegate;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @Description 活动挑战管理端查询 API
 * @Author Codex
 * @Date 2026/11/9
 **/
@RestController
@RequestMapping("/challenges")
public class ChallengeApi {

    /**
     * 活动挑战接口参数校验及响应编排服务。
     **/
    @Resource
    private ChallengeDelegate challengeDelegate;

    /**
     * 分页搜索活动挑战目录。
     *
     * @param request 分页和筛选参数
     * @return 活动挑战分页结果
     */
    @PostMapping("/list")
    public DokkanResponse<PageResponse<Map<String, Object>>> list(@RequestBody ChallengeListRequest request) {
        return challengeDelegate.list(request);
    }

    /**
     * 查询活动及其逻辑关卡和难度地图。
     *
     * @param request 源活动 ID
     * @return 活动挑战目录详情
     */
    @PostMapping("/detail")
    public DokkanResponse<Map<String, Object>> detail(@RequestBody ChallengeEventRequest request) {
        return challengeDelegate.eventDetail(request);
    }

    /**
     * 查询一个难度地图下的完整战斗阶段和敌人出场。
     *
     * @param request 源地图 ID
     * @return 地图 Boss 详情
     */
    @PostMapping("/map/detail")
    public DokkanResponse<Map<String, Object>> mapDetail(@RequestBody ChallengeMapRequest request) {
        return challengeDelegate.mapDetail(request);
    }
}
