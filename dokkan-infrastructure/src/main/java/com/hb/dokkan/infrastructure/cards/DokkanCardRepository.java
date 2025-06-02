package com.hb.dokkan.infrastructure.cards;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hb.dokkan.infrastructure.cards.domain.CardPO;
import com.hb.dokkan.infrastructure.cards.mapper.DokkanCardMapper;
import org.springframework.stereotype.Repository;

/**
 * @Description xxxxx
 * @Author stargazer
 * @Date 2025/3/9 19:35
 **/
@Repository
public class DokkanCardRepository extends ServiceImpl<DokkanCardMapper, CardPO> {
}
