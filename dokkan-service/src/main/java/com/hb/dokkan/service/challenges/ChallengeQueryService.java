package com.hb.dokkan.service.challenges;

import com.hb.dokkan.common.constants.ExceptionErrorCode;
import com.hb.dokkan.common.constants.CardSyncConstants;
import com.hb.dokkan.common.domain.request.challenges.ChallengeListRequest;
import com.hb.dokkan.common.domain.response.base.PageResponse;
import com.hb.dokkan.common.exception.domain.DokkanBizException;
import com.hb.dokkan.config.kx.KxWikiProperties;
import com.hb.dokkan.infrastructure.mysql.challenges.ChallengeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @Description 活动挑战目录及敌人数据查询服务
 * @Author Codex
 * @Date 2026/11/9
 **/
@Service
public class ChallengeQueryService {

    /**
     * 活动挑战 MySQL 仓储。
     **/
    private final ChallengeRepository challengeRepository;

    /**
     * 活动挑战源地区和语言。
     **/
    private final KxWikiProperties properties;

    /**
     * 注入挑战查询服务依赖。
     *
     * @param challengeRepository 活动挑战仓储
     * @param properties KX Wiki 配置
     */
    public ChallengeQueryService(ChallengeRepository challengeRepository, KxWikiProperties properties) {
        this.challengeRepository = challengeRepository;
        this.properties = properties;
    }

    /**
     * 按活动名和数据状态分页查询活动挑战目录。
     *
     * @param request 分页筛选参数
     * @return 活动列表分页结果
     */
    public PageResponse<Map<String, Object>> list(ChallengeListRequest request) {
        validateDataStatus(request.getDataStatus());
        int pageNum = request.getPageNum() == null ? 1 : request.getPageNum();
        int pageSize = request.getPageSize() == null ? 15 : Math.min(request.getPageSize(), 100);
        if (pageNum < 1 || pageSize < 1) {
            throw new DokkanBizException(ExceptionErrorCode.QUERY_PARAM_ERROR);
        }
        String keyword = request.getEventName() == null ? "" : request.getEventName().trim();
        String dataStatus = request.getDataStatus();
        long total = challengeRepository.countEvents(properties.getRegion(), properties.getLang(), keyword, dataStatus);
        List<Map<String, Object>> events = challengeRepository.listEvents(properties.getRegion(),
                properties.getLang(), keyword, dataStatus, (pageNum - 1) * pageSize, pageSize);
        return PageResponse.<Map<String, Object>>builder()
                .currentPage(pageNum)
                .pageSize(pageSize)
                .total(total)
                .data(events)
                .build();
    }

    /**
     * 查询单个活动的所有关卡和难度地图目录。
     *
     * @param eventId 源活动 ID
     * @return 活动及其关卡地图目录
     */
    public Map<String, Object> eventDetail(Long eventId) {
        if (eventId == null || eventId <= 0) {
            throw new DokkanBizException(ExceptionErrorCode.QUERY_PARAM_ERROR);
        }
        return challengeRepository.getEventDetail(properties.getRegion(), properties.getLang(), eventId);
    }

    /**
     * 查询单张难度地图的完整敌人战斗树。
     *
     * @param mapId 源地图 ID
     * @return 地图战斗和敌人详情
     */
    public Map<String, Object> mapDetail(Long mapId) {
        if (mapId == null || mapId <= 0) {
            throw new DokkanBizException(ExceptionErrorCode.QUERY_PARAM_ERROR);
        }
        return challengeRepository.getMapDetail(properties.getRegion(), properties.getLang(), mapId);
    }

    /**
     * 只接受明确支持的本地快照状态。
     */
    private void validateDataStatus(String dataStatus) {
        if (dataStatus == null || dataStatus.isBlank()) {
            return;
        } else if (CardSyncConstants.CHALLENGE_DATA_STATUS_NONE.equals(dataStatus)
                || CardSyncConstants.CHALLENGE_DATA_STATUS_CAPTURED.equals(dataStatus)) {
            return;
        } else if (CardSyncConstants.CHALLENGE_DATA_STATUS_PARTIAL.equals(dataStatus)) {
            return;
        } else {
            throw new DokkanBizException(ExceptionErrorCode.QUERY_PARAM_ERROR);
        }
    }
}
