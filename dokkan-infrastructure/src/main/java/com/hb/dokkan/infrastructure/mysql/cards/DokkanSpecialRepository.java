package com.hb.dokkan.infrastructure.mysql.cards;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.hb.dokkan.common.domain.po.mysql.cards.SpecialPO;
import com.hb.dokkan.infrastructure.mysql.cards.mapper.DokkanSpecialMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Description 必杀数据库层
 * @Author stargazer
 * @Date 2025/3/9 19:35
 **/
@Repository
public class DokkanSpecialRepository extends ServiceImpl<DokkanSpecialMapper, SpecialPO> {

    public List<SpecialPO> batchQueryBySpecialIds(List<Long> specialIds) {
        if (CollectionUtils.isEmpty(specialIds)) {
            return Lists.newArrayList();
        }
        return this.lambdaQuery().in(SpecialPO::getSpecialId, specialIds).list();
    }
}
