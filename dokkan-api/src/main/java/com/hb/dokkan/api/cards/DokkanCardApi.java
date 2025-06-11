package com.hb.dokkan.api.cards;

import com.hb.dokkan.common.domain.DokkanResponse;
import com.hb.dokkan.service.cards.DokkanCardService;
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
@RequestMapping("/card")
public class DokkanCardApi {

    @Resource
    private DokkanCardService cardService;

    @PostMapping("/init-card")
    public DokkanResponse initCard(){
        return cardService.initCard();
    }



}
