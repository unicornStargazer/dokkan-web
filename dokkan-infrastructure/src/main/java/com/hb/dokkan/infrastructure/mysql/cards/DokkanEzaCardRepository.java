package com.hb.dokkan.infrastructure.mysql.cards;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hb.dokkan.common.domain.po.mysql.cards.EzaCardPO;
import com.hb.dokkan.infrastructure.mysql.cards.mapper.DokkanEzaCardMapper;
import org.springframework.stereotype.Repository;

/**
 * @Description 极限卡牌数据库层
 * @Author stargazer
 * @Date 2025/3/9 19:35
 **/
@Repository
public class DokkanEzaCardRepository extends ServiceImpl<DokkanEzaCardMapper, EzaCardPO> {

}
