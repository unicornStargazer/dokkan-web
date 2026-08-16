package com.hb.dokkan.service.job.sync.strategy.strategies;

import com.hb.dokkan.common.constants.CardSyncConstants;
import com.hb.dokkan.common.constants.TranslationMappingConstants;
import com.hb.dokkan.common.domain.po.mysql.cards.CardPO;
import com.hb.dokkan.common.domain.po.mysql.cards.EzaCardPO;
import com.hb.dokkan.common.domain.po.mysql.cards.SkillPO;
import com.hb.dokkan.common.domain.po.mysql.cards.SpecialPO;
import com.hb.dokkan.common.enums.FixDataTypeEnum;
import com.hb.dokkan.common.utils.CollectionUtils;
import com.hb.dokkan.common.utils.JsonUtils;
import com.hb.dokkan.infrastructure.mysql.cards.DokkanCardRepository;
import com.hb.dokkan.infrastructure.mysql.cards.DokkanEzaCardRepository;
import com.hb.dokkan.infrastructure.mysql.cards.DokkanSkillRepository;
import com.hb.dokkan.infrastructure.mysql.cards.DokkanSpecialRepository;
import com.hb.dokkan.service.job.sync.SyncDataService;
import com.hb.dokkan.service.job.sync.strategy.FixDataStrategy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Description 修复存量数据中的角色名错误翻译
 * @Author stargazer
 * @Date 2026/8/16 00:00
 **/
@Component
@Slf4j
public class FixNameTranslationErrorStrategy implements FixDataStrategy {

    @Resource
    private DokkanCardRepository cardRepository;

    @Resource
    private DokkanEzaCardRepository ezaCardRepository;

    @Resource
    private DokkanSkillRepository skillRepository;

    @Resource
    private DokkanSpecialRepository specialRepository;

    @Resource
    private ResourceLoader resourceLoader;

    @Lazy
    @Resource
    private SyncDataService syncDataService;

    @Resource(name = "defaultTransactionTemplate")
    private TransactionTemplate transactionTemplate;

    @Value(TranslationMappingConstants.NAME_ERROR_FILE_PROPERTY)
    private String nameErrorFile;

    /**
     * 获取角色名错误翻译修复策略类型。
     *
     * @return 修复策略类型
     */
    @Override
    public FixDataTypeEnum getFixDataType() {
        return FixDataTypeEnum.FIX_NAME_TRANSLATION_ERROR;
    }

    /**
     * 按 name-error.json 映射修复 MySQL 存量角色名，并在成功修改后重建 ES。
     */
    @Override
    public void fixData() {
        List<Map.Entry<String, String>> mappings = loadNameErrorMappings();
        if (CollectionUtils.isEmpty(mappings)) {
            log.warn("name translation error mappings is empty, location={}", nameErrorFile);
            return;
        }

        // 先全量查询四张可能包含角色名文案的表，只收集确实发生变化的记录。
        List<CardPO> changedCards = fixCards(cardRepository.list(), mappings);
        List<EzaCardPO> changedEzaCards = fixEzaCards(ezaCardRepository.list(), mappings);
        List<SkillPO> changedSkills = fixSkills(skillRepository.list(), mappings);
        List<SpecialPO> changedSpecials = fixSpecials(specialRepository.list(), mappings);
        if (CollectionUtils.isEmpty(changedCards) && CollectionUtils.isEmpty(changedEzaCards)
                && CollectionUtils.isEmpty(changedSkills) && CollectionUtils.isEmpty(changedSpecials)) {
            log.info("name translation error fix skipped db update, no changed data");
            syncDataService.syncEsCardData();
            return;
        }

        // MySQL 批量更新必须在同一个事务内完成，避免多表文案修复状态不一致。
        Boolean fixed = transactionTemplate.execute(status -> {
            try {
                updateChangedData(changedCards, changedEzaCards, changedSkills, changedSpecials);
                return true;
            } catch (Exception e) {
                status.setRollbackOnly();
                log.error("FixNameTranslationErrorStrategy#fixData error :{}", e.getMessage(), e);
                return false;
            }
        });
        if (!Boolean.TRUE.equals(fixed)) {
            return;
        }

        log.info("name translation error fix completed, cards:{}, ezaCards:{}, skills:{}, specials:{}",
                changedCards.size(), changedEzaCards.size(), changedSkills.size(), changedSpecials.size());
        syncDataService.syncEsCardData();
    }

