package com.hb.dokkan.infrastructure.mysql.cards.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hb.dokkan.common.domain.po.mysql.cards.SkillPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description DokkanSkillMapper
 * @Author stargazer
 * @Date 2025/3/9 19:36
 **/
@Mapper
public interface DokkanSkillMapper extends BaseMapper<SkillPO> {
}
