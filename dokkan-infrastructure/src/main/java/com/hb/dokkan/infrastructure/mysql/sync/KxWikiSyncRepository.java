package com.hb.dokkan.infrastructure.mysql.sync;

import com.hb.dokkan.infrastructure.mysql.sync.mapper.KxWikiSyncMapper;
import com.hb.dokkan.common.constants.CardSyncConstants;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Description KX Wiki整个数据源成功版本仓储，服务重启后仍保留更新时间
 * @Author Codex
 * @Date 2026/11/8
 **/
@Repository
public class KxWikiSyncRepository {
    /**
     * 整个数据源成功版本的数据访问接口。
     **/
    private final KxWikiSyncMapper mapper;

    /**
     * 注入同步状态数据访问接口。
     **/
    public KxWikiSyncRepository(KxWikiSyncMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 读取指定地区及语言最近一次完整成功的源版本，未同步时返回null。
     **/
    public String findLastUpdatedAt(String syncKey) {
        return mapper.selectLastUpdatedAt(syncKey);
    }

    /**
     * 保存整轮同步成功版本；保留源文本，不进行有损日期转换。
     **/
    public void confirmVersion(String syncKey, String updatedAt) {
        mapper.confirmVersion(syncKey, updatedAt);
    }

    /**
     * 复用版本表按范围后缀存储两个更新入口水位，与源版本在同一事务确认。
     *
     * @param syncKey 当前地区和语言的卡片范围
     * @param updatedAt 本轮成功源版本
     * @param ddlFingerprint 可选国际服批次指纹
     * @param timelineCheckpoint 可选时间线日期和当天指纹 JSON，长度小于版本字段上限
     **/
    @Transactional(rollbackFor = Exception.class)
    public void confirmCardVersion(String syncKey, String updatedAt, String ddlFingerprint, String timelineCheckpoint) {
        mapper.confirmVersion(syncKey, updatedAt);
        if (ddlFingerprint != null) {
            mapper.confirmVersion(syncKey + CardSyncConstants.KX_WIKI_DDL_CHECKPOINT_SUFFIX, ddlFingerprint);
        }
        if (timelineCheckpoint != null) {
            mapper.confirmVersion(syncKey + CardSyncConstants.KX_WIKI_TIMELINE_CHECKPOINT_SUFFIX, timelineCheckpoint);
        }
    }
}
