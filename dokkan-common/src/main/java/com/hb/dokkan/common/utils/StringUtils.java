package com.hb.dokkan.common.utils;

import java.util.Objects;

/**
 * @Description 字符串工具类
 * @Author stargazer
 * @Date 2025/6/2 1:12
 **/
public class StringUtils {

    /**
     * 蛇形转驼峰
     */
    public static String snakeToCamel(String snakeCase) {
        if (snakeCase == null || snakeCase.isEmpty()) {
            return snakeCase; // 或者返回 ""，根据需求
        }

        StringBuilder camelCaseBuilder = new StringBuilder();
        String[] parts = snakeCase.split("_");

        boolean firstPart = true;

        for (String part : parts) {
            if (part.isEmpty()) { // 处理多个连续下划线或首尾下划线的情况
                if (firstPart && camelCaseBuilder.isEmpty()) {
                    // 如果是类似 "_word" 的情况，第一个有效部分仍应按首个单词处理
                    // 但如果只是 "___"，则 parts 会包含多个空字符串，最终结果为空
                    continue;
                } else if (!firstPart) {
                    // 如果是 "word__next"，中间的空 part 表示一个额外的下划线，
                    // 这通常意味着下一个单词应该大写（如果存在）
                    // 但对于标准的 snake_case -> camelCase，我们通常忽略多余的下划线
                    continue;
                } else {
                    // 处理类似 "____word" 的情况，或者纯粹是下划线的情况
                    continue;
                }
            }

            if (firstPart && camelCaseBuilder.isEmpty()) { // 确保这是第一个非空部分
                // 第一个单词（或部分）转换为小写
                // 如果原 snake_case 可能包含大写字母，例如 "FIRST_NAME"，这里会确保首单词小写 "first"
                camelCaseBuilder.append(part.toLowerCase());
                firstPart = false;
            } else {
                // 后续单词首字母大写，其余小写
                // 如果原 snake_case 部分包含大写，例如 "user_ID_Value"，会变成 "userIdValue"
                camelCaseBuilder.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    camelCaseBuilder.append(part.substring(1).toLowerCase());
                }
            }
        }
        return camelCaseBuilder.toString();
    }

    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

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

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static boolean equals(String str1, String str2) {
        return Objects.equals(str1, str2);
    }
}
