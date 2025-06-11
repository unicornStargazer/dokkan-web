package com.hb.dokkan.config.mybatis;


import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.hb.dokkan.common.utils.IdGeneratorUtil;
import org.springframework.stereotype.Component;

@Component
public class DokkanIdGenerator implements IdentifierGenerator {


    @Override
    public Number nextId(Object entity) {
        return null;
    }

    /**
     * 自动生成主键
     * @param entity 实体
     * @return String
     */
    @Override
    public String nextUUID(Object entity) {
        return IdGeneratorUtil.generate16CharUuid();
    }
}
