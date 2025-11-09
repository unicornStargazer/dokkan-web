package com.hb.dokkan.service.domain.wiki;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description 分类wiki信息
 * @Author stargazer
 * @Date 2025/11/9 14:49
 **/
@Data
public class WikiCategoryDTO implements Serializable {

    /**
     * 分类id
     */
    @JsonProperty("id")
    private Long categoryId;

    /**
     * 分类名称
     */
    @JsonProperty("name")
    private String categoryName;

    /**
     * 发布时间
     */
    @JsonProperty("open_at")
    private String publishTime;

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof WikiCategoryDTO that)) return false;
        return Objects.equal(categoryId, that.categoryId) && Objects.equal(categoryName, that.categoryName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(categoryId, categoryName);
    }
}
