package com.hb.dokkan.common.domain.po.mysql.sync;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hb.dokkan.common.domain.po.mysql.base.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Description KX Wiki整个数据源最近完整同步成功的版本
 * @Author Codex
 * @Date 2026/11/8
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kx_wiki_sync_checkpoint")
public class KxWikiSyncCheckpointPO extends BasePO {
    /**
     * 包含地区和语言的数据源范围，同一范围只保留一个成功版本。
     **/
    private String syncKey;

    /**
     * 整轮同步成功后确认的源站updated_at原文，用于判断整个数据源是否更新。
     **/
    private String sourceUpdatedAt;
}
