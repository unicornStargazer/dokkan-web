package com.hb.dokkan.common.utils;

import com.alibaba.fastjson.JSON;
import com.github.houbb.opencc4j.util.ZhConverterUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class TranslationUtils {

    public static void main(String[] args) {
        String str = "123数据撒大大llll";
        String simpleChinese = toSimpleChinese(str);
        System.out.println(simpleChinese);
    }

    public static <T> T toSimpleChinese(T traditon){
        if (ObjectUtils.isEmpty(traditon)){
            throw new RuntimeException("traditon is null");
        }
        String jsonString = JSON.toJSONString(traditon);
        String simple = ZhConverterUtil.toSimple(jsonString);
        Object parsedObject = JSON.parseObject(simple, traditon.getClass());
        return (T) parsedObject;
    }


    public static <T> List<T> toSimpleChinese(List<T> traditonList){
        if (CollectionUtils.isEmpty(traditonList)){
            throw new RuntimeException("traditonList is empty");
        }
        String jsonString = JSON.toJSONString(traditonList);
        String simple = ZhConverterUtil.toSimple(jsonString);
        List<T> simpleList = (List<T>) JSON.parseArray(simple, traditonList.get(0).getClass());
        return simpleList;
    }
}
