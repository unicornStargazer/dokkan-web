package com.hb.dokkan.service.domain.wiki;

import lombok.*;

/**
 * @Description 分类信息
 * @Author stargazer
 * @Date 2025/6/2 16:20
 **/
@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
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
