package com.hb.dokkan.service.job.sync.factory;

import com.hb.dokkan.common.enums.FixDataTypeEnum;
import com.hb.dokkan.service.job.sync.strategy.FixDataStrategy;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * 修复数据策略工厂
 *
 * @author huangbiao
 * @date 2026/1/5 11:54
 **/
@Component
public class FixDataStrategyFactory {

    @Resource
    private List<FixDataStrategy> fixDataStrategies;

    public FixDataStrategy getFixDataStrategy(Integer type) {
        if (type == null || Objects.isNull(FixDataTypeEnum.valuesOfCode(type))) {
            return null;
        }
        FixDataTypeEnum fixDataTypeEnum = FixDataTypeEnum.valuesOfCode(type);
        for (FixDataStrategy strategy : fixDataStrategies) {
            if (strategy.getFixDataType().equals(fixDataTypeEnum)){
                return strategy;
            }
        }
        return null;
    }
}
