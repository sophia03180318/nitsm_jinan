package com.jcca.common.utils;

import cn.hutool.json.JSONObject;
import com.jcca.common.annotation.WebField;

import java.lang.reflect.Field;
import java.util.ArrayList;

import java.util.List;


/**
 * WebField注解处理工具类，用于将标注了@WebField的字段生成指定格式的JSON
 */
public class WebTitleUtils {

    /**
     * 将对象中所有标注了@WebField注解的字段转换为JSON数组字符串
     *
     * @param item 要处理的对象
     * @return 生成的JSON数组字符串
     */
    public static List<JSONObject> getWebTitleList(Class<?> item) {
        if (item == null) {
            return new ArrayList<>();
        }

        // 获取对象的所有字段
        Field[] fields = item.getDeclaredFields();
        List<JSONObject> resultList = new ArrayList<>();

        for (Field field : fields) {
            // 检查字段是否标注了@WebField注解
            if (field.isAnnotationPresent(WebField.class)) {
                // 获取注解实例
                WebField webField = field.getAnnotation(WebField.class);

                // 创建映射关系
                JSONObject itsm = new JSONObject();
                itsm.put("property", field.getName());
                itsm.put("title", webField.title());

                resultList.add(itsm);
            }
        }
        return resultList;
    }


}