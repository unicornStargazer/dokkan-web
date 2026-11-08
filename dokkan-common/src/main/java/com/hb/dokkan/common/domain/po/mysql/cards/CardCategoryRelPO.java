package com.hb.dokkan.common.domain.po.mysql.cards;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hb.dokkan.common.domain.po.mysql.base.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 卡片与分类关系持久化对象。
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("card_category_rel")
public class CardCategoryRelPO extends BasePO implements Serializable {
    /**
     * 卡片业务 ID，本体卡和形态卡均使用源站 ID。
     **/
    private Long cardId;
    /**
     * 分类业务 ID。
     **/
    private Long categoryId;
}
