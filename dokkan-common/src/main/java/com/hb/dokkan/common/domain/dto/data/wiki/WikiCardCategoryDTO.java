package com.hb.dokkan.common.domain.dto.data.wiki;

import lombok.*;

import java.util.Date;

/**
 * @Description 卡牌-分类信息
 * @Author stargazer
 * @Date 2025/6/2 16:20
 **/
@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
public class WikiCardCategoryDTO {

    /**
     * 分类id
     */
    private Long id;

    /**
     * 分类名称
     */
    private String name;

     /**
     * 分类开放时间
     */
    private Date openAt;
}
