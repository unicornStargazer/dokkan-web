package com.hb.dokkan.common.utils;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hb.dokkan.common.exception.domain.DokkanSysException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
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


    public static void writeJson2File(Object value, String filePath) {
        if (ObjectUtils.isEmpty(value) || StringUtils.isBlank(filePath)) {
            return;
        }
        File file = new File(filePath);
        if (!FileUtil.exist(file)) {
            FileUtil.touch(file);
        }
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file,value);
        } catch (IOException e) {
            log.error("json write file, object:{} e", JSON.toJSONString(value),e);
            throw new DokkanSysException(e.getMessage());
        }
    }

    public static <K,V> List<Map<K,V>> json2ListMap(String json,Class<K> keyType, Class<V> valueType) {
        if (StringUtils.isBlank(json)) {
            return Lists.newArrayList();
        }
        MapType mapType = objectMapper.getTypeFactory().constructMapType(Map.class, keyType, valueType);
        CollectionType collectionType = objectMapper.getTypeFactory().constructCollectionType(List.class, mapType);
        List<Map<K,V>> result;
        try {
            result = objectMapper.readValue(json,collectionType);
        } catch (JsonProcessingException e) {
            log.error("json convert fail, json:{} e",json,e);
            throw new DokkanSysException(e.getMessage());
        }
        return result;
    }

    public static <K,V> Map<K,V> json2Map(String jsonStr, Class<K> keyType, Class<V> valueType) {
        if (StringUtils.isBlank(jsonStr)) {
            return Maps.newHashMap();
        }
        MapType mapType = objectMapper.getTypeFactory().constructMapType(Map.class, keyType, valueType);
        Map<K, V> result = null;
        try {
            result = objectMapper.readValue(jsonStr,mapType);
        } catch (JsonProcessingException e) {
            log.error("json convert fail, json:{} e",jsonStr,e);
            throw new DokkanSysException(e.getMessage());
        }
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
