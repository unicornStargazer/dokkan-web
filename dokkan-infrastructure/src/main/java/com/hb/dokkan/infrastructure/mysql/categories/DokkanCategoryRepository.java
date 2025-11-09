package com.hb.dokkan.infrastructure.mysql.categories;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hb.dokkan.infrastructure.mysql.categories.domain.DokkanCategoryPO;
import com.hb.dokkan.infrastructure.mysql.categories.mapper.DokkanCategoryMapper;
import org.springframework.stereotype.Repository;

/**
 * @Description 分类数据库操作类
 * @Author stargazer
 * @Date 2025/11/8 22:12
 **/
@Repository
public class DokkanCategoryRepository extends ServiceImpl<DokkanCategoryMapper, DokkanCategoryPO> {
}
