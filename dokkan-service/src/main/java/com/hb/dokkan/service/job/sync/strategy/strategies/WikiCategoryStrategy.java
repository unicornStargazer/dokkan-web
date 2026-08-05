package com.hb.dokkan.service.job.sync.strategy.strategies;

import com.hb.dokkan.common.domain.dto.data.wiki.WikiCategoryDTO;
import com.hb.dokkan.service.facade.DokkanDbFacade;
import com.hb.dokkan.service.job.sync.strategy.WikiInfoStrategy;
import com.hb.dokkan.service.job.sync.strategy.context.WikiContext;
import com.hb.dokkan.service.job.sync.strategy.enums.WikiInfoTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Description 分类信息策略
 * @Author stargazer
 * @Date 2025/11/9 14:44
 **/
@Component
@Slf4j
public class WikiCategoryStrategy implements WikiInfoStrategy {

    @Resource
    private DokkanDbFacade dokkanDbFacade;

    /**
     * 是否匹配
     *
     * @param type
     */
    @Override
    public boolean isMatched(WikiInfoTypeEnum type) {
        return WikiInfoTypeEnum.CATEGORY.equals(type);
    }

    /**
     * 执行策略
     *
     */
    @Override
    public void execute(WikiContext context) {
        List<WikiCategoryDTO> categories = dokkanDbFacade.getCategories();
        if (!CollectionUtils.isEmpty(categories)) {
            context.setCategoryData(categories);
        }
    }
}
