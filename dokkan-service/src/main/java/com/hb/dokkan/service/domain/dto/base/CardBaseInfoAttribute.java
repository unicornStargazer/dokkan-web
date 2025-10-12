package com.hb.dokkan.service.domain.dto.base;

import com.google.common.base.Objects;
import com.hb.dokkan.service.domain.wiki.CardTransformationDTO;
import com.hb.dokkan.service.domain.wiki.PotentialDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

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
    /**
     * 队长技id
     */
    private Long leaderSkillSetId;
    /**
     * 队长技
     */
    private String leaderSkill;
    /**
     * 被动id
     */
    private Long passiveSkillSetId;
    /**
     * 被动名称
     */
    private String passiveSkillName;
    /**
     * 被动
     */
    private String passiveSkillDesc;
    /**
     * 主动技id
     */
    private Integer activeSkillId;
    /**
     * 主动技名称
     */
    private String activeSkillName;
    /**
     * 主动技效果
     */
    private String activeSkillEffect;
    /**
     * 主动技条件
     */
    private String activeSkillCondition;
    /**
     * skillLevelMax
     */
    private Integer skillLevelMax;
    /**
     * 免费标志
     */
    private Boolean freeCardFlag;
    /**
     * 限定标志
     */
    private Boolean dokkanFesFlag;
    /**
     * 祭限定
     */
    private Boolean carnivalFlag;

    /**
     * 分类id
     */
    private List<Long> categoryId;

    /**
     * 链接id
     */
    private List<Long> linkId;

    /**
     * 三围潜力值
     */
    private List<PotentialDTO> potential;

    /**
     * 变身后信息
     */
    private List<CardTransformationDTO.NextCardDTO> nextCards;

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof CardBaseInfoAttribute attribute)) return false;
        return Objects.equal(leaderSkillSetId, attribute.leaderSkillSetId) && Objects.equal(leaderSkill, attribute.leaderSkill) && Objects.equal(passiveSkillSetId, attribute.passiveSkillSetId) && Objects.equal(passiveSkillName, attribute.passiveSkillName) && Objects.equal(passiveSkillDesc, attribute.passiveSkillDesc) && Objects.equal(activeSkillId, attribute.activeSkillId) && Objects.equal(activeSkillName, attribute.activeSkillName) && Objects.equal(activeSkillEffect, attribute.activeSkillEffect) && Objects.equal(activeSkillCondition, attribute.activeSkillCondition) && Objects.equal(skillLevelMax, attribute.skillLevelMax) && Objects.equal(freeCardFlag, attribute.freeCardFlag) && Objects.equal(dokkanFesFlag, attribute.dokkanFesFlag) && Objects.equal(carnivalFlag, attribute.carnivalFlag) && Objects.equal(categoryId, attribute.categoryId) && Objects.equal(linkId, attribute.linkId) && Objects.equal(potential, attribute.potential) && Objects.equal(nextCards, attribute.nextCards);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(leaderSkillSetId, leaderSkill, passiveSkillSetId, passiveSkillName, passiveSkillDesc, activeSkillId, activeSkillName, activeSkillEffect, activeSkillCondition, skillLevelMax, freeCardFlag, dokkanFesFlag, carnivalFlag, categoryId, linkId, potential, nextCards);
    }
}
