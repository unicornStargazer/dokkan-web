package com.hb.dokkan.common.domain;

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

    private Long id;

    private String creator;

    private Date createTime;

    private String modifier;

    private Date updateTime;

}
