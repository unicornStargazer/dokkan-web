package com.hb.dokkan.service.job.sync.strategy.context;

import com.hb.dokkan.service.domain.card.bo.WikiCardBO;
import com.hb.dokkan.service.domain.wiki.WikiCardDTO;
import lombok.*;

import java.util.List;

/**
 * @Description xxxxx
 * @Author stargazer
 * @Date 2025/6/2 21:10
 **/
@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
public class WikiContext {

    /**
     * wiki爬取的卡牌全量信息
     */
    private List<WikiCardDTO> wikiCards;

    /**
     * wiki卡牌html
     */
    private String htmlContent;

    /**
     * 卡牌数据
     */
    private WikiCardBO cardData;

}
