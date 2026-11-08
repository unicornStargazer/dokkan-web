package com.hb.dokkan.common.domain.po.es.cards;

import lombok.Data;

import java.io.Serializable;

/**
 * ES 专用必杀嵌套文档，避免索引模型依赖 MySQL PO。
 **/
@Data
public class CardEsSpecialDTO implements Serializable {
    /**
     * 必杀技能业务 ID。
     **/
    private Long specialId;
    /**
     * 必杀名称和效果合并后的展示文本。
     **/
    private String description;
    /**
     * 技能基础伤害倍率，沿用源站数值单位。
     **/
    private Integer increaseRate;
    /**
     * 必杀等级每提升一级的倍率增量。
     **/
    private Integer lvBonus;
    /**
     * 必杀类型：Normal、Hyper 或 Condition。
     **/
    private String style;
    /**
     * 该必杀解锁所需的必杀等级。
     **/
    private Integer lvStart;
    /**
     * 发动该必杀所需的最低气力值。
     **/
    private Integer eballNumStart;
    /**
     * 必杀攻击类别 ID。
     **/
    private Long specialCategoryId;
    /**
     * 必杀攻击类别名称。
     **/
    private String specialCategoryName;
    /**
     * 第一项必杀额外加成名称。
     **/
    private String specialBonus1;
    /**
     * 第二项必杀额外加成名称。
     **/
    private String specialBonus2;
    /**
     * 第一项必杀加成生效所需的必杀等级。
     **/
    private Integer specialBonus1Lv;
    /**
     * 第二项必杀加成生效所需的必杀等级。
     **/
    private Integer specialBonus2Lv;
    /**
     * 扩展属性 JSON，保留源站额外信息。
     **/
    private String attributes;
}
