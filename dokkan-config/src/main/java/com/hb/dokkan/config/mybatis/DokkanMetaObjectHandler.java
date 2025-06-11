package com.hb.dokkan.config.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class DokkanMetaObjectHandler implements MetaObjectHandler {

    /**
     * 自动填充 插入
     * @param metaObject 元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
       this.setFieldValByName("updateTime",new Date(),metaObject);
       this.setFieldValByName("createTime",new Date(),metaObject);
       this.setFieldValByName("creator",getUser(),metaObject);
       this.setFieldValByName("modifier",getUser(),metaObject);
    }

    private String getUser() {
//        UserEntity user = UserUtils.getUser();
//        if (user == null){
//            return "system";
//        }
        return "system";
    }

    /**
     * 自动填充 更新
     * @param metaObject 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新时自动填充 updTm 字段
        this.setFieldValByName("updTm",new Date(),metaObject);
        this.setFieldValByName("updBy",getUser(),metaObject);
    }
}
