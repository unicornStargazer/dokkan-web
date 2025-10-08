package com.hb.dokkan.service.domain.wiki;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * @Description 觉醒卡牌
 * @Author stargazer
 * @Date 2025/6/2 18:27
 **/
@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AwakeningInfoDTO {

    /**
     * 卡牌id
     */
    @JsonProperty("card_id")
    private Long cardId;

    /**
     * dk后卡牌id
     */
    @JsonProperty("awaked_card_id")
    private Long awakedCardId;

}
