package com.hb.dokkan.infrastructure.mysql.cards.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hb.dokkan.common.domain.po.mysql.cards.CardSkillPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * CardSkill MyBatis-Plus Mapper。
 **/
@Mapper
public interface CardSkillMapper extends BaseMapper<CardSkillPO> {
}
