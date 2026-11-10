package com.hb.dokkan.service.job.sync.strategy.context;

import com.hb.dokkan.common.domain.bo.data.KxWikiCardBO;
import com.hb.dokkan.common.domain.bo.data.KxWikiCardChangesBO;
import com.hb.dokkan.common.domain.dto.data.kx.KxWikiReferenceItemDTO;
import lombok.Data;
import java.util.*;

/**
 * KX Wiki 同步上下文，记录失败卡片和待确认的源水位。
 **/
@Data
public class WikiContext {
    /**
     * 是否重新抓取同步范围内的所有卡片，默认按版本和候选变化增量同步。
     **/
    private boolean force;
    /**
     * 本轮成功抓取的本体及形态卡聚合，等待事务写入。
     **/
    private List<KxWikiCardBO> cardData = new ArrayList<>();
    /**
     * 本轮抓取或写入失败的本体卡片 ID，用于重试与错误报告。
     **/
    private List<Long> failedCardIds = new ArrayList<>();
    /**
     * 源站当前语言的完整分类列表。
     **/
    private List<KxWikiReferenceItemDTO> categoryData = new ArrayList<>();
    /**
     * 源站英文分类列表，用于补充分类英文名称。
     **/
    private List<KxWikiReferenceItemDTO> englishCategoryData = new ArrayList<>();
    /**
     * 源站链接参考列表，仅包含名称及一级描述。
     **/
    private List<KxWikiReferenceItemDTO> linkData = new ArrayList<>();
    /**
     * 本轮源站版本水位，仅在 MySQL 和 ES 全部成功后确认。
     **/
    private String sourceUpdatedAt;
    /**
     * 最新批次和时间线的候选与待确认水位，整轮成功前不持久化。
     **/
    private KxWikiCardChangesBO cardChanges;
}
