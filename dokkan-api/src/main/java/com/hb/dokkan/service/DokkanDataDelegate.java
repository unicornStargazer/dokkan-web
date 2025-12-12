package com.hb.dokkan.service;

import com.hb.dokkan.common.domain.DokkanResponse;

/**
 * @Description 控制层-数据服务
 * @Author stargazer
 * @Date 2025/12/12 23:25
 **/
public interface DokkanDataDelegate {
    /**
     * 初始化卡片数据
     */
    DokkanResponse initCard();

    /**
     * 同步es卡片数据
     */
    DokkanResponse syncEsCardData();

    /**
     * 初始化分类数据
     */
    DokkanResponse initCategories();

    /**
     * 初始化链接数据
     */
    DokkanResponse initLinks();
}
