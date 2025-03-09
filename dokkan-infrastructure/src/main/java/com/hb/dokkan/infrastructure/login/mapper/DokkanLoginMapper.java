package com.hb.dokkan.infrastructure.login.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hb.dokkan.infrastructure.login.domain.po.LoginPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description xxxxx
 * @Author stargazer
 * @Date 2025/3/9 19:36
 **/
@Mapper
public interface DokkanLoginMapper extends BaseMapper<LoginPO> {
}
