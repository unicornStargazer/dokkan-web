package com.hb.dokkan.common.domain.bo.data;

import lombok.Data;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @Description 本轮卡片更新候选及尚未确认的持久化水位
 * @Author Codex
 * @Date 2026/11/10
 **/
@Data
public class KxWikiCardChangesBO {
    /**
     * 两个更新入口合并去重后的卡片 ID，仍受自动同步目录规则约束。
     **/
    private Set<Long> cardIds = new LinkedHashSet<>();
    /**
     * 本轮国际服最新批次指纹；其他地区不使用该入口，为空。
     **/
    private String ddlFingerprint;
    /**
     * 时间线最新日期和当天内容指纹的 JSON，可存入现有版本字段。
     **/
    private String timelineCheckpoint;
    /**
     * 任一更新入口水位是否改变，控制源 status 不变时是否仍扫描目录。
     **/
    private boolean changed;
}
