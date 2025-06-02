package com.hb.dokkan.service.cards.impl;

import com.hb.dokkan.common.domain.DokkanResponse;
import com.hb.dokkan.service.cards.DokkanCardService;
import com.hb.dokkan.service.job.sync.SyncDataService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Description xxxxx
 * @Author stargazer
 * @Date 2025/6/2 21:41
 **/
@Service
@Slf4j
public class DokkanCardServiceImpl implements DokkanCardService {

    @Resource
    private SyncDataService syncDataService;

    /**
     * 初始化卡片数据
     */
    @Override
    public DokkanResponse initCard() {
        try {
            syncDataService.initCard();
            return DokkanResponse.builder().success();
        } catch (Exception e) {
            log.error("DokkanCardService#initCard error,",e);
            return DokkanResponse.builder().fail();
        }
    }
}
