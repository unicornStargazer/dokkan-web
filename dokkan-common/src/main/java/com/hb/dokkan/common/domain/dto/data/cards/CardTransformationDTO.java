package com.hb.dokkan.common.domain.dto.data.cards;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * 卡片变身dto
 *
 * @author huangbiao
 * @date 2026/1/5 15:47
 **/
@Data
public class CardTransformationDTO {
    /**
     * 变身后卡牌id
     */
    private Long nextCardId;

    /**
     * 前一形态卡牌id
     */
    private Long startCardId;

    /**
     * 变身后卡牌信息
     */
    private NextCardDTO nextCard;


    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NextCardDTO {

        /**
         * 变身后卡牌id
         */
        private Long id;
        /**
         * 变身后卡牌名称
         */
        private String name;

        /**
         * 变身类型
         *
         * @see com.hb.dokkan.common.enums.CardTransformationTypeEnum
         */
        private int type;

        /**
         * 变身条件
         */
        private String transformCondition;
    }
}
