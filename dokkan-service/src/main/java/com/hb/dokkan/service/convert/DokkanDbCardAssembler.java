package com.hb.dokkan.service.convert;

import com.hb.dokkan.common.domain.dto.data.cards.PotentialDTO;
import com.hb.dokkan.common.domain.dto.data.dokkandb.DokkanDbCardDTO;
import com.hb.dokkan.common.domain.dto.data.dokkandb.DokkanDbCardStatsDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiCardBaseInfoDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiCardCategoryDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiCardDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiCardLinkDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiCardTransformationDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiEzaCardDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiSkillDTO;
import com.hb.dokkan.common.domain.dto.data.wiki.WikiSpecialAttackDTO;
import com.hb.dokkan.service.translation.DokkanTranslationService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/** Adapts DokkanDB responses to the existing wiki-shaped persistence contract. */
@Service
public class DokkanDbCardAssembler {
    @Resource
    private DokkanTranslationService translationService;

    public WikiCardDTO assemble(DokkanDbCardDTO source, DokkanDbCardStatsDTO stats) {
        Function<String, String> translate = translatorFor(source);
        WikiCardBaseInfoDTO card = new WikiCardBaseInfoDTO();
        card.setId(source.getId());
        card.setName(translate.apply(source.getName()));
        card.setTitle(translate.apply(source.getTitle()));
        card.setRarity(source.getRarity());
        card.setPropType(source.getElement());
        card.setCost(estimateCost(source));
        card.setOpenAt(parseDate(source.getOpenAt()));
        card.setHpValue(stats == null ? null : stats.getHpMax());
        card.setAtkValue(stats == null ? null : stats.getAtkMax());
        card.setDefValue(stats == null ? null : stats.getDefMax());
        card.setLeaderSkillSetId(source.getLeaderSkillSetId());
        card.setLeaderSkill(translate.apply(source.getLeaderSkill()));
        card.setPassiveSkillSetId(source.getPassiveSkillSetId());
        card.setPassiveSkillName(translate.apply(source.getPassiveSkillName()));
        card.setPassiveSkillDesc(translate.apply(source.getPassiveSkillDescription()));
        card.setActiveSkillId(source.getActiveSkillSetId());
        card.setActiveSkillName(translate.apply(source.getActiveSkillName()));
        card.setActiveSkillEffect(translate.apply(source.getActiveSkillEffect()));
        card.setActiveSkillCondition(translate.apply(source.getActiveSkillCondition()));
        card.setSkillLevelMax(source.getSkillLvMax());
        card.setDokkanFesFlag(hasTag(source, "Dokkan Festival"));
        card.setCarnivalFlag(hasTag(source, "Carnival"));
        card.setFreeCardFlag(isFreeCard(source));

        return WikiCardDTO.builder()
                .card(card)
                .potential(buildPotential(stats, false))
                .categories(safe(source.getCategoryIds()).stream().map(this::category).toList())
                .cardLinks(safe(source.getLinkIds()).stream().map(this::link).toList())
                .specials(buildSpecials(source, translate))
                .standbySkills(buildStandbySkills(source, translate))
                .finishSkills(buildFinishSkills(source, translate))
                .transformations(buildTransformations(source, translate))
                .ezaCardInfos(buildEza(source, stats, translate))
                .awakeningRoutes(Collections.emptyList())
                .build();
    }

    private Function<String, String> translatorFor(DokkanDbCardDTO card) {
        List<String> texts = new ArrayList<>();
        add(texts, card.getName(), card.getTitle(), card.getLeaderSkill(), card.getPassiveSkillName(),
                card.getPassiveSkillDescription(), card.getActiveSkillName(), card.getActiveSkillEffect(),
                card.getActiveSkillCondition(), card.getStandbySkillName(), card.getStandbySkillEffect(),
                card.getStandbySkillCondition(), card.getEzaLeaderSkill(), card.getEzaPassiveSkillName(),
                card.getEzaPassiveSkillDescription(), card.getEzaLeaderSkillPre(),
                card.getEzaPassiveSkillNamePre(), card.getEzaPassiveSkillDescriptionPre());
        addAll(texts, card.getSpecialNames(), card.getSpecialDescriptions(), card.getFinishSkillNames(),
                card.getFinishSkillEffects(), card.getFinishSkillConditions(), card.getTransformationNames());
        List<String> translated = translationService.translateAll(texts);
        Map<String, String> values = new LinkedHashMap<>();
        for (int i = 0; i < texts.size(); i++) values.put(texts.get(i), translated.get(i));
        return value -> StringUtils.isBlank(value) ? value : values.getOrDefault(value, value);
    }

