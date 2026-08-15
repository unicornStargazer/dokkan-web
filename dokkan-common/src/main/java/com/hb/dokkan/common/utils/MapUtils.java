package com.hb.dokkan.common.utils;

import java.util.Map;

/**
 * @Description Map工具类
 * @Author stargazer
 * @Date 2025/12/14 2:14
 **/
public class MapUtils {

    /**
     * 判断 Map 是否为空。
     *
     * @param map 待判断 Map
     * @return Map 为 null 或无元素时返回 true
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * 判断 Map 是否非空。
     *
     * @param map 待判断 Map
     * @return Map 不为 null 且有元素时返回 true
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }
}
