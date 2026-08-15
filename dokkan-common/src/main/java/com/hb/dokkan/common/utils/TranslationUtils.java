package com.hb.dokkan.common.utils;

import com.github.houbb.opencc4j.util.ZhConverterUtil;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @Description 中文翻译工具类
 * @Author stargazer
 * @Date 2025/12/13 21:42
 **/
public final class TranslationUtils {

    /**
     * 工具类禁止实例化。
     */
    private TranslationUtils() {
    }

    /**
     * 将文本中的繁体中文转换为简体中文，空文本返回原值。
     *
     * @param traditionText 待转换文本
     * @return 简体中文文本
     */
    public static String toSimpleChineseText(String traditionText) {
        if (Objects.isNull(traditionText)) {
            return null;
        }
        return ZhConverterUtil.toSimple(traditionText);
    }

    /**
     * 将对象中的繁体中文内容转换为简体中文，空对象返回 null。
     *
     * @param tradition 待转换对象
     * @param <T>       对象泛型
     * @return 简体中文对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T toSimpleChinese(T tradition) {
        if (Objects.isNull(tradition)) {
            return null;
        }
        String jsonString = JsonUtils.object2Json(tradition);
        String simpleChinese = ZhConverterUtil.toSimple(jsonString);
        return JsonUtils.json2Object(simpleChinese, (Class<T>) tradition.getClass());
    }

    /**
     * 将对象列表中的繁体中文内容转换为简体中文，空列表返回空集合。
     *
     * @param traditionList 待转换对象列表
     * @param <T>           对象泛型
     * @return 简体中文对象列表
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> toSimpleChinese(List<T> traditionList) {
        if (CollectionUtils.isEmpty(traditionList)) {
            return Collections.emptyList();
        }
        String jsonString = JsonUtils.object2Json(traditionList);
        String simpleChinese = ZhConverterUtil.toSimple(jsonString);
        return JsonUtils.json2List(simpleChinese, (Class<T>) traditionList.getFirst().getClass());
    }
}
