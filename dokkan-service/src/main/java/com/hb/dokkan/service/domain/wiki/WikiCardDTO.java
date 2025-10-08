package com.hb.dokkan.service.domain.wiki;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

/**
 * @Description 卡牌聚合信息
 * @Author stargazer
 * @Date 2025/6/2 2:16
 **/
@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WikiCardDTO {
    /**
     * 卡的基础信息
     */
    private WikiCardBaseInfoDTO card;

    /**
     * 卡的极限及超极限信息
     */
    @JsonProperty("optimal_awakening_growths")
    private List<WikiEzaCardDTO> ezaCardInfos;

    /**
     * 三围潜力值
     */
    private List<PotentialDTO> potential;

    /**
     * 分类
     */
    private List<WikiCategoryDTO> categories;

    /**
     * 必杀
     */
    private List<WikiSpecialAttackDTO> specials;

    /**
     * 觉醒相关信息
     */
    @JsonProperty("awakening_routes")
    List<AwakeningInfoDTO> awakeningRoutes;
    /**
     * 链接
     */
    @JsonProperty("card_links")
    private List<WikiLinkDTO> cardLinks;

    /**
     * 变身相关
     */
    private List<CardTransformationDTO> transformations;

    /**
     * 下拉释放
     */
    @JsonProperty("finish_skills")
    private List<WikiSkillDTO> finishSkills;

    /**
     * 下拉待定
     */
    @JsonProperty("standby_skills")
    private List<WikiSkillDTO> standbySkills;
}
