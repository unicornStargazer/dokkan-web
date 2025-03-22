package com.hb.dokkan.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.hb.dokkan.common.exception.domain.DokkanSysException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * @Description json工具类
 * @Author stargazer
 * @Date 2025/3/22 23:04
 **/
@UtilityClass
@Slf4j
public class JsonUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Map<String,Object> json2Map(String jsonStr) throws JsonProcessingException {
        if (StringUtils.isBlank(jsonStr)) {
            return Maps.newHashMap();
        }
        Map<String, Object> result = objectMapper.readValue(jsonStr, new TypeReference<Map<String, Object>>() {});
        return result;
    }

    public static <T> T  json2Object(String jsonStr, Class<T> clazz) {
        if (StringUtils.isBlank(jsonStr) || ObjectUtils.isEmpty(clazz)) {
            return null;
        }
        try {
            return objectMapper.readValue(jsonStr, new TypeReference<T>() {
            });
        } catch (JsonProcessingException e) {
            log.error("json convert fail, json:{} e",jsonStr,e);
            throw new DokkanSysException(e.getMessage());
        }
    }

}
