package com.hb.dokkan.common.domain.po.mysql.cards;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hb.dokkan.common.domain.po.mysql.base.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 卡片状态持久化对象。
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("card_state")
public class CardStatePO extends BasePO implements Serializable {
    /**
     * 卡片业务 ID，本体卡和形态卡均使用源站 ID。
     **/
    private Long cardId;
    /**
     * 卡片状态键：base、eza 或 seza。
     **/
    private String stateKey;
    /**
     * 觉醒阶段序号，base 状态为 0。
     **/
    private Integer step;
    /**
     * 当前状态的角色等级上限。
     **/
    private Integer lvMax;
    /**
     * 当前状态的必杀等级上限。
     **/
    private Integer saLvMax;
    /**
     * 当前状态的满级生命值，不含潜力等额外加成。
     **/
    private Long hpValue;
    /**
     * 当前状态的满级攻击值，不含潜力等额外加成。
     **/
    private Long atkValue;
    /**
     * 当前状态的满级防御值，不含潜力等额外加成。
     **/
    private Long defValue;
    /**
     * 队长技业务 ID，形态卡继承本体对应状态。
     **/
    private Long leaderSkillId;
    /**
     * 队长技名称。
     **/
    private String leaderSkillName;
    /**
     * 队长技描述，优先保留源站模板文本。
     **/
    private String leaderSkillDesc;
    /**
     * 被动技能业务 ID。
     **/
    private Long passiveSkillId;
    /**
     * 被动技能名称。
     **/
    private String passiveSkillName;
    /**
     * 被动描述，保留 passiveImg 等源站模板占位符。
     **/
    private String passiveSkillDesc;
    /**
     * 当前状态的开放时间，由源站 UTC 时间转换。
     **/
    private Date publishTime;
    /**
     * 状态扩展属性 JSON。
     **/
    private String attributes;
}
