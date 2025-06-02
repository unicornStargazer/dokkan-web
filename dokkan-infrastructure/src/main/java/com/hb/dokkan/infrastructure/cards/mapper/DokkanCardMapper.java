package com.hb.dokkan.infrastructure.cards.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hb.dokkan.infrastructure.cards.domain.CardPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description xxxxx
 * @Author stargazer
 * @Date 2025/3/9 19:36
 **/
@Mapper
public interface DokkanCardMapper extends BaseMapper<CardPO> {
}
