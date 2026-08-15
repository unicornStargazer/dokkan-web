package com.hb.dokkan.service.translation;

import com.hb.dokkan.common.constants.ExceptionErrorCode;
import com.hb.dokkan.common.constants.TranslationMappingConstants;
import com.hb.dokkan.common.domain.po.mysql.translation.TranslationMappingPO;
import com.hb.dokkan.common.domain.request.translation.TranslationMappingDeleteRequest;
import com.hb.dokkan.common.domain.request.translation.TranslationMappingQueryRequest;
import com.hb.dokkan.common.domain.request.translation.TranslationMappingSaveRequest;
import com.hb.dokkan.common.domain.request.translation.TranslationMappingStatusRequest;
import com.hb.dokkan.common.domain.response.base.PageResponse;
import com.hb.dokkan.common.domain.response.translation.TranslationMappingResponse;
import com.hb.dokkan.common.exception.domain.DokkanBizException;
import com.hb.dokkan.common.utils.CollectionUtils;
import com.hb.dokkan.common.utils.TranslationUtils;
import com.hb.dokkan.infrastructure.mysql.translation.TranslationMappingRepository;
import com.hb.dokkan.service.convert.TranslationMappingConvert;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @Description 翻译映射管理服务
 * @Author stargazer
 * @Date 2026/8/15 23:30
 **/
@Slf4j
@Service
public class TranslationMappingService {

    /** 启用翻译映射缓存，key 为原文，value 为中文译文。 */
    private final Map<String, String> mappingCache = new ConcurrentHashMap<>();

    /** 翻译映射缓存是否已加载。 */
    private volatile boolean mappingCacheLoaded;

    @Resource
    private TranslationMappingRepository translationMappingRepository;

    @Resource
    private TranslationMappingConvert translationMappingConvert;

    /**
     * 分页查询翻译映射。
     *
     * @param request 查询请求
     * @return 翻译映射分页响应
     */
    public PageResponse<TranslationMappingResponse> queryPage(TranslationMappingQueryRequest request) {
        PageResponse<TranslationMappingPO> page = translationMappingRepository.queryPage(request);
        return PageResponse.<TranslationMappingResponse>builder()
                .currentPage(page.getCurrentPage())
                .pageSize(page.getPageSize())
                .total(page.getTotal())
                .data(translationMappingConvert.poListToResponseList(page.getData()))
                .build();
    }

    /**
     * 新增或编辑翻译映射。
     *
     * @param request 保存请求
     * @return 保存后的翻译映射响应
     */
    public TranslationMappingResponse save(TranslationMappingSaveRequest request) {
        checkSaveRequest(request);
        TranslationMappingPO mapping = translationMappingConvert.saveRequestToPo(request);
        fillDefaultFields(mapping);
        checkDuplicateSource(mapping);
        translationMappingRepository.saveOrUpdate(mapping);
        refreshMappingCache();
        return translationMappingConvert.poToResponse(translationMappingRepository.getById(mapping.getId()));
    }

    /**
     * 按原文新增或更新初始化映射。
     *
     * @param sourceText  原文术语或短语
     * @param targetText  中文译文
     * @param mappingType 映射类型
     * @param remark      备注说明
     */
    public void saveInitializationMapping(String sourceText, String targetText, String mappingType, String remark) {
        if (StringUtils.isAnyBlank(sourceText, targetText)) {
            return;
        }
        TranslationMappingPO mapping = new TranslationMappingPO();
        mapping.setSourceText(sourceText.trim());
        mapping.setTargetText(TranslationUtils.toSimpleChineseText(targetText.trim()));
        mapping.setSourceLanguage(TranslationMappingConstants.DEFAULT_SOURCE_LANGUAGE);
        mapping.setMappingType(StringUtils.defaultIfBlank(mappingType, TranslationMappingConstants.MAPPING_TYPE_SYSTEM));
        mapping.setEnabled(TranslationMappingConstants.DEFAULT_ENABLED);
        mapping.setRemark(remark);
        translationMappingRepository.saveOrUpdateBySourceText(mapping);
    }

    /**
     * 删除翻译映射。
     *
     * @param request 删除请求
     */
    public void delete(TranslationMappingDeleteRequest request) {
        if (Objects.isNull(request) || CollectionUtils.isEmpty(request.getIds())) {
            throw new DokkanBizException(ExceptionErrorCode.QUERY_PARAM_ERROR);
        }
        translationMappingRepository.removeByMappingIds(request.getIds());
        refreshMappingCache();
    }

