package com.jcca.common.log.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author HanHW
 * @description 字段名
 * @className FieldLogAnno
 * @date 2024/4/9 17:34
 * @since 2.1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface FieldLogAnno {

    // 字段名称
    String title() default "";

}