    private List<WikiSpecialAttackDTO> buildSpecials(DokkanDbCardDTO card, Function<String, String> translate) {
        List<WikiSpecialAttackDTO> result = new ArrayList<>();
        for (int i = 0; i < safe(card.getSpecialIds()).size(); i++) {
            result.add(WikiSpecialAttackDTO.builder()
                    .id(at(card.getSpecialIds(), i))
                    .name(translate.apply(at(card.getSpecialNames(), i)))
                    .description(translate.apply(at(card.getSpecialDescriptions(), i)))
                    .increaseRate(at(card.getSpecialIncreaseRates(), i))
                    .lvBonus(valueOrZero(at(card.getSpecialLvBonuses(), i)))
                    .style(at(card.getSpecialStyles(), i))
                    .lvStart(1)
                    .eballNumStart(at(card.getSpecialKis(), i))
                    .build());
        }
        return result;
    }

    private List<WikiSkillDTO> buildStandbySkills(DokkanDbCardDTO card, Function<String, String> translate) {
        if (card.getStandbySkillSetId() == null) return Collections.emptyList();
        return List.of(WikiSkillDTO.builder()
                .id(card.getStandbySkillSetId())
                .name(translate.apply(card.getStandbySkillName()))
                .conditionDescription(translate.apply(card.getStandbySkillCondition()))
                .effectDescription(translate.apply(card.getStandbySkillEffect()))
                .label("standby")
                .build());
    }

    private List<WikiSkillDTO> buildFinishSkills(DokkanDbCardDTO card, Function<String, String> translate) {
        List<WikiSkillDTO> result = new ArrayList<>();
        for (int i = 0; i < safe(card.getFinishSkillSetIds()).size(); i++) {
            result.add(WikiSkillDTO.builder()
                    .id(at(card.getFinishSkillSetIds(), i))
                    .name(translate.apply(at(card.getFinishSkillNames(), i)))
                    .conditionDescription(translate.apply(at(card.getFinishSkillConditions(), i)))
                    .effectDescription(translate.apply(at(card.getFinishSkillEffects(), i)))
                    .increaseRate(at(card.getFinishSkillIncreaseRates(), i))
                    .label("finish")
                    .build());
        }
        return result;
    }

    private List<WikiCardTransformationDTO> buildTransformations(DokkanDbCardDTO card,
                                                                   Function<String, String> translate) {
        List<WikiCardTransformationDTO> result = new ArrayList<>();
        for (int i = 0; i < safe(card.getTransformationIds()).size(); i++) {
            Long nextId = at(card.getTransformationIds(), i);
            WikiCardTransformationDTO.NextCardDTO next = WikiCardTransformationDTO.NextCardDTO.builder()
                    .id(nextId)
                    .name(translate.apply(at(card.getTransformationNames(), i)))
                    .type(103)
                    .build();
            result.add(WikiCardTransformationDTO.builder()
                    .startCardId(card.getId())
                    .nextCardId(nextId)
                    .nextCard(next)
                    .build());
        }
        return result;
    }

    private List<WikiEzaCardDTO> buildEza(DokkanDbCardDTO card, DokkanDbCardStatsDTO stats,
                                           Function<String, String> translate) {
        if (card.getStep() == null || card.getStep() <= 0) return Collections.emptyList();
        List<WikiEzaCardDTO> result = new ArrayList<>();
        if (card.getStep() >= 8 && card.getStepPre() != null) {
            result.add(eza(card, stats, translate, card.getStepPre(), card.getEzaLeaderSkillSetIdPre(),
                    card.getEzaLeaderSkillPre(), card.getEzaPassiveSkillSetIdPre(),
                    card.getEzaPassiveSkillNamePre(), card.getEzaPassiveSkillDescriptionPre()));
        }
        result.add(eza(card, stats, translate, card.getStep(), card.getEzaLeaderSkillSetId(),
                card.getEzaLeaderSkill(), card.getEzaPassiveSkillSetId(),
                card.getEzaPassiveSkillName(), card.getEzaPassiveSkillDescription()));
        return result;
    }

