package com.hb.dokkan.service.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Description 扩展属性
 * @Author stargazer
 * @Date 2025/9/11 0:19
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CardBaseInfoAttribute implements Serializable {

    private String leaderSkill;

    private String passiveSkillDesc;

    private Boolean freeCardFlag;

    private Boolean dokkanFesFlag;

    private Boolean carnivalFlag;

}
