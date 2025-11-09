package com.hb.dokkan.service.domain.wiki;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description 链接数据传输对象
 * @Author stargazer
 * @Date 2025/11/9 16:01
 **/
@Data
public class WikiLinkDTO implements Serializable {
    private static final long serialVersionUID = -4455670674127549051L;

    /**
     * 链接id
     */
    @JsonProperty("id")
    private Long linkId;
    /**
     * 链接名称
     */
    @JsonProperty("name")
    private String linkName;
    /**
     * 一级描述
     */
    @JsonProperty("level1_description")
    private String level1Description;
    /**
     * 满级描述
     */
    @JsonProperty("level10_description")
    private String level10Description;

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof WikiLinkDTO that)) return false;
        return Objects.equal(linkId, that.linkId) && Objects.equal(linkName, that.linkName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(linkId, linkName);
    }
}
