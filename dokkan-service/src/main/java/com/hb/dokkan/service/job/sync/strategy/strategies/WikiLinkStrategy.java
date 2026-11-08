package com.hb.dokkan.service.job.sync.strategy.strategies;

import com.hb.dokkan.service.client.KxWikiClient;
import com.hb.dokkan.service.job.sync.strategy.WikiInfoStrategy;
import com.hb.dokkan.service.job.sync.strategy.context.WikiContext;
import com.hb.dokkan.service.job.sync.strategy.enums.WikiInfoTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * @Description 从 KX Wiki reference 读取链接定义
 * @Author Codex
 * @Date 2026/11/8
 **/
@Component
public class WikiLinkStrategy implements WikiInfoStrategy {
    /**
     * KX Wiki 数据访问客户端。
     **/
    @Resource
    private KxWikiClient client;

    /**
     * 匹配链接同步任务。
     **/
    @Override
    public boolean isMatched(WikiInfoTypeEnum type) {
        return type == WikiInfoTypeEnum.LINK;
    }

    /**
     * reference 只提供一级描述，满级描述仍由卡片详情补齐。
     **/
    @Override
    public void execute(WikiContext context) {
        context.setLinkData(client.listLinks());
    }
}
