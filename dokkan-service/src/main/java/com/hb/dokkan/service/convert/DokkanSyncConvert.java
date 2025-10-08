package com.hb.dokkan.service.convert;

import com.hb.dokkan.infrastructure.cards.domain.CardPO;
import com.hb.dokkan.infrastructure.cards.domain.EzaCardPO;
import com.hb.dokkan.infrastructure.cards.domain.SkillPO;
import com.hb.dokkan.infrastructure.cards.domain.SpecialPO;
import com.hb.dokkan.service.domain.dto.base.*;
import com.hb.dokkan.service.domain.wiki.WikiCardBaseInfoDTO;
import com.hb.dokkan.service.domain.wiki.WikiEzaCardDTO;
import com.hb.dokkan.service.domain.wiki.WikiSkillDTO;
import com.hb.dokkan.service.domain.wiki.WikiSpecialAttackDTO;
import com.hb.dokkan.service.enums.CardPropTypeEnum;
import org.mapstruct.*;

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
     * wiki转eza
     */
    List<EzaCardInfoDTO> wikiCard2EzaDtoList(List<WikiEzaCardDTO> ezaCardInfos);

    @Mapping(source = "id", target = "cardId")
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
}
