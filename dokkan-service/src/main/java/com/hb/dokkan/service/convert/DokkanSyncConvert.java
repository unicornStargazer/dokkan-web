package com.hb.dokkan.service.convert;

import com.hb.dokkan.common.utils.DateUtils;
import com.hb.dokkan.infrastructure.mysql.cards.domain.CardPO;
import com.hb.dokkan.infrastructure.mysql.cards.domain.EzaCardPO;
import com.hb.dokkan.infrastructure.mysql.cards.domain.SkillPO;
import com.hb.dokkan.infrastructure.mysql.cards.domain.SpecialPO;
import com.hb.dokkan.infrastructure.mysql.categories.domain.DokkanCategoryPO;
import com.hb.dokkan.infrastructure.mysql.links.domain.DokkanLinkPO;
import com.hb.dokkan.service.domain.card.dto.*;
import com.hb.dokkan.service.domain.wiki.*;
import com.hb.dokkan.service.enums.CardPropTypeEnum;
import org.mapstruct.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @Description 同步数据转换类
 * @Author stargazer
 * @Date 2025/6/2 21:59
 **/
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface DokkanSyncConvert {

    /**
     * dto 转数据库模型
     */
    List<CardPO> wikiCard2POList(List<CardBaseInfoDTO> wikiCards);


    /**
     * wiki 转 dto
     */

    @Mappings({
            @Mapping(source = "openAt",target = "publishTime"),
            @Mapping(target = "cardId",source = "id"),
            @Mapping(target = "cardName",source = "name"),
            @Mapping(target = "type", qualifiedByName = "getType",source = "propType"),
            @Mapping(target = "propType", qualifiedByName = "getPropType",source = "propType"),
    })
    CardBaseInfoDTO wikiCard2BaseDto(WikiCardBaseInfoDTO wikiCard);






    @Named("getType")
    default Integer getType(Integer type) {
        return CardPropTypeEnum.getProp(type);
    }

    @Named("getPropType")
    default String getPropType(Integer type) {
        return Optional.ofNullable(CardPropTypeEnum.getCardPropEnumByType(type)).map(CardPropTypeEnum::getDescription).orElse(null);
    }

    /**
     * wiki 转 attribute
     */
    CardBaseInfoAttribute wikiCard2Attribute(WikiCardBaseInfoDTO card);

    /**
     * eza 转 attribute
     */
    @Mappings({
            @Mapping(source = "leaderSkillId", target = "leaderSkillSetId"),
            @Mapping(source = "passiveSkillId",target = "passiveSkillSetId"),
    })
    CardBaseInfoAttribute wikiCard2EzaAttribute(EzaCardInfoDTO card);

    /**
     * wiki转eza
     */
    List<EzaCardInfoDTO> wikiCard2EzaDtoList(List<WikiEzaCardDTO> ezaCardInfos);

    @Mappings(
            @Mapping(source = "openAt",target = "publishTime")
    )
    EzaCardInfoDTO wikiCard2EzaDto(WikiEzaCardDTO ezaCardInfo);

    /**
     * wiki转skill
     */

    List<SkillDTO> wikiSkill2DtoList(List<WikiSkillDTO> skills);

    @Mapping(source = "id", target = "skillId")
    SkillDTO wikiSkill2Dto(WikiSkillDTO skills);

    /**
     * wiki 转 必杀
     */
    List<SpecialAttackDTO> wikiSpecial2DtoList(List<WikiSpecialAttackDTO> specials);

    @Mapping(source = "id", target = "specialId")
    SpecialAttackDTO wikiSpecial2Dto(WikiSpecialAttackDTO specials);

    /**
     * skill dto 转 po
     */
    List<SkillPO> wikiSkill2POList(List<SkillDTO> downPullSkills);

    /**
     * eza dto 转 po
     */
    List<EzaCardPO> wikiEza2POList(List<EzaCardInfoDTO> ezaCardInfos);

    /**
     * eza dto 转 po
     */
    List<SpecialPO> wikiSpecial2POList(List<SpecialAttackDTO> specialAttackDTOS);

    /**
     * 分类 dto 转 po
     */
    List<DokkanCategoryPO> convertToCategoryPO(List<WikiCategoryDTO> data);

    @Mapping(target = "publishTime", qualifiedByName = "convertToDate" , source = "publishTime")
    DokkanCategoryPO convertToCategoryPO(WikiCategoryDTO data);
    /**
     * 转换为日期
     */
    @Named("convertToDate")
    default Date convertToDate(String date) {
        return DateUtils.day2Date(date);
    }

    /**
     * 链接 dto 转 po
     */
    List<DokkanLinkPO> convertToLinkPO(List<WikiLinkDTO> data);
}
