package com.hb.dokkan.service.cards;

import cn.hutool.core.util.NumberUtil;
import com.google.common.collect.Lists;
import com.hb.dokkan.common.domain.dto.cards.CardQueryConditionDTO;
import com.hb.dokkan.common.domain.request.cards.CardTagBatchRequest;
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

    /**
     * 卡片 ES 查询与文档写入仓储。
     **/
    @Resource
    private DokkanEsCardRepository dokkanEsCardRepository;

    /**
     * 分类字典访问仓储。
     **/
    @Resource
    private DokkanCategoryRepository categoryRepository;

    /**
     * 链接技能字典访问仓储。
     **/
    @Resource
    private DokkanLinkRepository linkRepository;

    /**
     * 卡片查询参数及展示视图转换器。
     **/
    @Resource
    private DokkanCardConvert cardConvert;

    /**
     * KX Wiki 与 MySQL、ES 的同步编排服务。
     **/
    @Resource
    private SyncDataService syncDataService;


    /**
     * 查询卡片列表
     *
     **/
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

    /**
     * 合并分类/链接名称及稀有度，保留新标签精确筛选条件。
     **/
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
     * 查询卡片详情
     **/
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
    /**
     * 执行人工批量标签更新，提交 MySQL 后刷新对应 ES。
     **/
    public int batchUpdateTags(CardTagBatchRequest request) {
        return syncDataService.batchUpdateTags(request);
    }
}
