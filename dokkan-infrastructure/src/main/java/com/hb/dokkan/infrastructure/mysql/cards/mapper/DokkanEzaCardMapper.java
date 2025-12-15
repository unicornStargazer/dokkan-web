package com.hb.dokkan.infrastructure.mysql.cards.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hb.dokkan.common.domain.po.mysql.cards.EzaCardPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description DokkanEzaCardMapper
 * @Author stargazer
 * @Date 2025/3/9 19:36
 **/
@Mapper
public interface DokkanEzaCardMapper extends BaseMapper<EzaCardPO> {
}