    /**
     * 读取角色名错误翻译映射，并按错误词长度倒序排序。
     *
     * @return 错误译名到正确译名的映射列表
     */
    private List<Map.Entry<String, String>> loadNameErrorMappings() {
        try (InputStream input = resourceLoader.getResource(nameErrorFile).getInputStream()) {
            Map<String, String> values = JsonUtils.inputStream2Map(input, String.class, String.class);
            return values.entrySet().stream()
                    .filter(entry -> StringUtils.isNoneBlank(entry.getKey(), entry.getValue()))
                    .sorted((left, right) -> Integer.compare(right.getKey().length(), left.getKey().length()))
                    .toList();
        } catch (Exception e) {
            log.warn("name translation error mapping load failed, location={}", nameErrorFile, e);
            return List.of();
        }
    }

    /**
     * 修复 card 表角色名相关字段。
     *
     * @param cards    card 表数据
     * @param mappings 错误译名映射
     * @return 发生变化的 card 数据
     */
    private List<CardPO> fixCards(List<CardPO> cards, List<Map.Entry<String, String>> mappings) {
        if (CollectionUtils.isEmpty(cards)) {
            return List.of();
        }
        return cards.stream()
                .filter(card -> fixCard(card, mappings))
                .toList();
    }

    /**
     * 修复单条 card 数据。
     *
     * @param card     card 数据
     * @param mappings 错误译名映射
     * @return 是否发生变化
     */
    private boolean fixCard(CardPO card, List<Map.Entry<String, String>> mappings) {
        if (Objects.isNull(card)) {
            return false;
        }
        boolean changed = false;
        String cardName = replaceNameErrors(card.getCardName(), mappings);
        if (!Objects.equals(card.getCardName(), cardName)) {
            card.setCardName(cardName);
            changed = true;
        }
        String title = replaceNameErrors(card.getTitle(), mappings);
        if (!Objects.equals(card.getTitle(), title)) {
            card.setTitle(title);
            changed = true;
        }
        String attributes = replaceNameErrors(card.getAttributes(), mappings);
        if (!Objects.equals(card.getAttributes(), attributes)) {
            card.setAttributes(attributes);
            changed = true;
        }
        return changed;
    }

    /**
     * 修复 eza_card 表角色名相关字段。
     *
     * @param ezaCards eza_card 表数据
     * @param mappings 错误译名映射
     * @return 发生变化的 eza_card 数据
     */
    private List<EzaCardPO> fixEzaCards(List<EzaCardPO> ezaCards, List<Map.Entry<String, String>> mappings) {
        if (CollectionUtils.isEmpty(ezaCards)) {
            return List.of();
        }
        return ezaCards.stream()
                .filter(ezaCard -> fixEzaCard(ezaCard, mappings))
                .toList();
    }

    /**
     * 修复单条 eza_card 数据。
     *
     * @param ezaCard  eza_card 数据
     * @param mappings 错误译名映射
     * @return 是否发生变化
     */
    private boolean fixEzaCard(EzaCardPO ezaCard, List<Map.Entry<String, String>> mappings) {
        if (Objects.isNull(ezaCard)) {
            return false;
        }
        boolean changed = fixCard(ezaCard, mappings);
        String leaderSkill = replaceNameErrors(ezaCard.getLeaderSkill(), mappings);
        if (!Objects.equals(ezaCard.getLeaderSkill(), leaderSkill)) {
            ezaCard.setLeaderSkill(leaderSkill);
            changed = true;
        }
        String passiveSkillDesc = replaceNameErrors(ezaCard.getPassiveSkillDesc(), mappings);
        if (!Objects.equals(ezaCard.getPassiveSkillDesc(), passiveSkillDesc)) {
            ezaCard.setPassiveSkillDesc(passiveSkillDesc);
            changed = true;
        }
        return changed;
    }

    /**
     * 修复 skill 表角色名相关字段。
     *
     * @param skills   skill 表数据
     * @param mappings 错误译名映射
     * @return 发生变化的 skill 数据
     */
    private List<SkillPO> fixSkills(List<SkillPO> skills, List<Map.Entry<String, String>> mappings) {
        if (CollectionUtils.isEmpty(skills)) {
            return List.of();
        }
        return skills.stream()
                .filter(skill -> fixSkill(skill, mappings))
                .toList();
    }

