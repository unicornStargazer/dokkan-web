package com.hb.dokkan.service.helper;

import com.hb.dokkan.common.constants.CardSyncConstants;
import com.hb.dokkan.common.constants.EsCardAttributeKey;
import com.hb.dokkan.common.domain.po.es.cards.*;
import com.hb.dokkan.common.domain.po.mysql.cards.*;
import com.hb.dokkan.common.domain.po.mysql.link.DokkanLinkPO;
import com.hb.dokkan.common.domain.po.mysql.category.DokkanCategoryPO;
import com.hb.dokkan.common.enums.*;
import com.hb.dokkan.common.utils.DateUtils;
import com.hb.dokkan.common.utils.JsonUtils;
import com.hb.dokkan.infrastructure.mysql.cards.*;
import com.hb.dokkan.infrastructure.mysql.links.DokkanLinkRepository;
import com.hb.dokkan.infrastructure.mysql.categories.DokkanCategoryRepository;
import com.hb.dokkan.service.convert.KxWikiEsConvert;
import com.hb.dokkan.service.storage.CardIconStorageService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description 从新卡片表构建兼容 ES 文档，只读取本批卡片的子表
 * @Author Codex
 * @Date 2026/11/8
 **/
@Component
public class EsCardSyncHelper {
    /**
     * 卡片状态表访问仓储。
     **/
    @Resource
    private CardStateRepository stateRepository;
    /**
     * 卡片技能表访问仓储。
     **/
    @Resource
    private CardSkillRepository skillRepository;
    /**
     * 卡片分类关系表访问仓储。
     **/
    @Resource
    private CardCategoryRelRepository categoryRelRepository;
    /**
     * 卡片链接关系表访问仓储。
     **/
    @Resource
    private CardLinkRelRepository linkRelRepository;
    /**
     * 卡片主表访问仓储。
     **/
    @Resource
    private DokkanCardRepository cardRepository;
    /**
     * 分类字典访问仓储。
     **/
    @Resource
    private DokkanCategoryRepository categoryRepository;
    /**
     * 链接技能字典访问仓储。
     **/
    @Resource
    private DokkanLinkRepository linkRepository;
    /**
     * 卡片主记录、技能与潜力到 ES 文档的转换器。
     **/
    @Resource
    private KxWikiEsConvert convert;
    /**
     * 卡片头像下载及对象存储服务。
     **/
    @Resource
    private CardIconStorageService cardIconStorageService;

    /**
     * 按本批卡片读取状态、技能、关系和字典并组装完整 ES 文档。
     **/
    public List<CardEsPO> buildEsCards(List<CardPO> cards) {
        if (cards == null || cards.isEmpty()) {
            return List.of();
        }
        List<Long> ids = cards.stream().map(CardPO::getCardId).toList();
        Map<Long, List<CardStatePO>> states = stateRepository.queryByCardIds(ids).stream().collect(Collectors.groupingBy(CardStatePO::getCardId));
        Map<Long, List<CardSkillPO>> skills = skillRepository.queryByCardIds(ids).stream().collect(Collectors.groupingBy(CardSkillPO::getCardId));
        List<CardCategoryRelPO> categoryRelations = categoryRelRepository.queryByCardIds(ids);
        List<CardLinkRelPO> linkRelations = linkRelRepository.queryByCardIds(ids);
        Map<Long, String> categoryNames = categoryRepository.list().stream().collect(Collectors.toMap(DokkanCategoryPO::getCategoryId,
                row -> Objects.toString(row.getCategoryName(), CardSyncConstants.EMPTY_TEXT), (left, right) -> left));
        Map<Long, String> linkNames = linkRepository.list().stream().collect(Collectors.toMap(DokkanLinkPO::getLinkId,
                row -> Objects.toString(row.getLinkName(), CardSyncConstants.EMPTY_TEXT), (left, right) -> left));
        Map<Long, List<CardPO>> forms = cardRepository.lambdaQuery().in(CardPO::getBaseCardId, ids).list().stream()
                .collect(Collectors.groupingBy(CardPO::getBaseCardId));
        Map<Long, List<CardCategoryRelPO>> categoryByCard = categoryRelations.stream().collect(Collectors.groupingBy(CardCategoryRelPO::getCardId));
        Map<Long, List<CardLinkRelPO>> linkByCard = linkRelations.stream().collect(Collectors.groupingBy(CardLinkRelPO::getCardId));
        List<CardEsPO> documents = new ArrayList<>();
        for (CardPO card : cards) {
            CardEsPO document = convert.cardToEs(card);
            document.setCardIcon(cardIconStorageService.saveCardIcon(card.getCardId(), card.getThumbnail()));
            document.setPotentials(convert.potentials(card.getPotential()));
            applyStates(document, states.getOrDefault(card.getCardId(), List.of()));
            applySkills(document, skills.getOrDefault(card.getCardId(), List.of()));
            document.setCategories(categoryByCard.getOrDefault(card.getCardId(), List.of()).stream()
                    .map(CardCategoryRelPO::getCategoryId).map(categoryNames::get).filter(Objects::nonNull)
                    .collect(Collectors.joining(CardSyncConstants.DISPLAY_NAME_SEPARATOR)));
            document.setLinks(linkByCard.getOrDefault(card.getCardId(), List.of()).stream()
                    .sorted(Comparator.comparing(CardLinkRelPO::getSort)).map(CardLinkRelPO::getLinkId).map(linkNames::get)
                    .filter(Objects::nonNull).collect(Collectors.joining(CardSyncConstants.DISPLAY_NAME_SEPARATOR)));
            applyForms(document, forms.getOrDefault(card.getCardId(), List.of()));
            documents.add(document);
        }
        return documents;
    }

