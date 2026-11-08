package com.hb.dokkan.common.domain.request.cards;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 批量设置卡片限定标记的请求。
 **/
@Data
public class CardTagBatchRequest implements Serializable {
    /**
     * 序列化版本标识。
     **/
    private static final long serialVersionUID = 1L;

    /**
     * 待更新卡片 ID。
     **/
    private List<Long> cardIds;
    /**
     * 限定标记，null 表示不修改。
     **/
    private Boolean dokkanFesFlag;
    /**
     * 祭限定标记，null 表示不修改。
     **/
    private Boolean carnivalFlag;
    /**
     * 免费卡标记，null 表示不修改。
     **/
    private Boolean freeCardFlag;
}
