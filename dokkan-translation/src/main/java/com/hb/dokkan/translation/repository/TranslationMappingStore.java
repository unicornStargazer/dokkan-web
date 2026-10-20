package com.hb.dokkan.translation.repository;

import com.hb.dokkan.common.domain.po.mysql.translation.TranslationMappingPO;
import com.hb.dokkan.common.domain.request.translation.TranslationMappingQueryRequest;
import com.hb.dokkan.common.domain.response.base.PageResponse;

import java.util.List;

/** 翻译模块访问映射存储的窄端口，由基础设施模块提供实现。 */
public interface TranslationMappingStore {

    /** 分页查询映射。 */
    PageResponse<TranslationMappingPO> queryPage(TranslationMappingQueryRequest request);

    /** 查询启用映射。 */
    List<TranslationMappingPO> listEnabledMappings();

    /** 按原文查询映射，不存在时返回 null。 */
    TranslationMappingPO getBySourceText(String sourceText);

    /** 按主键查询映射，不存在时返回 null。 */
    TranslationMappingPO getById(String id);

    /** 新增或更新映射。 */
    boolean saveOrUpdate(TranslationMappingPO mapping);

    /** 新增映射，原文已存在时不得覆盖。 */
    boolean saveIfAbsentBySourceText(TranslationMappingPO mapping);

    /** 删除指定映射。 */
    boolean removeByIds(List<String> ids);

    /** 更新映射。 */
    boolean updateById(TranslationMappingPO mapping);
}
