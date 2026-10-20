package com.hb.dokkan.translation.repository;

import com.hb.dokkan.common.domain.po.mysql.category.DokkanCategoryPO;

import java.util.List;

/** 翻译模块读取分类术语的窄端口。 */
public interface TranslationGlossaryStore {

    /** 查询本地分类术语，数据不可用时由实现返回空列表。 */
    List<DokkanCategoryPO> listCategories();
}
