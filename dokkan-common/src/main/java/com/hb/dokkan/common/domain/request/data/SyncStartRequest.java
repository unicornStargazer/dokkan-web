package com.hb.dokkan.common.domain.request.data;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description 启动异步同步，MySQL 支持增量或全量重新抓取。
 * @Author Codex
 * @Date 2026/11/8
 **/
@Data
public class SyncStartRequest implements Serializable {
    /**
     * 序列化版本标识。
     **/
    private static final long serialVersionUID = 1L;

    /**
     * 同步类型：mysql、es 或 manual。
     **/
    private String type;
    /**
     * 指定补录的卡片 ID，仅 manual 类型使用。
     **/
    private List<Long> cardIds;
    /**
     * MySQL 同步是否全量重新抓取，缺省为增量；忽略源版本、候选变化及 hash。
     **/
    private Boolean force;
}
