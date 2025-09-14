package com.hb.dokkan.service.domain.wiki;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * @Description 变身模型
 * @Author stargazer
 * @Date 2025/6/2 16:23
 **/
@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardTransformationDTO {

    /**
     * 变身后卡牌id
     */
    @JsonProperty("next_card_id")
    private Long nextCardId;

    /**
     * 变身后卡牌信息
     */
    @JsonProperty("next_card")
    private NextCardDTO nextCard;


    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NextCardDTO {

        /**
         *变身后卡牌id
         */
        private Long id;
        /**
         * 变身后卡牌名称
         */
        private String name;
        /**
         * 初始卡面id
         */
        @JsonProperty("base_id")
        private Long baseId;

    }
}
