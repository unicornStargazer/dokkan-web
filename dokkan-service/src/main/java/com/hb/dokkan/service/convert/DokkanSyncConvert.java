package com.hb.dokkan.service.convert;

import com.alibaba.fastjson.JSON;
import com.hb.dokkan.infrastructure.cards.domain.CardPO;
import com.hb.dokkan.service.domain.CardBaseInfoDTO;
import org.mapstruct.*;

import java.util.List;

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
            @Mapping(target = "attributes", expression = "java(buildAttributes(cardInfo))")
    })
    CardPO wikiCard2PO(CardBaseInfoDTO cardInfo);

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
}
