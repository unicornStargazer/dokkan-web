package com.hb.dokkan.service.job.sync.strategy;

import com.hb.dokkan.service.job.sync.strategy.context.WikiContext;
import com.hb.dokkan.service.job.sync.strategy.enums.WikiInfoTypeEnum;

/**
 * @Description xxxxx
 * @Author stargazer
 * @Date 2025/6/2 21:01
 **/
public interface WikiInfoStrategy {
    boolean isMatched(WikiInfoTypeEnum type);

    void execute(WikiContext context);

}
