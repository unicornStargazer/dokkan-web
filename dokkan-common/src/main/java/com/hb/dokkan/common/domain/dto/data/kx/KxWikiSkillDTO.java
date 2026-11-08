package com.hb.dokkan.common.domain.dto.data.kx;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import java.util.*;
import java.io.Serializable;

/**
 * @Description KX Wiki KxWikiSkillDTO 响应模型
 * @Author Codex
 * @Date 2026/11/8
 **/
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KxWikiSkillDTO implements Serializable {
    /**
     * 源站技能记录 id，用于区分各档气力必杀并匹配 enrichment。
     **/
    private Long id;
    /**
     * 源站 skill_id 效果编号，可被不同必杀记录共用，不能用作记录唯一标识。
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
     * 源站必杀等级或技能解锁等级。
     **/
    private Integer superAttackLevel;
    /**
     * 展示描述文本。
     **/
    private String description;
    /**
     * 保留源站占位符的描述模板，展示时优先使用。
     **/
    private String descriptionTemplate;
    /**
     * 当前技能关联的领域，保留展开与依赖关系。
     **/
    private List<KxWikiDomainDTO> domains;
    /**
     * 技能发动条件的普通文本。
     **/
    private String condition;
    /**
     * 保留源站占位符的技能发动条件模板。
     **/
    private String conditionTemplate;
    /**
     * 技能允许发动的次数上限。
     **/
    private Integer execLimit;
    /**
     * 技能效果持续回合数。
     **/
    private Integer turn;
    /**
     * 必杀额外加成及其生效等级。
     **/
    private List<Map<String,Object>> bonuses;
    /**
     * 预备技对应的终结技能列表。
     **/
    private List<KxWikiSkillDTO> finishSkills;
    /**
     * 伤害倍率及必杀等级增量信息。
     **/
    private KxWikiDamageDTO damage;
    /**
     * 攻击类别列表，按源站顺序保留。
     **/
    private List<KxWikiAttackTypeDTO> attackTypes;
}
