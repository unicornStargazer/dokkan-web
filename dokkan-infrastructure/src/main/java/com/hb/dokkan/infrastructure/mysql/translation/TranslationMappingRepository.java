package com.hb.dokkan.infrastructure.mysql.translation;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.hb.dokkan.common.constants.TranslationMappingConstants;
import com.hb.dokkan.common.domain.po.mysql.translation.TranslationMappingPO;
import com.hb.dokkan.common.domain.request.translation.TranslationMappingQueryRequest;
import com.hb.dokkan.common.domain.response.base.PageResponse;
import com.hb.dokkan.common.utils.CollectionUtils;
import com.hb.dokkan.infrastructure.mysql.translation.mapper.TranslationMappingMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @Description 翻译映射数据库操作类
 * @Author stargazer
 * @Date 2026/8/15 23:30
 **/
@Repository
public class TranslationMappingRepository extends ServiceImpl<TranslationMappingMapper, TranslationMappingPO> {

    /**
     * 分页查询翻译映射。
     *
     * @param request 查询请求
     * @return 翻译映射分页结果
     */
    public PageResponse<TranslationMappingPO> queryPage(TranslationMappingQueryRequest request) {
        int pageNum = resolvePageNum(request);
        int pageSize = resolvePageSize(request);
        Page<TranslationMappingPO> page = this.page(new Page<>(pageNum, pageSize), buildQueryWrapper(request));
        return PageResponse.<TranslationMappingPO>builder()
                .currentPage((int) page.getCurrent())
                .pageSize((int) page.getSize())
                .total(page.getTotal())
                .data(CollectionUtils.isEmpty(page.getRecords()) ? Lists.newArrayList() : page.getRecords())
                .build();
    }

    /**
     * 查询启用中的翻译映射。
     *
     * @return 启用中的翻译映射列表
     */
    public List<TranslationMappingPO> listEnabledMappings() {
        LambdaQueryWrapper<TranslationMappingPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TranslationMappingPO::getEnabled, Boolean.TRUE)
                .orderByDesc(TranslationMappingPO::getUpdateTime);
        List<TranslationMappingPO> rows = this.list(wrapper);
        return CollectionUtils.isEmpty(rows) ? Lists.newArrayList() : rows;
    }

    /**
     * 按原文查询翻译映射。
     *
     * @param sourceText 原文术语或短语
     * @return 翻译映射，不存在时返回 null
     */
    public TranslationMappingPO getBySourceText(String sourceText) {
        if (StringUtils.isBlank(sourceText)) {
            return null;
        }
        LambdaQueryWrapper<TranslationMappingPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TranslationMappingPO::getSourceText, sourceText);
        return this.getOne(wrapper, false);
    }

    /**
     * 按原文新增翻译映射，已存在时保留用户维护的数据不覆盖。
     *
     * @param mapping 翻译映射
     * @return 是否新增成功
     */
    public boolean saveIfAbsentBySourceText(TranslationMappingPO mapping) {
        if (Objects.isNull(mapping) || StringUtils.isBlank(mapping.getSourceText())) {
            return false;
        }
        TranslationMappingPO existing = getBySourceText(mapping.getSourceText());
        if (Objects.nonNull(existing)) {
            return false;
        }
        return this.save(mapping);
    }

    /**
     * 根据 ID 列表删除翻译映射。
     *
     * @param ids 翻译映射 ID 列表
     * @return 是否删除成功
     */
    public boolean removeByMappingIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        return this.removeByIds(ids);
    }

    /**
     * 构建翻译映射查询条件。
     *
     * @param request 查询请求
     * @return 查询条件
     */
    private LambdaQueryWrapper<TranslationMappingPO> buildQueryWrapper(TranslationMappingQueryRequest request) {
        LambdaQueryWrapper<TranslationMappingPO> wrapper = new LambdaQueryWrapper<>();
        if (Objects.isNull(request)) {
            return wrapper.orderByDesc(TranslationMappingPO::getUpdateTime);
        }
        if (StringUtils.isNotBlank(request.getKeyword())) {
            wrapper.and(query -> query.like(TranslationMappingPO::getSourceText, request.getKeyword())
                    .or()
                    .like(TranslationMappingPO::getTargetText, request.getKeyword())
                    .or()
                    .like(TranslationMappingPO::getRemark, request.getKeyword()));
        }
        wrapper.like(StringUtils.isNotBlank(request.getSourceText()), TranslationMappingPO::getSourceText,
                        request.getSourceText())
                .like(StringUtils.isNotBlank(request.getTargetText()), TranslationMappingPO::getTargetText,
                        request.getTargetText())
                .eq(StringUtils.isNotBlank(request.getSourceLanguage()), TranslationMappingPO::getSourceLanguage,
                        request.getSourceLanguage())
                .eq(StringUtils.isNotBlank(request.getMappingType()), TranslationMappingPO::getMappingType,
                        request.getMappingType())
                .eq(Objects.nonNull(request.getEnabled()), TranslationMappingPO::getEnabled, request.getEnabled())
                .orderByDesc(TranslationMappingPO::getUpdateTime);
        return wrapper;
    }

    /**
     * 解析页码，缺失或非法时使用默认值。
     *
     * @param request 查询请求
     * @return 页码
     */
    private int resolvePageNum(TranslationMappingQueryRequest request) {
        if (Objects.isNull(request) || Objects.isNull(request.getPageNum())
                || request.getPageNum() < TranslationMappingConstants.DEFAULT_PAGE_NUM) {
            return TranslationMappingConstants.DEFAULT_PAGE_NUM;
        }
        return request.getPageNum();
    }

    /**
     * 解析每页数量，避免一次查询过多数据。
     *
     * @param request 查询请求
     * @return 每页数量
     */
    private int resolvePageSize(TranslationMappingQueryRequest request) {
        if (Objects.isNull(request) || Objects.isNull(request.getPageSize())
                || request.getPageSize() < TranslationMappingConstants.DEFAULT_PAGE_NUM) {
            return TranslationMappingConstants.DEFAULT_PAGE_SIZE;
        }
        return Math.min(request.getPageSize(), TranslationMappingConstants.MAX_PAGE_SIZE);
    }
}
