package com.hb.dokkan.infrastructure.login;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hb.dokkan.infrastructure.login.domain.po.LoginPO;
import com.hb.dokkan.infrastructure.login.mapper.DokkanLoginMapper;
import org.springframework.stereotype.Repository;

/**
 * @Description xxxxx
 * @Author stargazer
 * @Date 2025/3/9 19:35
 **/
@Repository
public class DokkanLoginRepository extends ServiceImpl<DokkanLoginMapper, LoginPO> {
}
