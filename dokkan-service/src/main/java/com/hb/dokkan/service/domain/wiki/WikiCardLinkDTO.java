package com.hb.dokkan.service.domain.wiki;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * @Description card-link信息
 * @Author stargazer
 * @Date 2025/6/2 16:22
 **/
@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
public class WikiCardLinkDTO {

    /**
     * linkId
     */
    @JsonProperty("link_skill_id")
    private Long id;

    /**
     * name
     */
    private String name;

    /**
     * link描述
     */
    @JsonProperty("level10_description")
    private String desc;

}
