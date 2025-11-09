package com.hb.dokkan.service.job.sync.strategy.context;

import com.hb.dokkan.service.domain.card.bo.WikiCardBO;
import com.hb.dokkan.service.domain.wiki.WikiCardDTO;
import com.hb.dokkan.service.domain.wiki.WikiCategoryDTO;
import lombok.*;

import java.util.List;

/**
 * @Description WIKI上下文
 * @Author stargazer
 * @Date 2025/6/2 21:10
 **/
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
@NoArgsConstructor
public class WikiContext {

    /***************************卡牌信息****************************/

    /**
     * wiki爬取的卡牌全量信息
     */
    List<WikiCardDTO> wikiCards;

    /**
     * 卡牌数据
     */
    WikiCardBO cardData;
    /***************************卡牌信息****************************/

    /***************************分类信息****************************/
     /**
     * 分类数据
     */
    List<WikiCategoryDTO> categoryData;

    /***************************分类信息****************************/


}
