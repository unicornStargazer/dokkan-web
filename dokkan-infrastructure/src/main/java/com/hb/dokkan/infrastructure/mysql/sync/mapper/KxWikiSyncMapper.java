package com.hb.dokkan.infrastructure.mysql.sync.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hb.dokkan.common.domain.po.mysql.sync.KxWikiSyncCheckpointPO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @Description KX Wiki整个数据源成功版本的持久化操作
 * @Author Codex
 * @Date 2026/11/8
 **/
@Mapper
public interface KxWikiSyncMapper extends BaseMapper<KxWikiSyncCheckpointPO> {
    /**
     * 读取指定同步范围的成功版本；首次同步返回null。
     **/
    @Select("SELECT source_updated_at FROM kx_wiki_sync_checkpoint WHERE sync_key = #{syncKey}")
    String selectLastUpdatedAt(@Param("syncKey") String syncKey);

    /**
     * 保存整轮成功版本；调用方负责确认本轮没有失败卡片。
     **/
    @Insert("""
            INSERT INTO kx_wiki_sync_checkpoint (id, sync_key, source_updated_at)
            VALUES (UUID(), #{syncKey}, #{updatedAt})
            ON DUPLICATE KEY UPDATE source_updated_at = #{updatedAt}, update_time = CURRENT_TIMESTAMP
            """)
    int confirmVersion(@Param("syncKey") String syncKey, @Param("updatedAt") String updatedAt);
}
