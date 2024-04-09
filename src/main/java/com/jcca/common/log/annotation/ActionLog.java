package com.jcca.common.log.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 行为日志注解
 *
 * @author 小白龙
 * @date 2018/11/12
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ActionLog {
    // 模块名称
    String title() default "";

    // 日志名称
    String name() default "";

    // 日志消息
    String message() default "";

    // 行为key
    String key() default "";
}
