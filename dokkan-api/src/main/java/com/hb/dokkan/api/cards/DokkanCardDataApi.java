package com.hb.dokkan.api.cards;

import com.hb.dokkan.common.domain.DokkanResponse;
import com.hb.dokkan.service.cards.DokkanCardDataService;
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
@RequestMapping("/card-data")
public class DokkanCardDataApi {

    @Resource
    private DokkanCardDataService cardService;

    /**
     * 同步wiki卡牌数据
     * @return
     */
    @PostMapping("/init-card")
    public DokkanResponse initCard(){
        return cardService.initCard();
    }

    /**
     * 同步es卡牌数据
     */
    @PostMapping("/sync-es-card")
    public DokkanResponse syncEsCardData(){
        return cardService.syncEsCardData();
    }



}
