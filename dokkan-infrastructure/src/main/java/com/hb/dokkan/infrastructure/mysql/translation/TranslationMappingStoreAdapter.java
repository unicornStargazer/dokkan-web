package com.hb.dokkan.infrastructure.mysql.translation;

import com.hb.dokkan.common.domain.po.mysql.translation.TranslationMappingPO;
import com.hb.dokkan.common.domain.request.translation.TranslationMappingQueryRequest;
import com.hb.dokkan.common.domain.response.base.PageResponse;
import com.hb.dokkan.translation.repository.TranslationMappingStore;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.List;

/** 将翻译模块映射存储端口适配到 MySQL Repository。 */
@Repository
public class TranslationMappingStoreAdapter implements TranslationMappingStore {

    @Resource
    private TranslationMappingRepository repository;

    /** {@inheritDoc} */
    @Override
    public PageResponse<TranslationMappingPO> queryPage(TranslationMappingQueryRequest request) {
        return repository.queryPage(request);
    }

    /** {@inheritDoc} */
    @Override
    public List<TranslationMappingPO> listEnabledMappings() {
        return repository.listEnabledMappings();
    }

    /** {@inheritDoc} */
    @Override
    public TranslationMappingPO getBySourceText(String sourceText) {
        return repository.getBySourceText(sourceText);
    }

    /** {@inheritDoc} */
    @Override
    public TranslationMappingPO getById(String id) {
        return repository.getById(id);
    }

    /** {@inheritDoc} */
    @Override
    public boolean saveOrUpdate(TranslationMappingPO mapping) {
        return repository.saveOrUpdate(mapping);
    }

    /** {@inheritDoc} */
    @Override
    public boolean saveIfAbsentBySourceText(TranslationMappingPO mapping) {
        return repository.saveIfAbsentBySourceText(mapping);
    }

    /** {@inheritDoc} */
    @Override
    public boolean removeByIds(List<String> ids) {
        return repository.removeByMappingIds(ids);
    }

    /** {@inheritDoc} */
    @Override
    public boolean updateById(TranslationMappingPO mapping) {
        return repository.updateById(mapping);
    }
}
