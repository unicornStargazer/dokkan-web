package com.hb.dokkan.common.enums;

import com.hb.dokkan.common.constants.CardSyncConstants;
import lombok.Getter;

/**
 * 卡片状态枚举，统一表示 KX Wiki 的状态节点。
 **/
@Getter
public enum CardStateEnum {
    BASE(CardSyncConstants.CARD_STATE_BASE),
    EZA(CardSyncConstants.CARD_STATE_EZA),
    SEZA(CardSyncConstants.CARD_STATE_SEZA);

    /**
     * 卡片状态键：base、eza 或 seza。
     **/
    private final String key;

    /**
     * 绑定源响应与持久化使用的状态键。
     **/
    CardStateEnum(String key) {
        this.key = key;
    }
}
