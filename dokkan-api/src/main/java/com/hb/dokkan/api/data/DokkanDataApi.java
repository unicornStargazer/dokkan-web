package com.hb.dokkan.api.data;

import com.hb.dokkan.common.domain.DokkanResponse;
import com.hb.dokkan.service.DokkanDataDelegate;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description card服务
 * @Author stargazer
 * @Date 2025/6/2 21:38
 **/
@RestController
@RequestMapping("/data")
public class DokkanDataApi {

    @Resource
    private DokkanDataDelegate dataDelegate;

    /**
     * 同步wiki卡牌数据
     * @return
     */
    @PostMapping("/init-card")
    public DokkanResponse initCard(){
        return dataDelegate.initCard();
    }

    /**
     * 同步es卡牌数据
     */
    @PostMapping("/sync-es-card")
    public DokkanResponse syncEsCardData(){
        return dataDelegate.syncEsCardData();
    }


    /**
     * 初始化分类数据
     */
    @PostMapping("/init-category")
    public DokkanResponse initCategories(){
        return dataDelegate.initCategories();
    }


    /**
     * 初始化链接数据
     */
    @PostMapping("/init-link")
    public DokkanResponse initLinks(){
        return dataDelegate.initLinks();
    }



}
