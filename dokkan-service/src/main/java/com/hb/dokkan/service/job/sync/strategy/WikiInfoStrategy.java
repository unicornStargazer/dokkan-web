package com.hb.dokkan.service.job.sync.strategy;

import com.hb.dokkan.service.job.sync.strategy.context.WikiContext;
import com.hb.dokkan.service.job.sync.strategy.enums.WikiInfoTypeEnum;

/**
 * @Description wiki策略
 * @Author stargazer
 * @Date 2025/6/2 21:01
 **/
public interface WikiInfoStrategy {
    /**
     * 是否匹配
     */
    boolean isMatched(WikiInfoTypeEnum type);

    /**
     * 执行策略
     */
    void execute(WikiContext context);

}
