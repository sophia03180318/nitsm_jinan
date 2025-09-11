package com.jcca.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义WebField注解，用于标记需要生成JSON的字段
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WebField {
    /**
     * 前端显示的标题
     * @return string
     */
    public String title();
}