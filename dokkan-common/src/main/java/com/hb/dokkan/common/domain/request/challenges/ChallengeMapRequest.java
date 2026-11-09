package com.hb.dokkan.common.domain.request.challenges;

import lombok.Data;

import java.io.Serializable;

/**
 * 查询一张挑战难度地图敌人详情的请求参数。
 **/
@Data
public class ChallengeMapRequest implements Serializable {
    /**
     * 序列化版本标识。
     **/
    private static final long serialVersionUID = 1L;

    /**
     * 源难度地图 ID。
     **/
    private Long mapId;
}
