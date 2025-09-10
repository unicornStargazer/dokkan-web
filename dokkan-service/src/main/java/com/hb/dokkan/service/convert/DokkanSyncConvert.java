package com.hb.dokkan.service.convert;

import com.hb.dokkan.infrastructure.cards.domain.CardPO;
import com.hb.dokkan.service.domain.dto.CardBaseInfoDTO;
import com.hb.dokkan.service.domain.wiki.WikiCardBaseInfoDTO;
import com.hb.dokkan.service.enums.CardPropTypeEnum;
import org.mapstruct.*;

import java.util.List;
import java.util.Objects;

/**
 * @Description xxxxx
 * @Author stargazer
 * @Date 2025/6/2 21:59
 **/
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface DokkanSyncConvert {


    List<CardPO> wikiCard2POList(List<WikiCardBaseInfoDTO> wikiCards);


    @Mappings({
            @Mapping(source = "openAt",target = "publishTime"),
            @Mapping(target = "attributes", qualifiedByName = "buildAttributes", source = "cardInfo"),
            @Mapping(target = "cardId",source = "id"),
            @Mapping(target = "cardName",source = "name"),
            @Mapping(target = "type", qualifiedByName = "getType",source = "cardInfo"),
            @Mapping(target = "propType", qualifiedByName = "getPropType",source = "cardInfo"),
            @Mapping(target = "id", ignore = true)
    })
    CardPO wikiCard2PO(WikiCardBaseInfoDTO cardInfo);



    @Mappings({
            @Mapping(source = "openAt",target = "publishTime"),
            @Mapping(target = "cardId",source = "id"),
            @Mapping(target = "cardName",source = "name"),
            @Mapping(target = "type", qualifiedByName = "getType",source = "cardInfo"),
            @Mapping(target = "propType", qualifiedByName = "getPropType",source = "cardInfo"),
    })
    CardBaseInfoDTO wikiCard2Dto(WikiCardBaseInfoDTO wikiCard);
}
