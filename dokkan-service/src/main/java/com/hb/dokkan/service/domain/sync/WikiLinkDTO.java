package com.hb.dokkan.service.domain.sync;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * @Description xxxxx
 * @Author stargazer
 * @Date 2025/6/2 16:22
 **/
@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WikiLinkDTO {

    private Long id;

    private String name;

    @JsonProperty("level10_description")
    private String desc;

}
