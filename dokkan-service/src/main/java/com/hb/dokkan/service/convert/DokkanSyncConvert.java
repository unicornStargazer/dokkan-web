package com.hb.dokkan.service.convert;

import com.hb.dokkan.infrastructure.cards.domain.CardPO;
import com.hb.dokkan.service.domain.dto.base.CardBaseInfoAttribute;
import com.hb.dokkan.service.domain.dto.base.CardBaseInfoDTO;
import com.hb.dokkan.service.domain.wiki.WikiCardBaseInfoDTO;
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
    CardBaseInfoDTO wikiCard2Dto(WikiCardBaseInfoDTO wikiCard);






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
}
