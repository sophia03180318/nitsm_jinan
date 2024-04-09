package com.jcca.common.input;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 日志打印控制工具
 */
@Deprecated
public class LogInputUtils {

    /**
     * 日志是否开启配置
     */
    public static Map<String, Boolean> logInfoStatusMap = new HashMap<String, Boolean>();
    public static Map<String, Boolean> logErrorStatusMap = new HashMap<String, Boolean>();

    static {
        ServerTypeEnum[] serverTypes = ServerTypeEnum.values();
        for (ServerTypeEnum serverType : serverTypes) {
            if(ServerTypeEnum.SYSTEM_INIT == serverType){
                logInfoStatusMap.put(serverType.name(), true);
            }else{
                logInfoStatusMap.put(serverType.name(), false);
            }

            logErrorStatusMap.put(serverType.name(), true);
        }
    }

    /**
     * 是否开启打印INFO日志
     *
     * @param enumType 业务类型
     * @return
     */
    public static Boolean inputInfo(ServerTypeEnum enumType) {
        Boolean result = logInfoStatusMap.get(enumType.name());
        if (Objects.isNull(result)) {
            result = false;
        }
        return result;
    }

    /**
     * 是否开启error 日志
     *
     * @param enumType
     * @return
     */
    public static Boolean inputError(ServerTypeEnum enumType) {
        Boolean result = logErrorStatusMap.get(enumType.name());
        if (Objects.isNull(result)) {
            result = false;
        }

        return result;
    }

    /**
     * 格式化INFO类型日志
     *
     * @param enumType
     * @param assetIp
     * @param msg
     * @return
     */
    public static String formattingInfoLog(ServerTypeEnum enumType, String assetIp, String msg) {
        StringBuilder str = new StringBuilder("");
        str.append("日志代码[");
        str.append(enumType.getCode());
        str.append("]");
        str.append("业务模块[");
        str.append(enumType.getMsg());
        str.append("]");
        if (StrUtil.isNotEmpty(assetIp)) {
            str.append("设备:[");
            str.append(assetIp);
            str.append("]");
        }
        str.append("于");
        str.append(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        str.append("记录日志：");
        str.append(msg);
        return str.toString();
    }

    /**
     * 格式化INFO类型日志
     *
     * @param enumType
     * @param assetIp
     * @param errorMsg
     * @return
     */
    public static String formattingErrorLog(ServerTypeEnum enumType, ErrorCodeEnum errorEnum, String assetIp, String errorMsg) {
        StringBuilder str = new StringBuilder("");
        str.append("日志代码[");
        str.append(enumType.getCode());
        str.append("]");
        str.append("业务模块[");
        str.append(enumType.getMsg());
        str.append("]");
        if (StrUtil.isNotEmpty(assetIp)) {
            str.append("设备IP:[");
            str.append(assetIp);
            str.append("]");
        }
        str.append("于");
        str.append(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        str.append("发生错误:[");
        str.append(errorEnum.name());
        str.append("]");

        if (StrUtil.isNotEmpty(errorMsg)) {
            str.append("记录异常信息：[");
            str.append(errorMsg);
            str.append("]");
        }
        str.append("自定义提示信息：[");
        str.append(errorEnum.getDescStr());
        str.append("]");
        str.append("自定义解决方案：[");
        str.append(errorEnum.getOperationStr());
        str.append("]");

        return str.toString();
    }


}
