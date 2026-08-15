package com.hb.dokkan.common.utils;

import java.util.Objects;

/**
 * @Description 字符串工具类
 * @Author stargazer
 * @Date 2025/6/2 1:12
 **/
public class StringUtils {

    /**
     * 将蛇形命名转换为驼峰命名。
     *
     * @param snakeCase 蛇形字符串
     * @return 驼峰字符串，入参为空时返回原值
     */
    public static String snakeToCamel(String snakeCase) {
        if (snakeCase == null || snakeCase.isEmpty()) {
            return snakeCase;
        }

        StringBuilder camelCaseBuilder = new StringBuilder();
        String[] parts = snakeCase.split("_");
        boolean firstPart = true;

        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }

            if (firstPart && camelCaseBuilder.isEmpty()) {
                camelCaseBuilder.append(part.toLowerCase());
                firstPart = false;
            } else {
                camelCaseBuilder.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    camelCaseBuilder.append(part.substring(1).toLowerCase());
                }
            }
        }
        return camelCaseBuilder.toString();
    }

    /**
     * 判断字符串是否为空白。
     *
     * @param str 待判断字符串
     * @return 字符串为 null、空串或纯空白时返回 true
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 判断所有字符串是否都为空白。
     *
     * @param strs 待判断字符串数组
     * @return 所有字符串为空白或数组为空时返回 true
     */
    public static boolean isAllBlank(String... strs) {
        if (Objects.isNull(strs)) {
            return true;
        }
        for (String str : strs) {
            if (isNotBlank(str)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断字符串是否非空白。
     *
     * @param str 待判断字符串
     * @return 字符串非空白时返回 true
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * 判断两个字符串是否相等。
     *
     * @param str1 字符串一
     * @param str2 字符串二
     * @return 两个字符串相等时返回 true
     */
    public static boolean equals(String str1, String str2) {
        return Objects.equals(str1, str2);
    }
}
