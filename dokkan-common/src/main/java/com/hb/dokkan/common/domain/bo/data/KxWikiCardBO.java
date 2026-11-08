package com.hb.dokkan.common.domain.bo.data;

import com.hb.dokkan.common.domain.po.mysql.cards.*;
import com.hb.dokkan.common.domain.po.mysql.link.DokkanLinkPO;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description 本体卡及形态卡的完整事务聚合
 * @Author Codex
 * @Date 2026/11/8
 **/
@Data
public class KxWikiCardBO {
    /**
     * 卡片主记录。
     **/
    private List<CardPO> cards = new ArrayList<>();
    /**
     * 全部状态。
     **/
    private List<CardStatePO> states = new ArrayList<>();
    /**
     * 全部技能。
     **/
    private List<CardSkillPO> skills = new ArrayList<>();
    /**
     * 分类关系。
     **/
    private List<CardCategoryRelPO> categories = new ArrayList<>();
    /**
     * 链接关系。
     **/
    private List<CardLinkRelPO> links = new ArrayList<>();
    /**
     * 卡片详情提供的完整链接信息。
     **/
    private List<DokkanLinkPO> linkDefinitions = new ArrayList<>();
}
