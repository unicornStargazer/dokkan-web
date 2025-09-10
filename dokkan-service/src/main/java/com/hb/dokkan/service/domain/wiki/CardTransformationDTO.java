package com.hb.dokkan.service.domain.wiki;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * @Description xxxxx
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

    @JsonProperty("next_card_id")
    private Long nextCardId;

    @JsonProperty("next_card")
    private NextCardDTO nextCard;


    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NextCardDTO {

        private Long id;

        private String name;

        @JsonProperty("base_id")
        private Long baseId;

    }
}
