package com.hb.dokkan.service.job.sync;

/**
 * @Description 数据同步服务
 * @Author stargazer
 * @Date 2025/6/2 20:50
 **/
public interface SyncDataService {

    /**
     * 初始化卡片数据
     */
     void initCard();

     /**
     * 同步es卡片数据
     */
    void syncEsCardData();

}
