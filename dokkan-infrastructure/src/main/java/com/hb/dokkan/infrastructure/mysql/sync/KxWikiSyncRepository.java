package com.hb.dokkan.infrastructure.mysql.sync;

import com.hb.dokkan.infrastructure.mysql.sync.mapper.KxWikiSyncMapper;
import org.springframework.stereotype.Repository;

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
}
