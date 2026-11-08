package com.hb.dokkan.infrastructure.mysql.cards.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hb.dokkan.common.domain.po.mysql.cards.CardCategoryRelPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * CardCategoryRel MyBatis-Plus Mapper。
 **/
@Mapper
public interface CardCategoryRelMapper extends BaseMapper<CardCategoryRelPO> {
}
