package com.hb.dokkan.service.domain.sync;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * @Description xxxxx
 * @Author stargazer
 * @Date 2025/6/2 16:20
 **/
@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WikiCategoryDTO {

    /**
     * 分类id
     */
    private Long id;

    /**
     * 分类名称
     */
    private String name;
}
