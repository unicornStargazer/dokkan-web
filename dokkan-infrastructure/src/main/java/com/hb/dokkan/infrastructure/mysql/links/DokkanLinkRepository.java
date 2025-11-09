package com.hb.dokkan.infrastructure.mysql.links;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hb.dokkan.infrastructure.mysql.links.domain.DokkanLinkPO;
import com.hb.dokkan.infrastructure.mysql.links.mapper.DokkanLinkMapper;
import org.springframework.stereotype.Repository;

/**
 * @Description 链接数据库操作类
 * @Author stargazer
 * @Date 2025/11/8 22:12
 **/
@Repository
public class DokkanLinkRepository extends ServiceImpl<DokkanLinkMapper, DokkanLinkPO> {


}
