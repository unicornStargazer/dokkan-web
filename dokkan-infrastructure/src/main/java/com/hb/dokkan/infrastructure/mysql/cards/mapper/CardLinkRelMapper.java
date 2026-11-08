package com.hb.dokkan.infrastructure.mysql.cards.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hb.dokkan.common.domain.po.mysql.cards.CardLinkRelPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * CardLinkRel MyBatis-Plus Mapper。
 **/
@Mapper
public interface CardLinkRelMapper extends BaseMapper<CardLinkRelPO> {
}
