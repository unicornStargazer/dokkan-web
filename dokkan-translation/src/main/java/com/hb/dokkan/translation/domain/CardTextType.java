package com.hb.dokkan.translation.domain;

/** 卡片字段的翻译语义；名称与技能效果分别约束，普通入口保留通用类型。 */
public enum CardTextType {
    GENERAL,
    CHARACTER_NAME,
    TITLE,
    SKILL_NAME,
    LEADER_SKILL,
    PASSIVE_SKILL,
    ACTIVE_EFFECT,
    ACTIVE_CONDITION,
    SUPER_ATTACK,
    STANDBY_EFFECT,
    STANDBY_CONDITION,
    FINISH_EFFECT,
    FINISH_CONDITION
}
