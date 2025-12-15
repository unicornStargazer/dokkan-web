package com.hb.dokkan.infrastructure.mysql.cards.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hb.dokkan.common.domain.po.mysql.cards.SpecialPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description DokkanSpecialMapper
 * @Author stargazer
 * @Date 2025/3/9 19:36
 **/
@Mapper
public interface DokkanSpecialMapper extends BaseMapper<SpecialPO> {
}
