package com.hb.dokkan.infrastructure.mysql.categories;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.hb.dokkan.common.utils.CollectionUtils;
import com.hb.dokkan.common.domain.po.mysql.category.DokkanCategoryPO;
import com.hb.dokkan.infrastructure.mysql.categories.mapper.DokkanCategoryMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Description 分类数据库操作类
 * @Author stargazer
 * @Date 2025/11/8 22:12
 **/
@Repository
public class DokkanCategoryRepository extends ServiceImpl<DokkanCategoryMapper, DokkanCategoryPO> {

    /**
     * 根据分类id列表查询分类列表
     */
    public List<DokkanCategoryPO> queryCategoriesByIds(List<Integer> categoryIds) {
        LambdaQueryWrapper<DokkanCategoryPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(DokkanCategoryPO::getCategoryId, categoryIds);
        return queryCategories(wrapper);
    }

    /**
     * 根据查询条件查询分类列表
     */
    public List<DokkanCategoryPO> queryCategories(LambdaQueryWrapper<DokkanCategoryPO> wrapper) {
        List<DokkanCategoryPO> list = this.list(wrapper);
        if (CollectionUtils.isEmpty(list)) {
            return Lists.newArrayList();
        }
        return list;
    }

}
