package com.hb.dokkan.common.utils;

import cn.hutool.core.io.FileUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
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
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Description json工具类
 * @Author stargazer
 * @Date 2025/3/22 23:04
 **/
@UtilityClass
@Slf4j
public class JsonUtils {

    /** Jackson ObjectMapper 统一实例，避免业务代码直接依赖第三方 JSON API。 */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /** JSON 数组首个元素下标，用于读取外部接口首选结果。 */
    private static final int FIRST_ARRAY_INDEX = 0;

    /**
     * 将对象写入 JSON 文件，文件不存在时自动创建。
     *
     * @param value    待写入对象
     * @param filePath 文件路径
     */
    public static void writeJson2File(Object value, String filePath) {
        if (ObjectUtils.isEmpty(value) || StringUtils.isBlank(filePath)) {
            return;
        }
        writeJson2File(value, new File(filePath));
    }

    /**
     * 将对象写入 JSON 文件，文件不存在时自动创建。
     *
     * @param value 待写入对象
     * @param path  文件路径
     */
    public static void writeJson2File(Object value, Path path) {
        if (ObjectUtils.isEmpty(value) || Objects.isNull(path)) {
            return;
        }
        writeJson2File(value, path.toFile());
    }

    /**
     * 将对象写入 JSON 文件，异常时抛出系统异常。
     *
     * @param value 待写入对象
     * @param file  目标文件
     */
    public static void writeJson2File(Object value, File file) {
        if (ObjectUtils.isEmpty(value) || Objects.isNull(file)) {
            return;
        }
        if (!FileUtil.exist(file)) {
            FileUtil.touch(file);
        }
        try {
            OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(file, value);
        } catch (IOException e) {
            log.error("json write file fail, file={}", file.getAbsolutePath(), e);
            throw new DokkanSysException(e.getMessage());
        }
    }