    private WikiEzaCardDTO eza(DokkanDbCardDTO card, DokkanDbCardStatsDTO stats, Function<String, String> translate,
                                Integer step, Integer leaderId, String leader, Integer passiveId,
                                String passiveName, String passiveDescription) {
        WikiEzaCardDTO result = new WikiEzaCardDTO();
        result.setId(card.getId());
        result.setStep(step);
        result.setLvMax(stats != null && stats.getEzaLvMax() != null ? stats.getEzaLvMax() : stats == null ? null : stats.getBaseLvMax());
        result.setName(translate.apply(card.getName()));
        result.setRarity(card.getRarity());
        result.setPropType(card.getElement());
        result.setCost(estimateCost(card));
        result.setHpValue(ezaStat(stats == null ? null : stats.getHpMaxEza(), stats == null ? null : stats.getHpMax()));
        result.setAtkValue(ezaStat(stats == null ? null : stats.getAtkMaxEza(), stats == null ? null : stats.getAtkMax()));
        result.setDefValue(ezaStat(stats == null ? null : stats.getDefMaxEza(), stats == null ? null : stats.getDefMax()));
        result.setOpenAt(latestEzaDate(card));
        result.setTitle(translate.apply(card.getTitle()));
        result.setLeaderSkillId(leaderId);
        result.setLeaderSkill(translate.apply(leader));
        result.setPassiveSkillId(passiveId);
        result.setPassiveSkillName(translate.apply(passiveName));
        result.setPassiveSkillDesc(translate.apply(passiveDescription));
        result.setDokkanFesFlag(hasTag(card, "Dokkan Festival"));
        result.setCarnivalFlag(hasTag(card, "Carnival"));
        result.setFreeCardFlag(isFreeCard(card));
        return result;
    }

    private List<PotentialDTO> buildPotential(DokkanDbCardStatsDTO stats, boolean eza) {
        if (stats == null) return Collections.emptyList();
        Map<String, Long> hp = eza ? stats.getHpTableEza() : stats.getHpTableMax();
        Map<String, Long> atk = eza ? stats.getAtkTableEza() : stats.getAtkTableMax();
        Map<String, Long> def = eza ? stats.getDefTableEza() : stats.getDefTableMax();
        if (CollectionUtils.isEmpty(hp)) return Collections.emptyList();
        return hp.keySet().stream().sorted((a, b) -> Integer.compare(Integer.parseInt(a), Integer.parseInt(b)))
                .map(order -> PotentialDTO.builder().order(Integer.parseInt(order)).hp(hp.get(order))
                        .atk(atk == null ? null : atk.get(order)).def(def == null ? null : def.get(order)).build())
                .toList();
    }

    private WikiCardCategoryDTO category(Long id) {
        WikiCardCategoryDTO result = new WikiCardCategoryDTO();
        result.setId(id);
        return result;
    }

    private WikiCardLinkDTO link(Long id) {
        WikiCardLinkDTO result = new WikiCardLinkDTO();
        result.setId(id);
        return result;
    }

    private boolean isFreeCard(DokkanDbCardDTO card) {
        return !hasTag(card, "Summonable") && !hasTag(card, "Dokkan Festival") && !hasTag(card, "Carnival");
    }

    /** DokkanDB does not expose cost; use game-standard defaults for newly discovered cards. */
    private int estimateCost(DokkanDbCardDTO card) {
        if (card.getRarity() != null && card.getRarity() >= 5) return 77;
        if (hasTag(card, "Dokkan Festival")) return 58;
        if (hasTag(card, "Summonable")) return 42;
        return 32;
    }

    private boolean hasTag(DokkanDbCardDTO card, String value) {
        return StringUtils.containsIgnoreCase(card.getTag(), value);
    }

    private Date latestEzaDate(DokkanDbCardDTO card) {
        return safe(card.getAwakeningData()).stream().map(DokkanDbCardDTO.AwakeningDTO::getOpenAtEza)
                .filter(StringUtils::isNotBlank).map(this::parseDate).filter(java.util.Objects::nonNull)
                .max(Date::compareTo).orElse(parseDate(card.getOpenAtUpdate()));
    }

    private Date parseDate(String value) {
        if (StringUtils.isBlank(value)) return null;
        try {
            return Date.from(OffsetDateTime.parse(value).toInstant());
        } catch (Exception ignored) {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                return Date.from(dateTime.atZone(ZoneId.of("Asia/Tokyo")).toInstant());
            } catch (Exception ignoredAgain) {
                return null;
            }
        }
    }

    @SafeVarargs
    private final void addAll(List<String> target, List<String>... values) {
        for (List<String> value : values) if (value != null) value.stream().filter(StringUtils::isNotBlank).forEach(target::add);
    }

    private void add(List<String> target, String... values) {
        for (String value : values) if (StringUtils.isNotBlank(value)) target.add(value);
    }

    private <T> List<T> safe(List<T> values) {
        return values == null ? Collections.emptyList() : values;
    }

    private <T> T at(List<T> values, int index) {
        return values == null || index < 0 || index >= values.size() ? null : values.get(index);
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private Long ezaStat(Long eza, Long base) {
        return eza == null ? base : eza;
    }
}
