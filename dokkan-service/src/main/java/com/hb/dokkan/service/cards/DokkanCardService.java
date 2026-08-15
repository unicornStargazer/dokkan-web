package com.hb.dokkan.service.cards;

import cn.hutool.core.util.NumberUtil;
import com.google.common.collect.Lists;
import com.hb.dokkan.common.domain.dto.cards.CardQueryConditionDTO;
import com.hb.dokkan.common.domain.dto.cards.CardQueryOptionDTO;
import com.hb.dokkan.common.domain.po.es.cards.CardEsPO;
import com.hb.dokkan.common.domain.po.mysql.category.DokkanCategoryPO;
import com.hb.dokkan.common.domain.po.mysql.link.DokkanLinkPO;
import com.hb.dokkan.common.domain.response.base.PageResponse;
import com.hb.dokkan.common.domain.vo.cards.CardDetailVO;
import com.hb.dokkan.common.domain.vo.cards.CardListVO;
import com.hb.dokkan.common.enums.CardRarityEnum;
import com.hb.dokkan.common.utils.CollectionUtils;
import com.hb.dokkan.common.utils.StringUtils;
import com.hb.dokkan.infrastructure.es.card.DokkanEsCardRepository;
import com.hb.dokkan.infrastructure.mysql.categories.DokkanCategoryRepository;
import com.hb.dokkan.infrastructure.mysql.links.DokkanLinkRepository;
import com.hb.dokkan.service.convert.DokkanCardConvert;
import com.hb.dokkan.service.job.sync.SyncDataService;
import jakarta.annotation.Resource;
import org.dromara.easyes.core.biz.EsPageInfo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

    @Resource
    private SyncDataService syncDataService;


    /**
     * 查询卡片列表
     *
     */
    public PageResponse<CardListVO> cardList(CardQueryOptionDTO queryOption) {
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
        Optional<EsPageInfo<CardEsPO>> optional = dokkanEsCardRepository.queryCardList(buildCondition(queryOption, categories, links));
        if (optional.isEmpty()) {
            return PageResponse.<CardListVO>builder()
                    .currentPage(queryOption.getPageNum())
                    .pageSize(queryOption.getPageSize())
                    .total(0)
                    .data(null)
                    .build();
        }
        EsPageInfo<CardEsPO> esPageInfo = optional.get();
        List<CardListVO> cardListVOS = cardConvert.convertToCardListVO(esPageInfo.getList());
        return PageResponse.<CardListVO>builder()
                .currentPage(esPageInfo.getPageNum())
                .pageSize(esPageInfo.getPageSize())
                .total(esPageInfo.getTotal())
                .data(cardListVOS)
                .build();
    }

    private CardQueryConditionDTO buildCondition(CardQueryOptionDTO queryOption, List<String> categories, List<String> links) {
        CardQueryConditionDTO cardQueryConditionDTO = cardConvert.convertToEsQueryCondition(queryOption);
        if (StringUtils.isNotBlank(cardQueryConditionDTO.getPropType())) {
            cardQueryConditionDTO.setType(null);
        }
        if (!NumberUtil.isNumber(cardQueryConditionDTO.getRarity())) {
            cardQueryConditionDTO.setRarity(CardRarityEnum.getEnumByRarity(cardQueryConditionDTO.getRarity()));
        }
        cardQueryConditionDTO.setCategories(categories);
        cardQueryConditionDTO.setLinks(links);
        return cardQueryConditionDTO;
    }

    /**
     * 重新翻译单张卡片，并返回最新卡片详情。
     *
     * @param cardId 卡片 ID
     * @return 最新卡片详情
     */
    public CardDetailVO retranslateCard(Long cardId) {
        syncDataService.retranslateCardById(cardId);
        CardQueryOptionDTO queryOption = new CardQueryOptionDTO();
        queryOption.setCardId(cardId);
        return cardDetail(queryOption);
    }

    /**
     * 查询卡片详情
     */
    public CardDetailVO cardDetail(CardQueryOptionDTO queryOption) {
        CardQueryConditionDTO cardQueryConditionDTO = cardConvert.convertToEsQueryCondition(queryOption);
        if (Objects.isNull(cardQueryConditionDTO)) {
            return null;
        }
        Optional<CardEsPO> optional = dokkanEsCardRepository.queryCardDetail(cardQueryConditionDTO);
        if (optional.isEmpty()) {
            return null;
        }
        CardEsPO cardEsPO = optional.get();
        return cardConvert.convertToCardDetailVO(cardEsPO);
    }
}