    /**
     * base 取基础技能和数值；eza 取极限技能；seza 优先提供极限数值及独立被动。
     **/
    private void applyStates(CardEsPO document, List<CardStatePO> states) {
        Map<String, CardStatePO> byKey = states.stream().collect(Collectors.toMap(CardStatePO::getStateKey, Function.identity()));
        CardStatePO base = byKey.get(CardStateEnum.BASE.getKey());
        if (base == null) {
            throw new IllegalStateException(CardSyncConstants.MISSING_BASE_STATE_MESSAGE + document.getCardId());
        }
        document.setHpValue(base.getHpValue());
        document.setAtkValue(base.getAtkValue());
        document.setDefValue(base.getDefValue());
        document.setLeaderSkill(base.getLeaderSkillDesc());
        document.setPassiveSkill(base.getPassiveSkillDesc());
        CardStatePO eza = byKey.get(CardStateEnum.EZA.getKey());
        CardStatePO seza = byKey.get(CardStateEnum.SEZA.getKey());
        if (eza != null) {
            document.setEzaLeaderSkill(eza.getLeaderSkillDesc());
            document.setEzaPassiveSkill(eza.getPassiveSkillDesc());
            document.setEzaPublishTime(eza.getPublishTime());
        }
        CardStatePO highest = seza == null ? eza : seza;
        if (highest != null) {
            document.setEzaHpValue(highest.getHpValue());
            document.setEzaAtkValue(highest.getAtkValue());
            document.setEzaDefValue(highest.getDefValue());
        }
        if (seza != null) {
            document.setSuperEzaPassiveSkill(seza.getPassiveSkillDesc());
            document.setSuperEzaPublishTime(seza.getPublishTime());
        }
        document.setOrderByTime(DateUtils.latestDate(document.getPublishTime(), document.getEzaPublishTime(), document.getSuperEzaPublishTime()));
    }

    /**
     * 技能按状态、展示顺序排列；super 保留 base/eza，其他技能按原类型展示。
     **/
    private void applySkills(CardEsPO document, List<CardSkillPO> skills) {
        List<CardSkillPO> sorted = skills.stream().sorted(Comparator.comparingInt((CardSkillPO row) ->
                stateOrder(row.getStateKey())).thenComparing(CardSkillPO::getSort)).toList();
        document.setSpecialSkills(sorted.stream().filter(row -> CardSkillTypeEnum.SUPER.getType().equals(row.getSkillType()))
                .filter(row -> !CardStateEnum.SEZA.getKey().equals(row.getStateKey())).map(convert::skillToSpecial).toList());
        document.setStandBySkills(sorted.stream().filter(row -> CardSkillTypeEnum.STANDBY.getType().equals(row.getSkillType()))
                .map(convert::skillToEsSkill).toList());
        document.setFinishSkills(sorted.stream().filter(row -> CardSkillTypeEnum.FINISH.getType().equals(row.getSkillType()))
                .map(convert::skillToEsSkill).toList());
        sorted.stream().filter(row -> CardSkillTypeEnum.ACTIVE.getType().equals(row.getSkillType()))
                .findFirst().ifPresent(row -> document.setActiveSkill(row.getEffectDesc()));
    }

    /**
     * 状态顺序复用枚举顺序，未知状态放在末尾。
     **/
    private int stateOrder(String key) {
        return Arrays.stream(CardStateEnum.values()).filter(state -> state.getKey().equals(key))
                .findFirst().map(Enum::ordinal).orElse(Integer.MAX_VALUE);
    }

    /**
     * 由 base_card_id 关系生成旧前端使用的 transformations JSON。
     **/
    private void applyForms(CardEsPO document, List<CardPO> forms) {
        List<Map<String, Object>> transformations = new ArrayList<>();
        for (CardPO form : forms.stream().sorted(Comparator.comparing(CardPO::getCardId)).toList()) {
            Map<String, Object> next = new LinkedHashMap<>();
            next.put(CardSyncConstants.TRANSFORM_ID_FIELD, form.getCardId());
            next.put(CardSyncConstants.TRANSFORM_NAME_FIELD, form.getCardName());
            next.put(CardSyncConstants.TRANSFORM_TYPE_FIELD, CardTransformationTypeEnum.fromKxFormKind(form.getFormKind()));
            next.put(CardSyncConstants.TRANSFORM_CONDITION_FIELD, form.getFormTrigger());
            Map<String, Object> relation = new LinkedHashMap<>();
            relation.put(CardSyncConstants.TRANSFORM_START_CARD_ID_FIELD, document.getCardId());
            relation.put(CardSyncConstants.TRANSFORM_NEXT_CARD_ID_FIELD, form.getCardId());
            relation.put(CardSyncConstants.TRANSFORM_NEXT_CARD_FIELD, next);
            transformations.add(relation);
        }
        document.setNextCardId(forms.isEmpty() ? null : forms.stream().map(CardPO::getCardId).min(Long::compareTo).orElse(null));
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(EsCardAttributeKey.TRANSFORMATIONS, JsonUtils.object2Json(transformations));
        document.setAttributes(attributes);
    }
}
