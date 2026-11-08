package com.hb.dokkan.common.domain.po.mysql.cards;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hb.dokkan.common.domain.po.mysql.base.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 卡片与链接关系持久化对象。
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("card_link_rel")
public class CardLinkRelPO extends BasePO implements Serializable {
    /**
     * 卡片业务 ID，本体卡和形态卡均使用源站 ID。
     **/
    private Long cardId;
    /**
     * 链接技能业务 ID。
     **/
    private Long linkId;
    /**
     * 卡片链接的展示顺序，从 0 开始。
     **/
    private Integer sort;
}
