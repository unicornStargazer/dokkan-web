package com.hb.dokkan.common.domain.dto.data.kx;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import java.util.*;
import java.io.Serializable;

/**
 * @Description KX Wiki KxWikiStateDTO 响应模型
 * @Author Codex
 * @Date 2026/11/8
 **/
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KxWikiStateDTO implements Serializable {
    /**
     * 卡片状态键：base、eza 或 seza。
     **/
    private String key;
    /**
     * 觉醒阶段序号，base 状态为 0。
     **/
    private Integer step;
    /**
     * 当前状态的角色等级上限。
     **/
    private Integer level;
    /**
     * 源站必杀等级或技能解锁等级。
     **/
    private Integer superAttackLevel;
    /**
     * 当前状态的满级三围数值。
     **/
    private KxWikiStatsDTO stats;
    /**
     * 当前状态队长技信息。
     **/
    private KxWikiTextDTO leader;
    /**
     * 当前状态被动技能信息；为空表示该卡片或形态在此状态没有被动。
     **/
    private KxWikiTextDTO passive;
    /**
     * 当前状态的必杀技能列表。
     **/
    private List<KxWikiSkillDTO> superAttacks;
    /**
     * 当前状态的主动技，为空表示没有主动技。
     **/
    private KxWikiSkillDTO activeSkill;
    /**
     * 当前状态的预备技及其终结技能。
     **/
    private KxWikiSkillDTO standbySkill;
    /**
     * 当前状态的链接技能列表。
     **/
    private List<KxWikiTextDTO> links;
    /**
     * 当前状态的分类列表。
     **/
    private List<KxWikiTextDTO> categories;
    /**
     * 进入该形态的触发条件。
     **/
    private KxWikiTextDTO trigger;
}
