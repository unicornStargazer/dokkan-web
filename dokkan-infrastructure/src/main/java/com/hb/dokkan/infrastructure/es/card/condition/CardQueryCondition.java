package com.hb.dokkan.infrastructure.es.card.condition;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @Description 查询条件类
 * @Author stargazer
 * @Date 2025/12/14 1:03
 **/
@Data
public class CardQueryCondition implements Serializable {
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
     */
    private String propType;
    /**
     * 超系或极系
     */
    private String type;

    /**
     * 稀有度
     */
    private String rarity;


    /**
     * 链接id列表
     */
    private List<String> links;

    /**
     * 分类id列表
     */
    private List<String> categories;

    /**
     * 排序字段
     * key: 排序字段名
     * value: true: 升序, false: 降序
     */
    private Map<String, Boolean> orderBy;
}
