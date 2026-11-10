package com.hb.dokkan.common.domain.bo.data;

import lombok.Data;
import com.hb.dokkan.common.domain.dto.data.kx.KxWikiCardChangeDTO;
import java.util.LinkedHashMap;
import java.util.Map;
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
     * 两个更新入口合并去重后的更新 ID，仍受稀有度和黑名单规则约束。
     **/
    private Set<Long> cardIds = new LinkedHashSet<>();
    /**
     * 当前 DDL 批次完整的卡片信号；未发布卡无需先出现在目录，已确认批次仍用来补录缺卡。
     **/
    private Map<Long, KxWikiCardChangeDTO> ddlCards = new LinkedHashMap<>();
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