    /**
     * 更新翻译映射启用状态。
     *
     * @param request 状态变更请求
     */
    public void updateStatus(TranslationMappingStatusRequest request) {
        if (Objects.isNull(request) || StringUtils.isBlank(request.getId()) || Objects.isNull(request.getEnabled())) {
            throw new DokkanBizException(ExceptionErrorCode.QUERY_PARAM_ERROR);
        }
        TranslationMappingPO mapping = translationMappingRepository.getById(request.getId());
        if (Objects.isNull(mapping)) {
            throw new DokkanBizException(ExceptionErrorCode.QUERY_PARAM_ERROR);
        }
        mapping.setEnabled(request.getEnabled());
        translationMappingRepository.updateById(mapping);
        refreshMappingCache();
    }

    /**
     * 查询启用翻译映射缓存，未加载时自动从数据库刷新。
     *
     * @return 启用翻译映射 Map
     */
    public Map<String, String> listEnabledMappingMap() {
        if (!mappingCacheLoaded) {
            refreshMappingCache();
        }
        return new LinkedHashMap<>(mappingCache);
    }

    /**
     * 刷新启用翻译映射缓存。
     */
    public synchronized void refreshMappingCache() {
        try {
            // 先按原文长度倒序构建稳定快照，保护术语时优先匹配长短语。
            Map<String, String> mappings = translationMappingRepository.listEnabledMappings().stream()
                    .filter(row -> StringUtils.isNoneBlank(row.getSourceText(), row.getTargetText()))
                    .sorted((left, right) -> Integer.compare(right.getSourceText().length(), left.getSourceText().length()))
                    .collect(Collectors.toMap(TranslationMappingPO::getSourceText,
                            row -> TranslationUtils.toSimpleChineseText(row.getTargetText()),
                            (left, right) -> left, LinkedHashMap::new));
            mappingCache.clear();
            mappingCache.putAll(mappings);
            mappingCacheLoaded = true;
            log.info("translation mapping cache refreshed, size={}", mappingCache.size());
        } catch (Exception e) {
            mappingCacheLoaded = true;
            log.warn("refresh translation mapping cache failed", e);
        }
    }

    /**
     * 校验保存请求。
     *
     * @param request 保存请求
     */
    private void checkSaveRequest(TranslationMappingSaveRequest request) {
        if (Objects.isNull(request)
                || StringUtils.isBlank(request.getSourceText())
                || StringUtils.isBlank(request.getTargetText())) {
            throw new DokkanBizException(ExceptionErrorCode.QUERY_PARAM_ERROR);
        }
        if (request.getSourceText().length() > TranslationMappingConstants.SOURCE_TEXT_MAX_LENGTH
                || request.getTargetText().length() > TranslationMappingConstants.TARGET_TEXT_MAX_LENGTH) {
            throw new DokkanBizException(ExceptionErrorCode.QUERY_PARAM_ERROR);
        }
    }

    /**
     * 填充保存请求未传入的默认字段。
     *
     * @param mapping 翻译映射数据库对象
     */
    private void fillDefaultFields(TranslationMappingPO mapping) {
        mapping.setSourceText(mapping.getSourceText().trim());
        mapping.setTargetText(TranslationUtils.toSimpleChineseText(mapping.getTargetText().trim()));
        mapping.setSourceLanguage(StringUtils.defaultIfBlank(mapping.getSourceLanguage(),
                TranslationMappingConstants.DEFAULT_SOURCE_LANGUAGE));
        mapping.setMappingType(StringUtils.defaultIfBlank(mapping.getMappingType(),
                TranslationMappingConstants.MAPPING_TYPE_CUSTOM));
        mapping.setEnabled(Objects.isNull(mapping.getEnabled()) ? TranslationMappingConstants.DEFAULT_ENABLED
                : mapping.getEnabled());
    }

    /**
     * 校验原文是否与其他映射重复。
     *
     * @param mapping 翻译映射数据库对象
     */
    private void checkDuplicateSource(TranslationMappingPO mapping) {
        TranslationMappingPO existing = translationMappingRepository.getBySourceText(mapping.getSourceText());
        if (Objects.nonNull(existing) && !Objects.equals(existing.getId(), mapping.getId())) {
            throw new DokkanBizException(ExceptionErrorCode.QUERY_PARAM_ERROR);
        }
    }
}
