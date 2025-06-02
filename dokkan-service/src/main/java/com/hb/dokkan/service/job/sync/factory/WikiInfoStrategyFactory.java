package com.hb.dokkan.service.job.sync.factory;

import com.alibaba.fastjson.JSON;
import com.hb.dokkan.common.constants.ResponseErrorCode;
import com.hb.dokkan.common.exception.domain.DokkanBizException;
import com.hb.dokkan.service.job.sync.strategy.WikiInfoStrategy;
import com.hb.dokkan.service.job.sync.strategy.enums.WikiInfoTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description xxxxx
 * @Author stargazer
 * @Date 2025/6/2 21:00
 **/
@Component
@Slf4j
public class WikiInfoStrategyFactory {

    @Resource
    private List<WikiInfoStrategy> wikiInfoStrategies;

    public WikiInfoStrategy getWikiStrategy(WikiInfoTypeEnum type) {
        for (WikiInfoStrategy strategy : wikiInfoStrategies) {
            if (strategy.isMatched(type)){
                return strategy;
            }
        }
        log.error("获取策略失败，type:{}", JSON.toJSONString(type));
        throw new DokkanBizException(ResponseErrorCode.HAS_NO_STRATEGY);
    }

}
