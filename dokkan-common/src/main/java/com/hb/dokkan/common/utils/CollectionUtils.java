package com.hb.dokkan.common.utils;

import java.util.Collection;

/**
 * @Description 集合工具类
 * @Author stargazer
 * @Date 2025/12/13 21:42
 **/
public class CollectionUtils {

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }
}
