package com.hb.dokkan.common.domain.dto.data.wiki;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
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
public class WikiCardTransformationDTO {

    /**
     * 变身后卡牌id
     */
    @JsonProperty("next_card_id")
    private Long nextCardId;

    /**
     * 前一形态卡牌id
     */
    @JsonProperty("start_card_id")
    private Long startCardId;

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
         * 变身类型:131-双向交替 103-变身  79-巨大化
         */
        private int type;

        /**
         * 变身条件
         */
        @JsonProperty("description")
        private String transformCondition;

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof NextCardDTO that)) return false;
            return type == that.type && Objects.equal(id, that.id) && Objects.equal(name, that.name) && Objects.equal(transformCondition, that.transformCondition);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(id, name, type, transformCondition);
        }
    }
}
