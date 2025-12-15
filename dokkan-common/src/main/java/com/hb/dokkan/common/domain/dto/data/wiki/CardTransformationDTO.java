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

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof NextCardDTO that)) return false;
            return Objects.equal(id, that.id) && Objects.equal(name, that.name) && Objects.equal(baseId, that.baseId);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(id, name, baseId);
        }
    }
}
