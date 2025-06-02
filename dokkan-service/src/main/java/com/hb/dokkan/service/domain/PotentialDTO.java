package com.hb.dokkan.service.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * @Description xxxxx
 * @Author stargazer
 * @Date 2025/6/2 16:17
 **/
@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PotentialDTO {

    private Integer order;

    private Number hp;

    private Number atk;

    private Number def;

}
