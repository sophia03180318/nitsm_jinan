package com.jcca.web.asset.utils;

import cn.hutool.core.util.ObjectUtil;
import com.jcca.poi.ss.formula.functions.T;
import com.jcca.poi.xssf.streaming.SXSSFWorkbook;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import static cn.hutool.core.map.MapUtil.isEmpty;

/**
 * @ Author：sophia
 * @ Date：Created in 11:25 2021/7/8
 * @ Description:
 */
@Slf4j
public class AssetImportUtils {

    /*根据属性名称获取对象属性值    为null返回 "" */
    public static String getFieldValues(Object obj, String name) throws NoSuchFieldException, IllegalAccessException {
        Class stuCla = (Class) obj.getClass();
        //    String methodName = "get"+name.substring(0, 1).toUpperCase() + name.substring(1);
        Field getMethod = stuCla.getDeclaredField(name.trim());
        getMethod.setAccessible(true);
        Object value = getMethod.get(obj);
        if (ObjectUtil.isNotNull(value)) {
            return (String) getMethod.get(obj);
        } else {
            return "";
        }


    }


    /*给指定对象某个属性进行赋值操作*/
    public static void setFieldValues(Object obj, String name, String value) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class stuCla = (Class) obj.getClass();
        //Field[] f1 = stuCla.getDeclaredFields()
        String methodName = name.substring(0, 1).toUpperCase() + name.substring(1);
        Method setMethod = stuCla.getDeclaredMethod("set" + methodName, String.class);
        setMethod.setAccessible(true);
        //执行该set方法
        setMethod.invoke(obj, value);

    }

    public static void setFieldValues(Object obj, String name, Integer value) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class stuCla = (Class) obj.getClass();
        //Field[] f1 = stuCla.getDeclaredFields()
        String methodName = name.substring(0, 1).toUpperCase() + name.substring(1);
        Method setMethod = stuCla.getDeclaredMethod("set" + methodName, Integer.class);
        setMethod.setAccessible(true);
        //执行该set方法
        setMethod.invoke(obj, value);

    }

    public static void setFieldValues(Object obj, String name, Byte value) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class stuCla = (Class) obj.getClass();
        //Field[] f1 = stuCla.getDeclaredFields()
        String methodName = name.substring(0, 1).toUpperCase() + name.substring(1);
        Method setMethod = stuCla.getDeclaredMethod("set" + methodName, Byte.class);
        setMethod.setAccessible(true);
        //执行该set方法
        setMethod.invoke(obj, value);

    }

    public static void setFieldValues(Object obj, String name, Date value) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class stuCla = (Class) obj.getClass();
        //Field[] f1 = stuCla.getDeclaredFields()
        String methodName = name.substring(0, 1).toUpperCase() + name.substring(1);
        Method setMethod = stuCla.getDeclaredMethod("set" + methodName, Date.class);
        setMethod.setAccessible(true);
        //执行该set方法
        setMethod.invoke(obj, value);

    }

    /*给指定对象一列相应属性进行赋值操作*/
    public static void setFieldValues(Object obj, Map<String, T> nameValueMap) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class stuCla = (Class) obj.getClass();// 得到类对象
        //Field[] f1 = stuCla.getDeclaredFields();//得到属性集合

        for (Map.Entry<String, T> nameValue : nameValueMap.entrySet()) {
            String methodName = nameValue.getKey().substring(0, 1).toUpperCase() + nameValue.getKey().substring(1);//

            //获取Test类当前属性的setXXX方法（私有和公有方法）
            /*Method setMethod=stuCla.getDeclaredMethod("set"+methodName);*/
            //获取Test类当前属性的setXXX方法（只能获取公有方法）
            Method setMethod = null;
            setMethod = stuCla.getMethod("set" + methodName, String.class);
            setMethod.setAccessible(true);
            //执行该set方法
            setMethod.invoke(obj, nameValue.getValue());
        }
    }

    public static <K, V> Map<K, V> removeMapNullValue(Map<K, V> map) {
        if (isEmpty(map)) {
            return map;
        } else {
            Iterator iter = map.entrySet().iterator();

            while (iter.hasNext()) {
                Map.Entry<K, V> entry = (Map.Entry) iter.next();
                try {
                    if (null == entry.getValue() || entry.getValue().toString().trim().isEmpty()) {
                        iter.remove();
                    }
                } catch (NullPointerException e) {
                    iter.remove();
                }

            }

            return map;
        }
    }

    public static void responseBody(SXSSFWorkbook workbook, HttpServletResponse response, String name) {
        // response
        try {
            String fileName = URLEncoder.encode(name + ".xlsx", "UTF-8");
            response.setContentType("application/msexcel;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            ServletOutputStream out = response.getOutputStream();
            workbook.write(out);
            out.close();
            workbook.dispose();
            workbook.close();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public static void responseBody2(SXSSFWorkbook workbook, HttpServletResponse response, String name) {
        // response
        try {
            String fileName = URLEncoder.encode(name + ".xls", "UTF-8");
            response.setContentType("application/msexcel;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            ServletOutputStream out = response.getOutputStream();
            workbook.write(out);
            out.close();
            workbook.dispose();
            workbook.close();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }


}