    /**
     * 将 JSON 字符串转换为 Map 列表，空字符串返回空列表。
     *
     * @param json      JSON 字符串
     * @param keyType   Map key 类型
     * @param valueType Map value 类型
     * @param <K>       key 泛型
     * @param <V>       value 泛型
     * @return Map 列表
     */
    public static <K, V> List<Map<K, V>> json2ListMap(String json, Class<K> keyType, Class<V> valueType) {
        if (StringUtils.isBlank(json)) {
            return Lists.newArrayList();
        }
        MapType mapType = OBJECT_MAPPER.getTypeFactory().constructMapType(Map.class, keyType, valueType);
        CollectionType collectionType = OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, mapType);
        try {
            return OBJECT_MAPPER.readValue(json, collectionType);
        } catch (JsonProcessingException e) {
            log.error("json convert to list map fail, json={}", json, e);
            throw new DokkanSysException(e.getMessage());
        }
    }

    /**
     * 将 JSON 字符串转换为 Map，空字符串返回空 Map。
     *
     * @param jsonStr   JSON 字符串
     * @param keyType   Map key 类型
     * @param valueType Map value 类型
     * @param <K>       key 泛型
     * @param <V>       value 泛型
     * @return Map 结果
     */
    public static <K, V> Map<K, V> json2Map(String jsonStr, Class<K> keyType, Class<V> valueType) {
        if (StringUtils.isBlank(jsonStr)) {
            return Maps.newHashMap();
        }
        MapType mapType = OBJECT_MAPPER.getTypeFactory().constructMapType(Map.class, keyType, valueType);
        try {
            return OBJECT_MAPPER.readValue(jsonStr, mapType);
        } catch (JsonProcessingException e) {
            log.error("json convert to map fail, json={}", jsonStr, e);
            throw new DokkanSysException(e.getMessage());
        }
    }

    /**
     * 将 JSON 字符串转换为指定对象，空字符串返回 null。
     *
     * @param jsonStr JSON 字符串
     * @param clazz   目标类型
     * @param <T>     目标泛型
     * @return 转换后的对象
     */
    public static <T> T json2Object(String jsonStr, Class<T> clazz) {
        if (StringUtils.isBlank(jsonStr) || ObjectUtils.isEmpty(clazz)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(jsonStr, clazz);
        } catch (JsonProcessingException e) {
            log.error("json convert to object fail, json={}, target={}", jsonStr, clazz.getName(), e);
            throw new DokkanSysException(e.getMessage());
        }
    }

    /**
     * 将 JSON 字符串转换为指定类型列表，解析失败时返回空列表。
     *
     * @param jsonString   JSON 字符串
     * @param elementClass 列表元素类型
     * @param <T>          元素泛型
     * @return 对象列表
     */
    public static <T> List<T> json2List(String jsonString, Class<T> elementClass) {
        if (StringUtils.isBlank(jsonString) || Objects.isNull(elementClass)) {
            return Collections.emptyList();
        }
        try {
            CollectionType listType = OBJECT_MAPPER.getTypeFactory()
                    .constructCollectionType(List.class, elementClass);
            return OBJECT_MAPPER.readValue(jsonString, listType);
        } catch (JsonProcessingException e) {
            log.error("json convert to list fail, target={}", elementClass.getName(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 将对象序列化为 JSON 字符串，空对象返回 null。
     *
     * @param value 待序列化对象
     * @return JSON 字符串
     */
    public static String object2Json(Object value) {
        if (Objects.isNull(value)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.error("object convert to json fail, type={}", value.getClass().getName(), e);
            throw new DokkanSysException(e.getMessage());
        }
    }

    /**
     * 从输入流读取 JSON Map，空输入流返回空 Map。
     *
     * @param input     JSON 输入流
     * @param keyType   Map key 类型
     * @param valueType Map value 类型
     * @param <K>       key 泛型
     * @param <V>       value 泛型
     * @return Map 结果
     */
    public static <K, V> Map<K, V> inputStream2Map(InputStream input, Class<K> keyType, Class<V> valueType) {
        if (Objects.isNull(input)) {
            return Collections.emptyMap();
        }
        MapType mapType = OBJECT_MAPPER.getTypeFactory().constructMapType(Map.class, keyType, valueType);
        try {
            return OBJECT_MAPPER.readValue(input, mapType);
        } catch (IOException e) {
            log.error("json input stream convert to map fail", e);
            throw new DokkanSysException(e.getMessage());
        }
    }

    /**
     * 从文件读取 JSON 对象，文件内容为空时返回 null。
     *
     * @param path  JSON 文件路径
     * @param clazz 目标类型
     * @param <T>   目标泛型
     * @return 读取后的对象
     */
    public static <T> T file2Object(Path path, Class<T> clazz) {
        if (Objects.isNull(path) || Objects.isNull(clazz)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(path.toFile(), clazz);
        } catch (IOException e) {
            log.error("json file convert to object fail, path={}, target={}", path, clazz.getName(), e);
            throw new DokkanSysException(e.getMessage());
        }
    }

    /**
     * 从文件读取 JSON Map，空路径返回空 Map。
     *
     * @param path JSON 文件路径
     * @return String 到 Object 的 Map
     */
    public static Map<String, Object> file2StringObjectMap(Path path) {
        if (Objects.isNull(path)) {
            return Collections.emptyMap();
        }
        MapType mapType = OBJECT_MAPPER.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
        try {
            return OBJECT_MAPPER.readValue(path.toFile(), mapType);
        } catch (IOException e) {
            log.error("json file convert to string object map fail, path={}", path, e);
            throw new DokkanSysException(e.getMessage());
        }
    }

    /**
     * 将对象转换为 String Map，转换失败时抛出系统异常。
     *
     * @param value 待转换对象
     * @return String Map
     */
    public static Map<String, String> convert2StringMap(Object value) {
        if (Objects.isNull(value)) {
            return Collections.emptyMap();
        }
        MapType mapType = OBJECT_MAPPER.getTypeFactory().constructMapType(Map.class, String.class, String.class);
        return OBJECT_MAPPER.convertValue(value, mapType);
    }

    /**
     * 将对象转换为指定类型，常用于经过 JSON 工具读取后的结构转换。
     *
     * @param value 待转换对象
     * @param clazz 目标类型
     * @param <T>   目标泛型
     * @return 转换后的对象
     */
    public static <T> T convertValue(Object value, Class<T> clazz) {
        if (Objects.isNull(value) || Objects.isNull(clazz)) {
            return null;
        }
        return OBJECT_MAPPER.convertValue(value, clazz);
    }

    /**
     * 读取 JSON 字符串中指定对象数组的字段文本，并按数组顺序返回。
     *
     * @param json       JSON 字符串
     * @param arrayField 数组字段名
     * @param textField  数组元素中的文本字段名
     * @return 字段文本列表
     */
    public static List<String> readArrayFieldTexts(String json, String arrayField, String textField) {
        if (StringUtils.isAnyBlank(json, arrayField, textField)) {
            return Collections.emptyList();
        }
        try {
            JsonNode arrayNode = OBJECT_MAPPER.readTree(json).path(arrayField);
            if (!arrayNode.isArray()) {
                return Collections.emptyList();
            }
            List<String> values = Lists.newArrayList();
            for (JsonNode node : arrayNode) {
                values.add(node.path(textField).asText(StringUtils.EMPTY));
            }
            return values;
        } catch (JsonProcessingException e) {
            log.error("json read array field texts fail, arrayField={}, textField={}", arrayField, textField, e);
            throw new DokkanSysException(e.getMessage());
        }
    }

    /**
     * 读取 JSON 字符串中首个数组元素下对象字段的文本。
     *
     * @param json        JSON 字符串
     * @param arrayField  数组字段名
     * @param objectField 数组首元素下的对象字段名
     * @param textField   文本字段名
     * @return 文本内容，不存在时返回 null
     */
    public static String readFirstArrayObjectText(String json, String arrayField, String objectField, String textField) {
        if (StringUtils.isAnyBlank(json, arrayField, objectField, textField)) {
            return null;
        }
        try {
            JsonNode content = OBJECT_MAPPER.readTree(json)
                    .path(arrayField)
                    .path(FIRST_ARRAY_INDEX)
                    .path(objectField)
                    .path(textField);
            return content.isTextual() ? content.asText() : null;
        } catch (JsonProcessingException e) {
            log.error("json read first array object text fail, arrayField={}, objectField={}, textField={}",
                    arrayField, objectField, textField, e);
            throw new DokkanSysException(e.getMessage());
        }
    }
}
