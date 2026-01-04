package com.hb.dokkan.common.domain.dto.data.wiki;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Date;

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

    @JsonProperty("awaked_card")
    private AwakenDetailDTO awakenDetail;

    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AwakenDetailDTO {
        /**
         * 觉醒类型
         * @see com.hb.dokkan.common.enums.CardAwakeningTypeEnum
         */
        private String type;

        /**
         * 觉醒id
         */
        private Long id;

        /**
         * 稀有度
         * @see com.hb.dokkan.common.enums.CardRarityEnum
         */
        private int rarity;

        /**
         * 开放时间
         */
        @JsonProperty("open_at")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "GMT+8")
        private Date openAt;
    }


}
