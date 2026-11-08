package com.hb.dokkan.common.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

/**
 * @Description 日期工具类
 * @Author stargazer
 * @Date 2025/3/22 23:46
 **/
@UtilityClass
public class DateUtils {

    /**
     * 严格解析 ISO 日期，非法或缺失日期直接抛错，供额度有效期等不可静默降级场景使用。
     *
     * @param value yyyy-MM-dd 日期
     * @return 本地日期
     **/
    public static LocalDate parseIsoDate(String value) {
        return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * 日期格式：年月日。
     **/
    private static final String DATE_PATTERN = "yyyy-MM-dd";

    /**
     * 日期时间格式：精确到秒。
     **/
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * 日期格式化器：年月日。
     **/
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_PATTERN);

    /**
     * 日期时间格式化器：精确到秒。
     **/
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat(DATE_TIME_PATTERN);

    /**
     * 将日期格式化为年月日字符串。
     *
     * @param day 日期对象
     * @return 年月日字符串，入参为空时返回 null
     **/
    public static String date2Day(Date day) {
        if (ObjectUtils.isEmpty(day)) {
            return null;
        }
        return DATE_FORMAT.format(day);
    }

    /**
     * 将日期格式化为精确到秒的日期时间字符串。
     *
     * @param time 日期对象
     * @return 日期时间字符串，入参为空时返回 null
     **/
    public static String date2Second(Date time) {
        if (ObjectUtils.isEmpty(time)) {
            return null;
        }
        return DATE_TIME_FORMAT.format(time);
    }

    /**
     * 将年月日字符串解析为日期。
     *
     * @param dateStr 年月日字符串
     * @return 日期对象，入参为空或解析失败时返回 null
     **/
    public static Date day2Date(String dateStr) {
        if (ObjectUtils.isEmpty(dateStr)) {
            return null;
        }
        try {
            return DATE_FORMAT.parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从多个日期中获取最新日期。
     *
     * @param dates 日期数组
     * @return 最新日期，不存在有效日期时返回 null
     **/
    public static Date latestDate(Date... dates) {
        if (ObjectUtils.isEmpty(dates)) {
            return null;
        }
        return Arrays.stream(dates)
                .filter(Objects::nonNull)
                .max(Date::compareTo)
                .orElse(null);
    }

    /**
     * 解析 KX UTC 日期；null 返回 null，非法日期抛异常避免写入错误时间。
     **/
    public static Date parseUtcDate(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        if (value.length() == "yyyy-MM-dd".length()) {
            return Date.from(LocalDate.parse(value).atStartOfDay(java.time.ZoneOffset.UTC).toInstant());
        }
        return Date.from(OffsetDateTime.parse(value).toInstant());
    }

    /**
     * 解析 Retry-After 秒数或 RFC 1123 日期；缺失、非法值使用默认毫秒值。
     **/
    public static long retryAfterMillis(String value, long fallbackMillis) {
        if (StringUtils.isBlank(value)) {
            return fallbackMillis;
        }
        try {
            return Math.max(0L, Math.multiplyExact(Long.parseLong(value.trim()), 1_000L));
        } catch (RuntimeException ignored) {
            try {
                return Math.max(0L, java.time.ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME)
                        .toInstant().toEpochMilli() - System.currentTimeMillis());
            } catch (RuntimeException invalidDate) {
                return fallbackMillis;
            }
        }
    }
}
