package com.hb.dokkan.common.domain.request.challenges;

import lombok.Data;

import java.io.Serializable;

/**
 * 查询一个活动挑战目录的请求参数。
 **/
@Data
public class ChallengeEventRequest implements Serializable {
    /**
     * 序列化版本标识。
     **/
    private static final long serialVersionUID = 1L;

    /**
     * 源活动 ID。
     **/
    private Long eventId;
}
