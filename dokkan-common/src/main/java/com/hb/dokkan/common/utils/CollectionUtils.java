package com.hb.dokkan.common.utils;

import java.util.Collection;

/**
 * @Description 集合工具类
 * @Author stargazer
 * @Date 2025/12/13 21:42
 **/
public class CollectionUtils {

    /**
     * 判断集合是否为空。
     *
     * @param collection 待判断集合
     * @return 集合为 null 或无元素时返回 true
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * 判断集合是否非空。
     *
     * @param collection 待判断集合
     * @return 集合不为 null 且有元素时返回 true
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }
}
