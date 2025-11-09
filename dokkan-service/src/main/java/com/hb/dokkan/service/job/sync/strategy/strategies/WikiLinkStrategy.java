package com.hb.dokkan.service.job.sync.strategy.strategies;

import com.hb.dokkan.common.utils.TranslationUtils;
import com.hb.dokkan.service.domain.wiki.WikiLinkDTO;
import com.hb.dokkan.service.facade.WikiFacade;
import com.hb.dokkan.service.job.sync.strategy.WikiInfoStrategy;
import com.hb.dokkan.service.job.sync.strategy.context.WikiContext;
import com.hb.dokkan.service.job.sync.strategy.enums.WikiInfoTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Description 链接信息策略
 * @Author stargazer
 * @Date 2025/11/9 14:44
 **/
@Component
@Slf4j
public class WikiLinkStrategy implements WikiInfoStrategy {

    @Resource
    private WikiFacade wikiFacade;

    /**
     * 是否匹配
     *
     * @param type
     */
    @Override
    public boolean isMatched(WikiInfoTypeEnum type) {
        return WikiInfoTypeEnum.LINK.equals(type);
    }

    /**
     * 执行策略
     *
     */
    @Override
    public void execute(WikiContext context) {
        List<WikiLinkDTO> links = wikiFacade.getWikiLink();
        if (!CollectionUtils.isEmpty(links)) {
            List<WikiLinkDTO> data = TranslationUtils.toSimpleChinese(links);
            context.setLinkData(data);
        }
    }
}
