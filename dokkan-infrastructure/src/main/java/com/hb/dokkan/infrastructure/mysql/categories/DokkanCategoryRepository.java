package com.hb.dokkan.infrastructure.mysql.categories;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.hb.dokkan.common.constants.CategoryConstants;
import com.hb.dokkan.common.domain.po.mysql.category.DokkanCategoryPO;
import com.hb.dokkan.common.domain.request.category.CategoryQueryRequest;
import com.hb.dokkan.common.domain.response.base.PageResponse;
import com.hb.dokkan.common.utils.CollectionUtils;
import com.hb.dokkan.infrastructure.mysql.categories.mapper.DokkanCategoryMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @Description 分类数据库操作类
 * @Author stargazer
 * @Date 2025/11/8 22:12
 **/
@Repository
public class DokkanCategoryRepository extends ServiceImpl<DokkanCategoryMapper, DokkanCategoryPO> {

    /**
     * 根据分类id列表查询分类列表。
     *
     * @param categoryIds 分类 ID 列表
     * @return 分类列表
     */
    public List<DokkanCategoryPO> queryCategoriesByIds(List<Integer> categoryIds) {
        LambdaQueryWrapper<DokkanCategoryPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(DokkanCategoryPO::getCategoryId, categoryIds);
        return queryCategories(wrapper);
    }

    /**
     * 根据查询条件查询分类列表。
     *
     * @param wrapper 查询条件
     * @return 分类列表
     */
    public List<DokkanCategoryPO> queryCategories(LambdaQueryWrapper<DokkanCategoryPO> wrapper) {
        List<DokkanCategoryPO> list = this.list(wrapper);
        if (CollectionUtils.isEmpty(list)) {
            return Lists.newArrayList();
        }
        return list;
    }

    /**
     * 根据 DokkanDB 分类 ID 查询分类列表。
     *
     * @param categoryId DokkanDB 分类 ID
     * @return 分类列表
     */
    public List<DokkanCategoryPO> listByCategoryId(Long categoryId) {
        if (Objects.isNull(categoryId)) {
            return Lists.newArrayList();
        }
        LambdaQueryWrapper<DokkanCategoryPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DokkanCategoryPO::getCategoryId, categoryId);
        return queryCategories(wrapper);
    }

    /**
     * 分页查询分类列表。
     *
     * @param request 查询请求
     * @return 分类分页结果
     */
    public PageResponse<DokkanCategoryPO> queryPage(CategoryQueryRequest request) {
        int pageNum = resolvePageNum(request);
        int pageSize = resolvePageSize(request);
        Page<DokkanCategoryPO> page = this.page(new Page<>(pageNum, pageSize), buildQueryWrapper(request));
        return PageResponse.<DokkanCategoryPO>builder()
                .currentPage((int) page.getCurrent())
                .pageSize((int) page.getSize())
                .total(page.getTotal())
                .data(CollectionUtils.isEmpty(page.getRecords()) ? Lists.newArrayList() : page.getRecords())
                .build();
    }

    /**
     * 构建分类分页查询条件。
     *
     * @param request 查询请求
     * @return 查询条件
     */
    private LambdaQueryWrapper<DokkanCategoryPO> buildQueryWrapper(CategoryQueryRequest request) {
        LambdaQueryWrapper<DokkanCategoryPO> wrapper = new LambdaQueryWrapper<>();
        if (Objects.isNull(request)) {
            return wrapper.orderByAsc(DokkanCategoryPO::getCategoryId);
        }
        if (StringUtils.isNotBlank(request.getKeyword())) {
            wrapper.and(query -> {
                query.like(DokkanCategoryPO::getCategoryName, request.getKeyword())
                        .or()
                        .like(DokkanCategoryPO::getCategoryNameEn, request.getKeyword());
                if (StringUtils.isNumeric(request.getKeyword())) {
                    query.or().eq(DokkanCategoryPO::getCategoryId, Long.valueOf(request.getKeyword()));
                }
            });
        }
        wrapper.eq(Objects.nonNull(request.getCategoryId()), DokkanCategoryPO::getCategoryId,
                        request.getCategoryId())
                .like(StringUtils.isNotBlank(request.getCategoryName()), DokkanCategoryPO::getCategoryName,
                        request.getCategoryName())
                .like(StringUtils.isNotBlank(request.getCategoryNameEn()), DokkanCategoryPO::getCategoryNameEn,
                        request.getCategoryNameEn())
                .orderByAsc(DokkanCategoryPO::getCategoryId);
        return wrapper;
    }

    /**
     * 解析页码，缺失或非法时使用默认值。
     *
     * @param request 查询请求
     * @return 页码
     */
    private int resolvePageNum(CategoryQueryRequest request) {
        if (Objects.isNull(request) || Objects.isNull(request.getPageNum())
                || request.getPageNum() < CategoryConstants.DEFAULT_PAGE_NUM) {
            return CategoryConstants.DEFAULT_PAGE_NUM;
        }
        return request.getPageNum();
    }

    /**
     * 解析每页数量，避免一次查询过多数据。
     *
     * @param request 查询请求
     * @return 每页数量
     */
    private int resolvePageSize(CategoryQueryRequest request) {
        if (Objects.isNull(request) || Objects.isNull(request.getPageSize())
                || request.getPageSize() < CategoryConstants.DEFAULT_PAGE_NUM) {
            return CategoryConstants.DEFAULT_PAGE_SIZE;
        }
        return Math.min(request.getPageSize(), CategoryConstants.MAX_PAGE_SIZE);
    }
}
