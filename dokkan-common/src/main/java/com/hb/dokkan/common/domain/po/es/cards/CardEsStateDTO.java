package com.hb.dokkan.common.domain.po.es.cards;

import lombok.Data;
import org.dromara.easyes.annotation.IndexField;
import org.dromara.easyes.annotation.rely.FieldType;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Description 卡片单个觉醒状态的完整展示数据，供详情页独立切换。
 * @Author Codex
 * @Date 2026/11/8
 **/
@Data
public class CardEsStateDTO implements Serializable {
    /**
     * 序列化版本标识。
     **/
    private static final long serialVersionUID = 1L;
    /**
     * 觉醒状态键：base、eza 或 seza。
     **/
    private String stateKey;
    /**
     * 当前状态的角色等级上限。
     **/
    private Integer lvMax;
    /**
     * 当前状态的必杀等级上限。
     **/
    private Integer saLvMax;
    /**
     * 当前状态的满级生命值，不含潜力加成。
     **/
    private Long hpValue;
    /**
     * 当前状态的满级攻击值，不含潜力加成。
     **/
    private Long atkValue;
    /**
     * 当前状态的满级防御值，不含潜力加成。
     **/
    private Long defValue;
    /**
     * 当前状态的队长技名称。
     **/
    private String leaderSkillName;
    /**
     * 当前状态的队长技描述，不从其他状态覆盖。
     **/
    private String leaderSkill;
    /**
     * 当前状态的被动名称。
     **/
    private String passiveSkillName;
    /**
     * 当前状态的被动描述。
     **/
    private String passiveSkill;
    /**
     * 当前状态被动关联的领域，保留其依赖关系。
     **/
    @IndexField(fieldType = FieldType.NESTED, nestedOrObjectClass = CardEsDomainDTO.class)
    private List<CardEsDomainDTO> passiveDomains;
    /**
     * 当前状态的开放时间，源站未提供时为空。
     **/
    private Date publishTime;
    /**
     * 当前状态的必杀技，按源站展示顺序排列。
     **/
    @IndexField(fieldType = FieldType.NESTED, nestedOrObjectClass = CardEsSpecialDTO.class)
    private List<CardEsSpecialDTO> specialSkills;
    /**
     * 当前状态的主动技，保留名称、条件及效果。
     **/
    @IndexField(fieldType = FieldType.NESTED, nestedOrObjectClass = CardEsSkillDTO.class)
    private List<CardEsSkillDTO> activeSkills;
    /**
     * 当前状态的待机技，未配置时为空列表。
     **/
    @IndexField(fieldType = FieldType.NESTED, nestedOrObjectClass = CardEsSkillDTO.class)
    private List<CardEsSkillDTO> standBySkills;
    /**
     * 当前状态的终结技，按源站展示顺序排列。
     **/
    @IndexField(fieldType = FieldType.NESTED, nestedOrObjectClass = CardEsSkillDTO.class)
    private List<CardEsSkillDTO> finishSkills;
}
