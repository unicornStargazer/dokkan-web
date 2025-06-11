package com.hb.dokkan.service.convert;

import com.alibaba.fastjson.JSON;
import com.hb.dokkan.infrastructure.cards.domain.CardPO;
import com.hb.dokkan.service.domain.CardBaseInfoDTO;
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


    List<CardPO> wikiCard2POList(List<CardBaseInfoDTO> wikiCards);


    @Mappings({
            @Mapping(source = "openAt",target = "publishTime"),
            @Mapping(target = "attributes", qualifiedByName = "buildAttributes", source = "cardInfo"),
            @Mapping(target = "cardId",source = "id"),
            @Mapping(target = "cardName",source = "name"),
            @Mapping(target = "type", qualifiedByName = "getType",source = "cardInfo"),
            @Mapping(target = "propType", qualifiedByName = "getPropType",source = "cardInfo")
    })
    CardPO wikiCard2PO(CardBaseInfoDTO cardInfo);

    @Named("buildAttributes")
    default String buildAttributes(CardBaseInfoDTO dto) {
        CardBaseInfoDTO baseInfoDTO = CardBaseInfoDTO.builder()
                .carnivalFlag(dto.getCarnivalFlag())
                .freeCardFlag(dto.getFreeCardFlag())
                .dokkanFesFlag(dto.getDokkanFesFlag())
                .leaderSkill(dto.getLeaderSkill())
                .passiveSkillDesc(dto.getPassiveSkillDesc())
                .build();
        return JSON.toJSONString(baseInfoDTO);
    }

    @Named("getType")
    default Integer getType(CardBaseInfoDTO cardBaseInfoDTO) {
        Integer type = cardBaseInfoDTO.getPropType();
        return CardPropTypeEnum.getProp(type);
    }

    @Named("getPropType")
    default String getPropType(CardBaseInfoDTO cardBaseInfoDTO) {
        Integer type = cardBaseInfoDTO.getPropType();
        return Objects.requireNonNull(CardPropTypeEnum.getCardPropEnumByType(type)).getDescription();
    }
}
