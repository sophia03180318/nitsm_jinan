package com.jcca.web.ibmMQ.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PCFParam {
    int value() default -1;

    int option() default 0;

    int[] supportType() default {};

    Class<?> supportTypeClass() default Default.class;

    String description() default "";

    public static class Option {
        public static final int DEFAULT = 0;

        public static final int MEASURABLE = 11;

        public static final int CALCULATED = 12;
    }

    public static class Default {
        public static final int UNDEFINED = -1;
    }
}
