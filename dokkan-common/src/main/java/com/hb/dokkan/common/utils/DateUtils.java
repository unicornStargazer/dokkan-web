package com.hb.dokkan.common.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
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

    /** 日期格式：年月日。 */
    private static final String DATE_PATTERN = "yyyy-MM-dd";

    /** 日期时间格式：精确到秒。 */
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /** DokkanDB 未携带时区的日期默认按东京时区解析。 */
    private static final String DOKKAN_DB_DEFAULT_ZONE = "Asia/Tokyo";

    /** 日期格式化器：年月日。 */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_PATTERN);

    /** 日期时间格式化器：精确到秒。 */
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat(DATE_TIME_PATTERN);

    /**
     * 将日期格式化为年月日字符串。
     *
     * @param day 日期对象
     * @return 年月日字符串，入参为空时返回 null
     */
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
     */
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
     */
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
     */
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
     * 解析 DokkanDB 日期字符串，优先支持带时区时间，未带时区时按东京时区兜底。
     *
     * @param value DokkanDB 日期字符串
     * @return 日期对象，入参为空或解析失败时返回 null
     */
    public static Date parseDokkanDbDate(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return Date.from(OffsetDateTime.parse(value).toInstant());
        } catch (Exception ignored) {
            return parseDokkanDbLocalDate(value);
        }
    }

    /**
     * 将 DokkanDB 未携带时区的日期字符串按东京时区解析。
     *
     * @param value DokkanDB 日期字符串
     * @return 日期对象，解析失败时返回 null
     */
    private static Date parseDokkanDbLocalDate(String value) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return Date.from(dateTime.atZone(ZoneId.of(DOKKAN_DB_DEFAULT_ZONE)).toInstant());
        } catch (Exception ignored) {
            return null;
        }
    }
}
