package com.hb.dokkan.common.utils;

import java.util.Map;

/**
 * @Description xxxxx
 * @Author stargazer
 * @Date 2025/12/14 2:14
 **/
public class MapUtils {

    /**
     * map是否为空
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }
}
