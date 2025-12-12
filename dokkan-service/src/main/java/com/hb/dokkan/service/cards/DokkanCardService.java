package com.hb.dokkan.service.cards;

import com.hb.dokkan.infrastructure.es.card.DokkanEsCardRepository;
import com.hb.dokkan.service.domain.cards.query.CardQueryOption;
import com.hb.dokkan.service.domain.cards.vo.CardListVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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


    /**
     * 查询卡片列表
     *
     */
    public List<CardListVO> cardList(CardQueryOption queryOption) {

        if (CollectionUtils.isEmpty(queryOption.getCategoryIds()) || CollectionUtils.isEmpty(queryOption.getLinkIds())) {
//            return dokkanEsCardRepository.cardList(queryOption);
            return null;
        }
        return null;     }
}
