package com.hb.dokkan.common.domain.po.mysql.cards;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hb.dokkan.common.domain.po.mysql.base.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @Description 卡牌模型表
 * @Author stargazer
 * @Date 2025/3/9 19:37
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("card")
public class CardPO extends BasePO {
    /**
     * 卡片id
     **/
    private Long cardId;
    /**
     * 卡片name
     **/
    private String cardName;
    /**
     * 形态全名。
     **/
    private String unitName;
    /**
     * 卡片描述
     **/
    private String title;
    /**
     * 属性（超 极）
     **/
    private Integer type;
    /**
     *  具体属性
     **/
    private String propType;
    /**
     * cost
     **/
    private Integer cost;
    /**
     * 稀有度
     **/
    private Integer rarity;
    /**
     * 角色 ID。
     **/
    private Integer characterId;
    /**
     * 源属性码。
     **/
    private Integer element;
    /**
     * 潜力预设 JSON。
     **/
    private String potential;
    /**
     * 资源 ID。
     **/
    private Long resourceId;
    /**
     * 本体卡 ID，形态卡使用。
     **/
    private Long baseCardId;
    /**
     * KX Wiki 形态类型。
     **/
    private String formKind;
    /**
     * 形态触发条件。
     **/
    private String formTrigger;
    /**
     * 发布时间
     **/
    private Date publishTime;
    /**
     * 是否存在 EZA。
     **/
    private Boolean ezaFlag;
    /**
     * 是否存在 SEZA。
     **/
    private Boolean sezaFlag;
    /**
     * 祭限定标记。
     **/
    private Boolean dokkanFesFlag;
    /**
     * 嘉年华标记。
     **/
    private Boolean carnivalFlag;
    /**
     * 免费卡标记。
     **/
    private Boolean freeCardFlag;
    /**
     * 人工标签锁定标记。
     **/
    private Boolean tagLocked;
    /**
     * KX Wiki 头像源地址。
     **/
    private String thumbnail;
    /**
     * KX Wiki 源数据哈希。
     **/
    private String sourceHash;
    /**
     * 最近同步时间。
     **/
    private Date syncedTime;
    /**
     * 扩展属性
     **/
    private String attributes;


}
