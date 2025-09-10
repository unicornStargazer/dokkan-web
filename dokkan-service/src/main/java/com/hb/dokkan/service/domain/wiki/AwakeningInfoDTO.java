package com.hb.dokkan.service.domain.wiki;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * @Description xxxxx
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

    private Long cardId;

    @JsonProperty("awaked_card_id")
    private Long awakedCardId;

}
