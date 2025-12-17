package com.hb.dokkan.common.domain.response.cards;

import com.hb.dokkan.common.domain.po.es.cards.CardEsPotentialDTO;
import com.hb.dokkan.common.domain.po.mysql.cards.SkillPO;
import com.hb.dokkan.common.domain.po.mysql.cards.SpecialPO;
import com.hb.dokkan.common.domain.vo.skill.SkillVO;
import com.hb.dokkan.common.domain.vo.special.SpecialVO;
import lombok.Data;
import org.dromara.easyes.annotation.IndexField;
import org.dromara.easyes.annotation.rely.FieldType;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Description 卡片详情响应
 * @Author stargazer
 * @Date 2025/12/17 21:44
 **/
@Data
public class CardDetailResponse implements Serializable {
    private static final long serialVersionUID = 6952311639863290359L;
    /**
     * 卡片id
     */
    private Long cardId;
    /**
     * 卡片name
     */
    private String cardName;
    /**
     * 卡片描述
     */
    private String title;
    /**
     * 属性（超 极）
     */
    private Integer type;
    /**
     *  具体属性
     */
    private String propType;
    /**
     * cost
     */
    private Integer cost;
    /**
     * 稀有度
     */
    private Integer rarity;
    /**
     * 生命值
     */
    private Long hpValue;
    /**
     * 防御值
     */
    private Long defValue;
    /**
     * 攻击值
     */
    private Long atkValue;
    /**
     * 发布时间
     */
    private Date publishTime;

    /**
     * 队长技
     */
    private String leaderSkill;

    /**
     * 被动
     */
    private String passiveSkill;

    /**
     * 主动技效果
     */
    private String activeSkill;

    /**
     * 免费标志
     */
    private Boolean freeCardFlag;
    /**
     * 限定标志
     */
    private Boolean dokkanFesFlag;
    /**
     * 祭限定
     */
    private Boolean carnivalFlag;

    /**
     * 极限标志
     */
    private Boolean ezaFlag;

    /**
     * 超极限标志
     */
    private Boolean superEzaFlag;


    /**
     * 分类 逗号分割
     */
    @IndexField(fieldType = FieldType.KEYWORD_TEXT,analyzer = "comma_analyzer", searchAnalyzer = "comma_analyzer")
    private String categories;

    /**
     * 链接
     */
    @IndexField(fieldType = FieldType.KEYWORD_TEXT,analyzer = "comma_analyzer", searchAnalyzer = "comma_analyzer")
    private String links;

    /**
     * 三围潜力值
     */
    @IndexField(fieldType = FieldType.NESTED, nestedOrObjectClass = CardEsPotentialDTO.class)
    private List<CardEsPotentialDTO> potentials;

    /**
     * 变身后cardId
     */
    private Long nextCardId;


    /******************* 基础信息-end *******************/



    /******************* 极限信息-start *******************/
    /**
     * 极限生命值
     */
    private Long ezaHpValue;
    /**
     * 极限攻击值
     */
    private Long ezaAtkValue;
    /**
     * 极限防御值
     */
    private Long ezaDefValue;

    /**
     * 极限发布时间
     */
    private Date ezaPublishTime;

    /**
     * 超极限发布时间
     */
    private Date superEzaPublishTime;

    /**
     * 极限队长技
     */
    private String ezaLeaderSkill;

    /**
     * 极限被动技描述
     */
    private String ezaPassiveSkill;

    /**
     * 超极限被动技
     */
    private String superEzaPassiveSkill;


    /******************* 极限信息-end *******************/


    /******************* 必杀信息-start *******************/

    /**
     * 必杀信息
     */
    @IndexField(fieldType = FieldType.NESTED, nestedOrObjectClass = SpecialPO.class)
    private List<SpecialVO> specialSkills;


    /******************* 必杀信息-end *******************/

    /******************* 下拉/待机技能-start *******************/

    /**
     * 待机技能
     */
    @IndexField(fieldType = FieldType.NESTED, nestedOrObjectClass = SkillPO.class)
    private List<SkillVO> standBySkills;

    /**
     * 待机完成技能
     */
    @IndexField(fieldType = FieldType.NESTED, nestedOrObjectClass = SpecialPO.class)
    private List<SkillVO> finishSkills;
}
