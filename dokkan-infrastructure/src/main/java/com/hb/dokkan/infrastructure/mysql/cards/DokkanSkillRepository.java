package com.hb.dokkan.infrastructure.mysql.cards;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.hb.dokkan.common.domain.po.mysql.cards.SkillPO;
import com.hb.dokkan.infrastructure.mysql.cards.mapper.DokkanSkillMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Description 技能数据库层
 * @Author stargazer
 * @Date 2025/3/9 19:35
 **/
@Repository
public class DokkanSkillRepository extends ServiceImpl<DokkanSkillMapper, SkillPO> {

    public List<SkillPO> batchQueryBySkillIds(List<String> skillIds) {
        if (CollectionUtils.isEmpty(skillIds)) {
            return Lists.newArrayList();
        }
        return this.lambdaQuery().in(SkillPO::getSkillId, skillIds).list();
    }
}
