package com.jcca.common.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.web2.constant.Web2Const;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * 日志工具
 *
 * @author Manager
 */
public class AppLogUtils {

    @Deprecated
    public static String logStr(String prefix, String suffix, String data) {
        StringBuilder logBuilder = new StringBuilder("[");
        logBuilder.append(prefix);
        logBuilder.append("-");
        logBuilder.append(suffix);
        logBuilder.append("]:");
        logBuilder.append(data);
        return logBuilder.toString();
    }

    public static String SPLIT = "|";

    private static Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    private static LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
    private static Logger logger = loggerContext.getLogger(Web2Const.PACKAGE_NAME);

    /**
     * 获取 Gson
     *
     * @return Gson
     */
    public static Gson getGson() {
        return gson;
    }

    /**
     * @description: 异常日志消息
     * @author: HanHW
     * @date: 2023/10/9 11:03
     * @param: [functionEnum：功能明细枚举, param：参数, e：堆栈消息]
     * @return: void
     * <p>
     * 使用示例：AppLogUtils.buildLogException(LogFunctionEnum.IP_MANAGE_NEW, req, new Exception("测试测试"));
     **/
    public static void buildLogError(LogFunctionEnum functionEnum, Object param, Throwable e) {
        if (Objects.isNull(param)) {
            param = "无";
        }

        String msg;
        if (param instanceof String) {
            msg = param.toString();
        } else {
            msg = gson.toJson(param);
        }

        StackTraceElement[] stackTrace = new Exception().getStackTrace();
        StackTraceElement stackTraceElement = stackTrace[1];
        String clazm = stackTraceElement.getClassName();
        String className = clazm.substring(clazm.lastIndexOf(".") + 1);
        String methodName = stackTraceElement.getMethodName();
        String fix = functionEnum.getFunction() + SPLIT + className + SPLIT + methodName + SPLIT + msg + SPLIT;
        logger.error(fix, e);
    }

    /**
     * ERROR 日志
     *
     * @param functionEnum LogFunctionEnum
     * @param param        参数
     * @param content      内容
     */
    public static void buildLogError(LogFunctionEnum functionEnum, Object param, Object content) {
        buildLogNormal(functionEnum, Level.ERROR, param, content);
    }

    /**
     * INFO 日志
     *
     * @param functionEnum LogFunctionEnum
     * @param param        参数
     * @param content      内容
     */
    public static void buildLogInfo(LogFunctionEnum functionEnum, Object param, Object content) {
        buildLogNormal(functionEnum, Level.INFO, param, content);
    }

    /**
     * WARN 日志
     *
     * @param functionEnum LogFunctionEnum
     * @param param        参数
     * @param content      内容
     */
    public static void buildLogWarn(LogFunctionEnum functionEnum, Object param, Object content) {
        buildLogNormal(functionEnum, Level.WARN, param, content);
    }

    /**
     * DEBUG 日志
     *
     * @param functionEnum LogFunctionEnum
     * @param param        参数
     * @param content      内容
     */
    public static void buildLogDebug(LogFunctionEnum functionEnum, Object param, Object content) {
        buildLogNormal(functionEnum, Level.DEBUG, param, content);
    }

    /**
     * @description: 手动普通日志消息
     * @author: HanHW
     * @date: 2023/10/9 11:03
     * @param: [functionEnum：功能明细枚举, logLevel：日志级别, param：参数, content：消息]
     * @return: void
     * <p>
     * 使用示例：AppLogUtils.buildLogNormal(LogFunctionEnum.IP_MANAGE_NEW,  req, "新建网络");
     **/
    private static void buildLogNormal(LogFunctionEnum functionEnum, Level logLevel, Object param, Object content) {

        Integer o = Web2Const.FUNCTION_LOG_MAP.get(functionEnum.getCode());
        if (Objects.isNull(o) || 0 == o) {
            // 已关闭的功能不打印日志
            return;
        }

        if (Objects.isNull(param)) {
            param = "无";
        }
        if (Objects.isNull(content)) {
            content = "无";
        }

        String msg;
        if (param instanceof String) {
            msg = param.toString();
        } else {
            msg = gson.toJson(param);
        }

        String cont;
        if (content instanceof String) {
            cont = content.toString();
        } else {
            cont = gson.toJson(content);
        }

        StackTraceElement[] stackTrace = new Exception().getStackTrace();
        StackTraceElement stackTraceElement = stackTrace[2];
        String methodName = stackTraceElement.getMethodName();
        String className = stackTraceElement.getClassName();
        className = className.substring(className.lastIndexOf(".") + 1);
        String fix = functionEnum.getFunction() + SPLIT + className + SPLIT + methodName + SPLIT + msg + SPLIT + cont;
        switch (logLevel.levelStr) {
            case "OFF":
                break;
            case "ERROR":
                logger.error(fix);
                break;
            case "WARN":
                logger.warn(fix);
                break;
            case "DEBUG":
                logger.debug(fix);
                break;
            case "TRACE":
                logger.trace(fix);
                break;
            case "INFO":
            default:
                logger.info(fix);
                break;
        }
    }

}
