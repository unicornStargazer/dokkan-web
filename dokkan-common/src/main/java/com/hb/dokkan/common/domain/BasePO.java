package com.hb.dokkan.common.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description xxxxx
 * @Author stargazer
 * @Date 2025/3/9 19:38
 **/
@Data
public class BasePO implements Serializable {
    private static final long serialVersionUID = -5181688177950821430L;

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField(fill = FieldFill.INSERT)
    private String creator;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String modifier;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

}
