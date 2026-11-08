package com.hb.dokkan.service.convert;

import com.hb.dokkan.common.domain.po.es.cards.CardEsSpecialDTO;
import com.hb.dokkan.common.domain.po.mysql.cards.CardSkillPO;
import org.junit.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.Assert.*;

/**
 * 验证新技能表映射至旧 ES/详情响应结构时不丢失展示字段。
 **/
public class KxWikiEsConvertTest {
    /**
     * 必杀等级、加成等级和攻击分类通过兼容 DTO 原样传到 API 视图。
     **/
    @Test
    public void shouldPreserveSuperAttackFieldsThroughApiMapping() {
        CardSkillPO skill = new CardSkillPO();
        skill.setSkillId(5546L);
        skill.setName("必杀");
        skill.setEffectDesc("效果");
        skill.setKi(12);
        skill.setIncreaseRate(150);
        skill.setAttackCategoryId(1);
        skill.setAttributes("{\"saLevel\":1,\"bonuses\":[{\"name\":\"威力提升30%\",\"super_attack_level\":20}]}");
        CardEsSpecialDTO special = Mappers.getMapper(KxWikiEsConvert.class).skillToSpecial(skill);
        assertEquals(Integer.valueOf(1), special.getLvStart());
        assertEquals(Integer.valueOf(20), special.getSpecialBonus1Lv());
        assertEquals("威力提升30%", special.getSpecialBonus1());
        assertEquals(Long.valueOf(1), special.getSpecialCategoryId());
        assertEquals(Integer.valueOf(12), special.getEballNumStart());
        var document = new com.hb.dokkan.common.domain.po.es.cards.CardEsPO();
        document.setSpecialSkills(java.util.List.of(special));
        var response = Mappers.getMapper(DokkanCardConvert.class).convertToCardDetailVO(document);
        assertEquals(special.getDescription(), response.getSpecialSkills().getFirst().getDescription());
        assertEquals(special.getSpecialBonus1Lv(), response.getSpecialSkills().getFirst().getSpecialBonus1Lv());
    }
}
