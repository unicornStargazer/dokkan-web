package com.hb.dokkan.service.categories;

import com.hb.dokkan.common.constants.CategoryConstants;
import com.hb.dokkan.common.constants.ExceptionErrorCode;
import com.hb.dokkan.common.domain.po.mysql.category.DokkanCategoryPO;
import com.hb.dokkan.common.domain.request.category.CategoryQueryRequest;
import com.hb.dokkan.common.domain.request.category.CategorySaveRequest;
import com.hb.dokkan.common.domain.response.base.PageResponse;
import com.hb.dokkan.common.domain.response.category.CategoryResponse;
import com.hb.dokkan.common.exception.domain.DokkanBizException;
import com.hb.dokkan.infrastructure.mysql.categories.DokkanCategoryRepository;
import com.hb.dokkan.service.convert.DokkanCategoryConvert;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * @Description 分类管理服务
 * @Author stargazer
 * @Date 2026/8/16 00:30
 **/
@Service
public class DokkanCategoryService {

    /**
     * 分类字典访问仓储。
     **/
    @Resource
    private DokkanCategoryRepository categoryRepository;

    /**
     * 分类请求、持久化对象及响应转换器。
     **/
    @Resource
    private DokkanCategoryConvert categoryConvert;


    /**
     * 分页查询分类列表。
     *
     * @param request 查询请求
     * @return 分类分页响应
     **/
    public PageResponse<CategoryResponse> queryPage(CategoryQueryRequest request) {
        PageResponse<DokkanCategoryPO> page = categoryRepository.queryPage(request);
        return PageResponse.<CategoryResponse>builder()
                .currentPage(page.getCurrentPage())
                .pageSize(page.getPageSize())
                .total(page.getTotal())
                .data(categoryConvert.poListToResponseList(page.getData()))
                .build();
    }

    /**
     * 新增或编辑分类，人工中文名称由后续 reference 同步保留。
     *
     * @param request 分类保存请求
     * @return 保存后的分类响应
     **/
    @Transactional(rollbackFor = Exception.class)
    public CategoryResponse save(CategorySaveRequest request) {
        checkSaveRequest(request);
        checkExistingCategory(request);
        checkDuplicateCategoryId(request);
        DokkanCategoryPO category = categoryConvert.saveRequestToPo(request);
        normalizeSaveFields(category);
        categoryRepository.saveOrUpdate(category);
        return categoryConvert.poToResponse(categoryRepository.getById(category.getId()));
    }

    /**
     * 校验分类保存请求必填项和长度限制。
     *
     * @param request 分类保存请求
     **/
    private void checkSaveRequest(CategorySaveRequest request) {
        if (!hasRequiredCategoryFields(request)) {
            throw new DokkanBizException(ExceptionErrorCode.CATEGORY_PARAM_ERROR);
        }
        if (request.getCategoryName().length() > CategoryConstants.CATEGORY_NAME_MAX_LENGTH
                || StringUtils.length(request.getCategoryNameEn()) > CategoryConstants.CATEGORY_NAME_EN_MAX_LENGTH) {
            throw new DokkanBizException(ExceptionErrorCode.CATEGORY_PARAM_ERROR);
        }
    }

    /**
     * 依次校验分类请求、业务 ID 与中文名，长度限制由调用方继续检查。
     **/
    private boolean hasRequiredCategoryFields(CategorySaveRequest request) {
        if (Objects.isNull(request)) {
            return false;
        } else if (Objects.isNull(request.getCategoryId())) {
            return false;
        } else if (request.getCategoryId() <= CategoryConstants.MIN_VALID_CATEGORY_ID) {
            return false;
        } else {
            return StringUtils.isNotBlank(request.getCategoryName());
        }
    }

    /**
     * 编辑分类时校验原记录存在，避免误新增重复数据。
     *
     * @param request 分类保存请求
     **/
    private void checkExistingCategory(CategorySaveRequest request) {
        if (StringUtils.isBlank(request.getId())) {
            return;
        }
        DokkanCategoryPO existing = categoryRepository.getById(request.getId());
        if (Objects.isNull(existing)) {
            throw new DokkanBizException(ExceptionErrorCode.CATEGORY_NOT_FOUND);
        }
    }

    /**
     * 校验分类业务 ID 不与其他分类重复。
     *
     * @param request 分类保存请求
     **/
    private void checkDuplicateCategoryId(CategorySaveRequest request) {
        List<DokkanCategoryPO> existingCategories = categoryRepository.listByCategoryId(request.getCategoryId());
        boolean duplicated = existingCategories.stream()
                .anyMatch(category -> !Objects.equals(category.getId(), request.getId()));
        if (duplicated) {
            throw new DokkanBizException(ExceptionErrorCode.CATEGORY_DUPLICATE);
        }
    }

    /**
     * 归一化分类保存字段，避免名称含多余空白字符。
     *
     * @param category 分类数据库对象
     **/
    private void normalizeSaveFields(DokkanCategoryPO category) {
        category.setCategoryName(category.getCategoryName().trim());
        category.setCategoryNameEn(StringUtils.trimToNull(category.getCategoryNameEn()));
    }
}
