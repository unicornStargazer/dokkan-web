package com.hb.dokkan.common.domain.po.es.cards;

import lombok.Data;

import java.io.Serializable;

/**
 * ES 专用技能嵌套文档，避免索引模型依赖 MySQL PO。
 **/
@Data
public class CardEsSkillDTO implements Serializable {
    /**
     * 兼容旧 ES 的字符串技能业务 ID。
     **/
    private String skillId;
    /**
     * 显示名称。
     **/
    private String name;
    /**
     * 技能发动条件的展示文本。
     **/
    private String conditionDescription;
    /**
     * 技能效果的展示文本。
     **/
    private String effectDescription;
    /**
     * 技能类型标记：standby 或 finish。
     **/
    private String label;
    /**
     * 技能基础伤害倍率，沿用源站数值单位。
     **/
    private Integer increaseRate;
    /**
     * 必杀攻击类别 ID。
     **/
    private Integer specialCategoryId;
    /**
     * 必杀攻击类别名称。
     **/
    private String specialCategoryName;
    /**
     * 扩展属性 JSON，保留源站额外信息。
     **/
    private String attributes;
}
