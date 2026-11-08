package com.hb.dokkan.common.domain.dto.data.kx;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import java.util.*;
import java.io.Serializable;

/**
 * @Description KX Wiki KxWikiEnrichedStateDTO 响应模型
 * @Author Codex
 * @Date 2026/11/8
 **/
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KxWikiEnrichedStateDTO implements Serializable {
    /**
     * 当前状态的必杀技能列表。
     **/
    private List<KxWikiSkillDTO> superAttacks;
    /**
     * 当前状态的主动技，为空表示没有主动技。
     **/
    private KxWikiSkillDTO activeSkill;
    /**
     * 预备技对应的终结技能列表。
     **/
    private List<KxWikiSkillDTO> finishSkills;
}
