package com.hb.dokkan.common.domain.vo.cards;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description 卡片列表vo
 * @Author stargazer
 * @Date 2025/12/11 22:34
 **/
@Data
public class CardListVO implements Serializable {
    private static final long serialVersionUID = 2977063380419132165L;


    /******************* 基础信息-start *******************/
    /**
     * 卡片id
     */
    private Long cardId;
    private String cardIcon;
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
    private String categories;

    /**
     * 链接
     */
    private String links;

}
