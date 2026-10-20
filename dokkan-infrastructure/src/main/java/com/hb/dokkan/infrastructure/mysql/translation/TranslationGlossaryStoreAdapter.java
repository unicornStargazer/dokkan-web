package com.hb.dokkan.infrastructure.mysql.translation;

import com.hb.dokkan.common.domain.po.mysql.category.DokkanCategoryPO;
import com.hb.dokkan.infrastructure.mysql.categories.DokkanCategoryRepository;
import com.hb.dokkan.translation.repository.TranslationGlossaryStore;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.List;

/** 将翻译模块分类术语端口适配到分类 MySQL Repository。 */
@Repository
public class TranslationGlossaryStoreAdapter implements TranslationGlossaryStore {

    @Resource
    private DokkanCategoryRepository categoryRepository;

    /** {@inheritDoc} */
    @Override
    public List<DokkanCategoryPO> listCategories() {
        return categoryRepository.list();
    }
}
