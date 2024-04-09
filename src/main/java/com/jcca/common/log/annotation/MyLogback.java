package com.jcca.common.log.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author HanHW
 * @description logback日志拦截
 * @className MyLogback
 * @date 2023/9/22 11:00
 * @since 2.1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MyLogback {

    String code() default "";

    String function() default "";

    String action() default "";
}
