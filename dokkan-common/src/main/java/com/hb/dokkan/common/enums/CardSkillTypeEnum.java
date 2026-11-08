package com.hb.dokkan.common.enums;

import com.hb.dokkan.common.constants.CardSyncConstants;
import lombok.Getter;

/**
 * 卡片技能类型枚举。
 **/
@Getter
public enum CardSkillTypeEnum {
    SUPER(CardSyncConstants.CARD_SKILL_SUPER),
    ACTIVE(CardSyncConstants.CARD_SKILL_ACTIVE),
    STANDBY(CardSyncConstants.CARD_SKILL_STANDBY),
    FINISH(CardSyncConstants.CARD_SKILL_FINISH);

    /**
     * 技能持久化类型键，与源站和 ES 统一。
     **/
    private final String type;

    /**
     * 绑定持久化和 ES 使用的技能类型值。
     **/
    CardSkillTypeEnum(String type) {
        this.type = type;
    }
}
