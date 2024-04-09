package com.jcca.web.ibmMQ.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;


@ThreadSafe
public final class BeanUtil {
    private static final Logger log = LoggerFactory.getLogger(BeanUtil.class);
    private static final String IS_PREFIX = "is";
    private static final String GETTER_PREFIX = "get";
    private static final String SETTER_PREFIX = "set";


    public static Method forSetter(Field field) throws Exception {

        return field.getDeclaringClass().getDeclaredMethod("set" +
                Strings.capitalize(field.getName()), new Class[]{field.getType()});

    }


    public static Method forGetter(Field field) throws Exception {
        StringBuffer methodName = new StringBuffer();
        if (field.getType() == boolean.class) {
            methodName.append("is");
        } else {
            methodName.append("get");
        }
        return field.getDeclaringClass().getDeclaredMethod(methodName.append(Strings.capitalize(field.getName())).toString(), new Class[0]);
    }


    public static Object getValue(Object obj, Field field) throws Exception {
        return field.get(obj);
    }


    public static Object getValueWithGetter(Object obj, Field field) throws Exception {
        return forGetter(field).invoke(obj, new Object[0]);
    }


    public static void setValue(Object obj, Field field, Object value) throws Exception {
        field.setAccessible(true);
        field.set(obj, convert(field.getType(), value));
    }


    public static void setValueWithSetter(Object obj, Field field, Object value) throws Exception {
        forSetter(field).invoke(obj, new Object[]{convert(field.getType(), value)});
    }


    public static List<Field> getFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<Field>();
        Field[] field = clazz.getDeclaredFields();
        for (Field f : field) {

            if (!Modifier.isStatic(f.getModifiers())) {

                fields.add(f);
            }
        }
        return fields;
    }


    public static List<Field> getFields(Class<?> clazz, boolean includeSuperClass) {
        if (includeSuperClass) {
            return getAllFields(clazz);
        }
        return getFields(clazz);
    }

    /*     */
    /*     */
    /*     */
    /*     */
    /*     */
    /*     */
    /*     */
    /*     */
    private static List<Field> getAllFields(Class<?> clazz) {
        /* 144 */
        List<Field> fields = new ArrayList<Field>();
        /* 145 */
        for (; !clazz.equals(Object.class); clazz = clazz.getSuperclass()) {
            /* 146 */
            Field[] field = clazz.getDeclaredFields();
            /* 147 */
            for (Field f : field) {
                /*     */
                /* 149 */
                if (!Modifier.isStatic(f.getModifiers()))
                    /*     */ {
                    /*     */
                    /* 152 */
                    fields.add(f);
                }
                /*     */
            }
            /*     */
        }
        /* 155 */
        return fields;
        /*     */
    }

    /*     */
    /*     */
    /*     */
    /*     */
    /*     */
    /*     */
    /*     */
    /*     */
    /*     */
    private static Object convert(Class<?> returnType, Object value) {
        /* 166 */
        if (log.isDebugEnabled()) {
            /* 167 */
            log.debug("Convert value '{}' to type '{}' ", value, returnType);
            /*     */
        }
        /* 169 */
        String strValue = "" + value;
        /*     */
        /* 171 */
        if (returnType.equals(boolean.class)) {
            /* 172 */
            return Boolean.valueOf(Boolean.parseBoolean(strValue));
            /*     */
        }
        /* 174 */
        if (returnType.equals(short.class)) {
            /* 175 */
            return Short.valueOf(Short.parseShort(strValue));
            /*     */
        }
        /* 177 */
        if (returnType.equals(int.class)) {
            /* 178 */
            return Integer.valueOf(Integer.parseInt(strValue));
            /*     */
        }
        if (returnType.equals(long.class)) {
            return Long.valueOf(Long.parseLong(strValue));
        }
        if (returnType.equals(float.class)) {
            return Float.valueOf(Float.parseFloat(strValue));
        }
        if (returnType.equals(double.class)) {
            return Double.valueOf(Double.parseDouble(strValue));
        }
        return value;
    }
}
