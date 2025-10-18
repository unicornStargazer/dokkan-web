package com.hb.dokkan.service.cards;

import com.hb.dokkan.common.domain.DokkanResponse;

/**
 * @Description
 * @Author stargazer
 * @Date 2025/6/2 21:40
 **/
public interface DokkanCardDataService {
    /**
     * 初始化卡片数据
     */
    DokkanResponse initCard();

    /**
     * 同步es卡片数据
     */
    DokkanResponse syncEsCardData();

}
