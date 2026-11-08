package com.hb.dokkan.service.job.sync.strategy.strategies;

import com.hb.dokkan.common.constants.CardSyncConstants;
import com.hb.dokkan.config.kx.KxWikiProperties;
import com.hb.dokkan.service.client.KxWikiClient;
import com.hb.dokkan.service.job.sync.strategy.WikiInfoStrategy;
import com.hb.dokkan.service.job.sync.strategy.context.WikiContext;
import com.hb.dokkan.service.job.sync.strategy.enums.WikiInfoTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * @Description 从 KX Wiki reference 读取中英文分类
 * @Author Codex
 * @Date 2026/11/8
 **/
@Component
public class WikiCategoryStrategy implements WikiInfoStrategy {
    /**
     * KX Wiki 数据访问客户端。
     **/
    @Resource
    private KxWikiClient client;
    /**
     * KX Wiki 客户端运行配置。
     **/
    @Resource
    private KxWikiProperties properties;

    /**
     * 匹配分类同步任务。
     **/
    @Override
    public boolean isMatched(WikiInfoTypeEnum type) {
        return type == WikiInfoTypeEnum.CATEGORY;
    }

    /**
     * 全部分页读取成功后交由编排层事务保存，避免失败页造成部分更新。
     **/
    @Override
    public void execute(WikiContext context) {
        context.setCategoryData(client.listCategories(properties.getLang()));
        context.setEnglishCategoryData(client.listCategories(CardSyncConstants.KX_WIKI_ENGLISH_LANGUAGE));
    }
}
