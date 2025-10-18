package com.hb.dokkan.infrastructure.mysql.cards;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hb.dokkan.infrastructure.mysql.cards.domain.SkillPO;
import com.hb.dokkan.infrastructure.mysql.cards.mapper.DokkanSkillMapper;
import org.springframework.stereotype.Repository;

/**
 * @Description 技能数据库层
 * @Author stargazer
 * @Date 2025/3/9 19:35
 **/
@Repository
public class DokkanSkillRepository extends ServiceImpl<DokkanSkillMapper, SkillPO> {

}
