package com.hb.dokkan.service.job.sync.strategy;

import com.hb.dokkan.common.enums.FixDataTypeEnum;

/**
 * 数据修复策略
 *
 * @author huangbiao
 * @date 2026/1/5 13:46
 **/
public interface FixDataStrategy {

    /**
     * 获取修复数据策略类型
     * @see FixDataTypeEnum
     */
    FixDataTypeEnum getFixDataType();


    /**
     * 修复数据
     */
    void fixData();

}
