package com.hb.dokkan.service.cards;

import com.google.common.collect.Lists;
import com.hb.dokkan.common.utils.CollectionUtils;
import com.hb.dokkan.common.utils.StringUtils;
import com.hb.dokkan.infrastructure.es.card.DokkanEsCardRepository;
import com.hb.dokkan.infrastructure.es.card.condition.CardQueryCondition;
import com.hb.dokkan.infrastructure.es.card.domain.CardEsPO;
import com.hb.dokkan.infrastructure.mysql.categories.DokkanCategoryRepository;
import com.hb.dokkan.infrastructure.mysql.categories.domain.DokkanCategoryPO;
import com.hb.dokkan.infrastructure.mysql.links.DokkanLinkRepository;
import com.hb.dokkan.infrastructure.mysql.links.domain.DokkanLinkPO;
import com.hb.dokkan.service.convert.DokkanCardConvert;
import com.hb.dokkan.service.domain.cards.query.CardQueryOption;
import com.hb.dokkan.service.domain.cards.vo.CardListVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description 卡片服务实现类
 * @Author stargazer
 * @Date 2025/12/10 22:32
 **/
@Service
public class DokkanCardService{

    @Resource
    private DokkanEsCardRepository dokkanEsCardRepository;

    @Resource
    private DokkanCategoryRepository categoryRepository;

    @Resource
    private DokkanLinkRepository linkRepository;

    @Resource
    private DokkanCardConvert cardConvert;


    /**
     * 查询卡片列表
     *
     */
    public List<CardListVO> cardList(CardQueryOption queryOption) {

        List<String> categories = Lists.newArrayList();
        List<String> links = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(queryOption.getCategoryIds())) {
            List<DokkanCategoryPO> categoryPOS = categoryRepository.queryCategoriesByIds(queryOption.getCategoryIds());
            categories = categoryPOS.stream().map(DokkanCategoryPO::getCategoryName).toList();
        }
        if (CollectionUtils.isNotEmpty(queryOption.getLinkIds())) {
            List<DokkanLinkPO> dokkanLinkPOS = linkRepository.queryLinksByIds(queryOption.getLinkIds());
            links = dokkanLinkPOS.stream().map(DokkanLinkPO::getLinkName).toList();
        }
        List<CardEsPO> cardEsPOS = dokkanEsCardRepository.queryCardList(buildCondition(queryOption, categories, links));
        return null;
    }

    private CardQueryCondition buildCondition(CardQueryOption queryOption, List<String> categories, List<String> links) {
        CardQueryCondition cardQueryCondition = cardConvert.convertToEsQueryCondition(queryOption);
        if (StringUtils.isNotBlank(cardQueryCondition.getPropType())) {
            cardQueryCondition.setType(null);
        }
        cardQueryCondition.setCategories(categories);
        cardQueryCondition.setLinks(links);
        return cardQueryCondition;
    }
}