    /**
     * 修复单条 skill 数据。
     *
     * @param skill    skill 数据
     * @param mappings 错误译名映射
     * @return 是否发生变化
     */
    private boolean fixSkill(SkillPO skill, List<Map.Entry<String, String>> mappings) {
        if (Objects.isNull(skill)) {
            return false;
        }
        boolean changed = false;
        String name = replaceNameErrors(skill.getName(), mappings);
        if (!Objects.equals(skill.getName(), name)) {
            skill.setName(name);
            changed = true;
        }
        String conditionDescription = replaceNameErrors(skill.getConditionDescription(), mappings);
        if (!Objects.equals(skill.getConditionDescription(), conditionDescription)) {
            skill.setConditionDescription(conditionDescription);
            changed = true;
        }
        String effectDescription = replaceNameErrors(skill.getEffectDescription(), mappings);
        if (!Objects.equals(skill.getEffectDescription(), effectDescription)) {
            skill.setEffectDescription(effectDescription);
            changed = true;
        }
        String specialCategoryName = replaceNameErrors(skill.getSpecialCategoryName(), mappings);
        if (!Objects.equals(skill.getSpecialCategoryName(), specialCategoryName)) {
            skill.setSpecialCategoryName(specialCategoryName);
            changed = true;
        }
        String attributes = replaceNameErrors(skill.getAttributes(), mappings);
        if (!Objects.equals(skill.getAttributes(), attributes)) {
            skill.setAttributes(attributes);
            changed = true;
        }
        return changed;
    }

    /**
     * 修复 special 表角色名相关字段。
     *
     * @param specials special 表数据
     * @param mappings 错误译名映射
     * @return 发生变化的 special 数据
     */
    private List<SpecialPO> fixSpecials(List<SpecialPO> specials, List<Map.Entry<String, String>> mappings) {
        if (CollectionUtils.isEmpty(specials)) {
            return List.of();
        }
        return specials.stream()
                .filter(special -> fixSpecial(special, mappings))
                .toList();
    }

    /**
     * 修复单条 special 数据。
     *
     * @param special  special 数据
     * @param mappings 错误译名映射
     * @return 是否发生变化
     */
    private boolean fixSpecial(SpecialPO special, List<Map.Entry<String, String>> mappings) {
        if (Objects.isNull(special)) {
            return false;
        }
        boolean changed = false;
        String description = replaceNameErrors(special.getDescription(), mappings);
        if (!Objects.equals(special.getDescription(), description)) {
            special.setDescription(description);
            changed = true;
        }
        String specialCategoryName = replaceNameErrors(special.getSpecialCategoryName(), mappings);
        if (!Objects.equals(special.getSpecialCategoryName(), specialCategoryName)) {
            special.setSpecialCategoryName(specialCategoryName);
            changed = true;
        }
        String specialBonus1 = replaceNameErrors(special.getSpecialBonus1(), mappings);
        if (!Objects.equals(special.getSpecialBonus1(), specialBonus1)) {
            special.setSpecialBonus1(specialBonus1);
            changed = true;
        }
        String specialBonus2 = replaceNameErrors(special.getSpecialBonus2(), mappings);
        if (!Objects.equals(special.getSpecialBonus2(), specialBonus2)) {
            special.setSpecialBonus2(specialBonus2);
            changed = true;
        }
        String attributes = replaceNameErrors(special.getAttributes(), mappings);
        if (!Objects.equals(special.getAttributes(), attributes)) {
            special.setAttributes(attributes);
            changed = true;
        }
        return changed;
    }

    /**
     * 批量更新发生变化的存量数据。
     *
     * @param changedCards    发生变化的 card 数据
     * @param changedEzaCards 发生变化的 eza_card 数据
     * @param changedSkills   发生变化的 skill 数据
     * @param changedSpecials 发生变化的 special 数据
     */
    private void updateChangedData(List<CardPO> changedCards, List<EzaCardPO> changedEzaCards,
                                   List<SkillPO> changedSkills, List<SpecialPO> changedSpecials) {
        if (!CollectionUtils.isEmpty(changedCards)) {
            cardRepository.updateBatchById(changedCards, CardSyncConstants.CARD_SAVE_BATCH_SIZE);
        }
        if (!CollectionUtils.isEmpty(changedEzaCards)) {
            ezaCardRepository.updateBatchById(changedEzaCards, CardSyncConstants.CARD_SAVE_BATCH_SIZE);
        }
        if (!CollectionUtils.isEmpty(changedSkills)) {
            skillRepository.updateBatchById(changedSkills, CardSyncConstants.CARD_SAVE_BATCH_SIZE);
        }
        if (!CollectionUtils.isEmpty(changedSpecials)) {
            specialRepository.updateBatchById(changedSpecials, CardSyncConstants.SPECIAL_SAVE_BATCH_SIZE);
        }
    }

    /**
     * 按错误译名到正确译名映射替换文本内容。
     *
     * @param source   原始文本
     * @param mappings 错误译名映射
     * @return 替换后的文本
     */
    private String replaceNameErrors(String source, List<Map.Entry<String, String>> mappings) {
        if (StringUtils.isBlank(source) || CollectionUtils.isEmpty(mappings)) {
            return source;
        }
        String result = source;
        for (Map.Entry<String, String> entry : mappings) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
