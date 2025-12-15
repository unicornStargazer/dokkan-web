package com.hb.dokkan.common.domain.dto.data.cards;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * @Description 潜能
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

    /**
     * 排序
     */
    private Integer order;

    /**
     * 生命值
     */
    private Number hp;
    /**
     * 攻击值
     */
    private Number atk;
    /**
     * 防御值
     */
    private Number def;

}
