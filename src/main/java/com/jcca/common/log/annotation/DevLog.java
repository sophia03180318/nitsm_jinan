package com.jcca.common.log.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author HanHW
 * @description 运维日志
 * @className DevLog
 * @date 2024/4/8 14:07
 * @since 2.1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface DevLog {
    // 模块名称
    String title() default "";

    // 日志名称
    String name() default "";

    // 行为key LogTypeConstant
    String key() default "";

    // 运维日志标识  DevLogConstant
    String dev() default "";
}
