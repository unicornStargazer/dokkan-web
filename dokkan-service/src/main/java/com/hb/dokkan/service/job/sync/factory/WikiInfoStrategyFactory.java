package com.hb.dokkan.service.job.sync.factory;

import com.hb.dokkan.common.constants.ExceptionErrorCode;
import com.hb.dokkan.common.exception.domain.DokkanBizException;
import com.hb.dokkan.service.job.sync.strategy.WikiInfoStrategy;
import com.hb.dokkan.service.job.sync.strategy.enums.WikiInfoTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description Wiki 信息同步策略工厂
 * @Author stargazer
 * @Date 2025/6/2 21:00
 **/
@Component
@Slf4j
public class WikiInfoStrategyFactory {

    @Resource
    private List<WikiInfoStrategy> wikiInfoStrategies;

    /**
     * 根据 Wiki 信息类型获取对应同步策略。
     *
     * @param type Wiki 信息类型
     * @return 同步策略
     */
    public WikiInfoStrategy getWikiStrategy(WikiInfoTypeEnum type) {
        for (WikiInfoStrategy strategy : wikiInfoStrategies) {
            if (strategy.isMatched(type)) {
                return strategy;
            }
        }
        log.error("获取策略失败，type:{}", type);
        throw new DokkanBizException(ExceptionErrorCode.HAS_NO_STRATEGY);
    }
}
