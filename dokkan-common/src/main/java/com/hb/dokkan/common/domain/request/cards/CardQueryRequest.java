package com.hb.dokkan.common.domain.request.cards;

import com.hb.dokkan.common.domain.request.base.PageRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @Description 卡片查询参数
 * @Author stargazer
 * @Date 2025/12/10 22:16
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardQueryRequest extends PageRequest implements Serializable {
    private static final long serialVersionUID = -8967595314460731405L;
    /**
     * 卡片id
     */
    private Long cardId;


    /************************* 卡片列表查询条件 *************************/

    /**
     * 卡片名称
     */
    private String cardName;
    /**
     * 属性名称
     * @see com.hb.dokkan.common.enums.CardPropTypeEnum
     */
    private String propType;
    /**
     * 超系或极系
     * @see com.hb.dokkan.common.enums.CardPropTypeEnum
     */
    private String type;

    /**
     * 稀有度
     * @see com.hb.dokkan.common.enums.CardRarityEnum
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
     * 链接匹配类型 0: and 1: or
     */
    private String linkMatchType;


    /**
     * 链接匹配类型 0: and 1: or
     */
    private String categoryMatchType;

    /**
     * 排序字段
     * key: 排序字段名
     * value: true: 升序, false: 降序
     */
    private Map<String, Boolean> orderBy;


    /************************* 卡片列表查询条件 *************************/


}
