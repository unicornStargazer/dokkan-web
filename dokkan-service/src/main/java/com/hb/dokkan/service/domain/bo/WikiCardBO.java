package com.hb.dokkan.service.domain.bo;

import com.hb.dokkan.service.domain.dto.base.CardBaseInfoDTO;
import com.hb.dokkan.service.domain.dto.base.EzaCardInfoDTO;
import com.hb.dokkan.service.domain.dto.base.SkillDTO;
import com.hb.dokkan.service.domain.dto.base.SpecialAttackDTO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description wiki card业务对象
 * @Author stargazer
 * @Date 2025/9/15 20:57
 **/
@Data
public class WikiCardBO implements Serializable {
    private static final long serialVersionUID = 4314575338046275359L;

    /**
     * 卡牌基础信息
     */
    private List<CardBaseInfoDTO> cardBaseData;

    /**
     * 卡牌极限信息
     */
    private List<EzaCardInfoDTO> ezaCardInfos;

    /**
     * 卡牌下拉技能信息
     */
    private List<SkillDTO> downPullSkills;

    /**
     * 必杀信息
     */
    private List<SpecialAttackDTO> specialAttacks;
}
