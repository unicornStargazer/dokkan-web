package com.hb.dokkan.common.domain.response.cards;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Description 卡片列表响应类
 * @Author stargazer
 * @Date 2025/12/10 22:08
 **/
@Data
public class CardListResponse implements Serializable {
    private static final long serialVersionUID = -4038210358736048697L;

/******************* 基础信息-start *******************/
    /**
     * 卡片id
     */
    private Long cardId;
    /**
     * 卡片name
     */
    private String cardName;
    /**
     * 卡片描述
     */
    private String title;
    /**
     * 属性（超 极）
     */
    private Integer type;
    /**
     *  具体属性
     */
    private String propType;
    /**
     * cost
     */
    private Integer cost;
    /**
     * 稀有度
     */
    private Integer rarity;

    /**
     * 发布时间
     */
    private Date publishTime;


    /**
     * 免费标志
     */
    private Boolean freeCardFlag;
    /**
     * 限定标志
     */
    private Boolean dokkanFesFlag;
    /**
     * 祭限定
     */
    private Boolean carnivalFlag;

    /**
     * 极限标志
     */
    private Boolean ezaFlag;

    /**
     * 超极限标志
     */
    private Boolean superEzaFlag;

    /**
     * 分类 逗号分割
     */
    private List<String> categories;

    /**
     * 链接
     */
    private List<String> links;


}
