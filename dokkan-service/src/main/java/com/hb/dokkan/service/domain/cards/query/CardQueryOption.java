package com.hb.dokkan.service.domain.cards.query;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * @Description 卡片查询参数
 * @Author stargazer
 * @Date 2025/12/11 22:49
 **/
@Data
public class CardQueryOption implements Serializable {
    /**
     * 页码
     */
    private Integer pageNum;

    /**
     * 每页数量
     */
    private Integer pageSize;
    /**
     * 卡片名称
     */
    private String cardName;
    /**
     * 卡片id
     */
    private Long cardId;
    /**
     * 属性名称
     * @see com.hb.dokkan.service.enums.CardPropTypeEnum
     */
    private String propType;
    /**
     * 超系或极系
     * @see com.hb.dokkan.service.enums.CardPropTypeEnum
     */
    private String type;

    /**
     * 稀有度
     * @see com.hb.dokkan.service.enums.CardRarityEnum
     */
    private String rarity;


    /**
     * 链接id列表
     */
    private List<Integer> linkIds;

    /**
     * 分类id列表
     */
    private List<Integer> categoryIds;

    /**
     * 初始化分页参数
     */
    public void initPageable() {
        if (Objects.nonNull(pageNum) && Objects.nonNull(pageSize)) {
            return;
        }
        if (Objects.isNull(pageNum)) {
            pageNum = 1;
        }
        if (Objects.isNull(pageSize)) {
            pageSize = 50;
        }
    }

}
