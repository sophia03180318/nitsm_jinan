package com.jcca.common.log.annotation;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.google.gson.Gson;
import com.jcca.common.exception.ResultException;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.web2.constant.Web2Const;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author HanHW
 * @description 方法执行链路
 * @className MyLogbackAop
 * @date 2023/9/22 11:01
 * @since 2.1.0.0
 */
@Aspect
@Component
public class MyLogbackAop {

    private Gson gson = new Gson();
    private LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

    @Pointcut("@annotation(com.jcca.common.log.annotation.MyLogback)")
    public void myLogback() {

    }

    @AfterReturning(value = "myLogback()", returning = "returnStr")
    public void recordLog(JoinPoint point, Object returnStr) {
        try {
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method targetMethod = signature.getMethod();
            Class<?> aClass = targetMethod.getDeclaringClass();
            MyLogback anno = targetMethod.getAnnotation(MyLogback.class);
            String methodName = targetMethod.getName();

            LogFunctionEnum function = LogFunctionEnum.getByCode(anno.code());
            if (Objects.isNull(function)) {
                return;
            }
            this.buildClassLog(function, aClass, methodName,
                    gson.toJson(point.getArgs()), gson.toJson(returnStr));
        } catch (ResultException e) {
            AppLogUtils.buildLogError(LogFunctionEnum.LOG_CONTROL, "注解日志打印异常", e);
        }
    }

    /**
     * @description: 方法注解日志打印
     * @author: HanHW
     * @date: 2023/10/10 14:18
     * @param: [functionEnum：功能项, claz：类名, methodName：方法名, param：参数, returnStr：返回值]
     * @return: void
     **/
    private void buildClassLog(LogFunctionEnum functionEnum, Class<?> claz, String methodName, String param, String returnStr) {
        Integer o = Web2Const.FUNCTION_LOG_MAP.get(functionEnum.getCode());
        if (Objects.isNull(o) || 0 == o) {
            // 已关闭的功能不打印日志
            return;
        }
        if (Objects.isNull(param)) {
            param = "无";
        }
        if (Objects.isNull(returnStr)) {
            returnStr = "无";
        }

        Logger logger = loggerContext.getLogger(claz);
        String className = claz.getSimpleName();
        String fix = functionEnum.getFunction() + AppLogUtils.SPLIT + className + AppLogUtils.SPLIT + methodName
                + AppLogUtils.SPLIT + param + AppLogUtils.SPLIT + returnStr;
        logger.info(fix);
    }

}
