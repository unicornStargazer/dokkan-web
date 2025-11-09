package com.hb.dokkan.service.data;

import com.hb.dokkan.common.domain.DokkanResponse;

/**
 * @Description 数据服务
 * @Author stargazer
 * @Date 2025/6/2 21:40
 **/
public interface DokkanDataService {
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
