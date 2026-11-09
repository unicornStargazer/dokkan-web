package com.hb.dokkan.service;

import com.hb.dokkan.common.constants.ExceptionErrorCode;
import com.hb.dokkan.common.domain.request.challenges.ChallengeEventRequest;
import com.hb.dokkan.common.domain.request.challenges.ChallengeListRequest;
import com.hb.dokkan.common.domain.request.challenges.ChallengeMapRequest;
import com.hb.dokkan.common.domain.response.base.DokkanResponse;
import com.hb.dokkan.common.domain.response.base.PageResponse;
import com.hb.dokkan.common.exception.domain.DokkanBizException;
import com.hb.dokkan.service.challenges.ChallengeQueryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @Description 活动挑战查询接口编排服务
 * @Author Codex
 * @Date 2026/11/9
 **/
@Slf4j
@Service
public class ChallengeDelegate {

    /**
     * 活动挑战查询领域服务。
     **/
    @Resource
    private ChallengeQueryService challengeQueryService;

    /**
     * 查询分页挑战活动列表并封装统一响应。
     *
     * @param request 活动列表请求
     * @return 分页活动结果
     */
    public DokkanResponse<PageResponse<Map<String, Object>>> list(ChallengeListRequest request) {
        try {
            if (request == null) {
                throw new DokkanBizException(ExceptionErrorCode.QUERY_PARAM_ERROR);
            }
            return DokkanResponse.<PageResponse<Map<String, Object>>>builder()
                    .withModel(challengeQueryService.list(request)).success();
        } catch (DokkanBizException exception) {
            log.warn("Challenge list rejected", exception);
            return DokkanResponse.<PageResponse<Map<String, Object>>>builder()
                    .fail(exception.getError().getErrorCode(), exception.getError().getErrorMsg());
        } catch (Exception exception) {
            log.error("Challenge list failed", exception);
            return DokkanResponse.<PageResponse<Map<String, Object>>>builder().fail();
        }
    }

    /**
     * 查询活动关卡和难度地图目录。
     *
     * @param request 活动 ID 请求
     * @return 活动目录响应
     */
    public DokkanResponse<Map<String, Object>> eventDetail(ChallengeEventRequest request) {
        try {
            if (request == null) {
                throw new DokkanBizException(ExceptionErrorCode.QUERY_PARAM_ERROR);
            }
            return DokkanResponse.<Map<String, Object>>builder()
                    .withModel(challengeQueryService.eventDetail(request.getEventId())).success();
        } catch (DokkanBizException exception) {
            log.warn("Challenge event detail rejected", exception);
            return DokkanResponse.<Map<String, Object>>builder()
                    .fail(exception.getError().getErrorCode(), exception.getError().getErrorMsg());
        } catch (Exception exception) {
            log.error("Challenge event detail failed", exception);
            return DokkanResponse.<Map<String, Object>>builder().fail();
        }
    }

    /**
     * 查询难度地图的战斗和敌人详情。
     *
     * @param request 地图 ID 请求
     * @return 地图敌人详情响应
     */
    public DokkanResponse<Map<String, Object>> mapDetail(ChallengeMapRequest request) {
        try {
            if (request == null) {
                throw new DokkanBizException(ExceptionErrorCode.QUERY_PARAM_ERROR);
            }
            return DokkanResponse.<Map<String, Object>>builder()
                    .withModel(challengeQueryService.mapDetail(request.getMapId())).success();
        } catch (DokkanBizException exception) {
            log.warn("Challenge map detail rejected", exception);
            return DokkanResponse.<Map<String, Object>>builder()
                    .fail(exception.getError().getErrorCode(), exception.getError().getErrorMsg());
        } catch (Exception exception) {
            log.error("Challenge map detail failed", exception);
            return DokkanResponse.<Map<String, Object>>builder().fail();
        }
    }
}
