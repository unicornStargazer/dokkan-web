package com.hb.dokkan.common.domain.po.mysql.cards;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hb.dokkan.common.domain.po.mysql.base.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 卡片技能持久化对象。
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("card_skill")
public class CardSkillPO extends BasePO implements Serializable {
    /**
     * 卡片业务 ID，本体卡和形态卡均使用源站 ID。
     **/
    private Long cardId;
    /**
     * 卡片状态键：base、eza 或 seza。
     **/
    private String stateKey;
    /**
     * 技能类型：super、active、standby 或 finish。
     **/
    private String skillType;
    /**
     * 源技能业务 ID，必杀技对应源站 skill_id。
     **/
    private Long skillId;
    /**
     * 显示名称。
     **/
    private String name;
    /**
     * 必杀类型：Normal、Hyper 或 Condition。
     **/
    private String style;
    /**
     * 技能发动所需的气力值。
     **/
    private Integer ki;
    /**
     * 技能发动条件，优先保留源站模板文本。
     **/
    private String conditionDesc;
    /**
     * 技能效果描述，优先保留源站模板文本。
     **/
    private String effectDesc;
    /**
     * 技能基础伤害倍率，沿用源站数值单位。
     **/
    private Integer increaseRate;
    /**
     * 必杀等级每提升一级的倍率增量。
     **/
    private Integer lvBonus;
    /**
     * 攻击类别 ID。
     **/
    private Integer attackCategoryId;
    /**
     * 攻击类别名称。
     **/
    private String attackCategoryName;
    /**
     * 同类记录的展示顺序，从 0 开始。
     **/
    private Integer sort;
    /**
     * 技能扩展属性 JSON，包含 bonuses、次数限制及回合等。
     **/
    private String attributes;
}
