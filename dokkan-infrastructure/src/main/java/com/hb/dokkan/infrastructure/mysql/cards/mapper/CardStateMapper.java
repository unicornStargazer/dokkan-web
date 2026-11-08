package com.hb.dokkan.infrastructure.mysql.cards.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hb.dokkan.common.domain.po.mysql.cards.CardStatePO;
import org.apache.ibatis.annotations.Mapper;

/**
 * CardState MyBatis-Plus Mapper。
 **/
@Mapper
public interface CardStateMapper extends BaseMapper<CardStatePO> {
}
