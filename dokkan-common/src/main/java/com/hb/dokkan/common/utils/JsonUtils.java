package com.hb.dokkan.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.google.common.collect.Maps;
import com.hb.dokkan.common.exception.domain.DokkanSysException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
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
            return objectMapper.readValue(jsonStr, clazz);
        } catch (JsonProcessingException e) {
            log.error("json convert fail, json:{} e",jsonStr,e);
            throw new DokkanSysException(e.getMessage());
        }
    }

    public static <T> List<T> json2List(String jsonString, Class<T> elementClass) {
        if (jsonString == null || jsonString.isEmpty() || elementClass == null) {
            return Collections.emptyList();
        }
        try {
            // 构建Jackson能够理解的集合类型 List<T>
            CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, elementClass);
            return objectMapper.readValue(jsonString, listType);
        } catch (JsonProcessingException e) {
            System.err.println("JSON转换为List时发生错误: " + e.getMessage());
            // 根据需要，你可以选择抛出自定义异常或返回null/空列表
            // throw new RuntimeException("JSON parsing error", e);
            return Collections.emptyList();
        }
    }

}
