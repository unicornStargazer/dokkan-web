package com.hb.dokkan.common.domain.request.challenges;

import lombok.Data;

import java.io.Serializable;

/**
 * 活动挑战列表分页和筛选参数。
 **/
@Data
public class ChallengeListRequest implements Serializable {
    /**
     * 序列化版本标识。
     **/
    private static final long serialVersionUID = 1L;

    /**
     * 当前页码，从1开始。
     **/
    private Integer pageNum;

    /**
     * 单页记录数，服务端限制最大值。
     **/
    private Integer pageSize;

    /**
     * 活动名称模糊查询关键字。
     **/
    private String eventName;

    /**
     * 可选地图快照状态筛选：none、captured或partial。
     **/
    private String dataStatus;
}
