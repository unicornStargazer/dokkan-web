package com.hb.dokkan.common.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ObjectUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description xxxxx
 * @Author stargazer
 * @Date 2025/3/22 23:46
 **/
@UtilityClass
public class DateUtils {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String date2Day(Date day) {
        if (ObjectUtils.isEmpty(day)) {
            return null;
        }
        return DATE_FORMAT.format(day);
    }

    public static String date2Second(Date time) {
        if (ObjectUtils.isEmpty(time)) {
            return null;
        }
        return DATE_TIME_FORMAT.format(time);
    }

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

}
