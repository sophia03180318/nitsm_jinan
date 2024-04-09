package com.jcca.web.xunjian.enums;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

/**
 * 巡检指标
 *
 * @author Lvyp
 */
@Getter
public enum XunJianTargetEnum {

    CPU_USED_RATE("1", "CPU使用率"),

    MEMORY_RATE("2", "内存使用率"),

    DISK_RATE("3", "磁盘使用率"),

    NET_CARD("4", "网卡状态"),

    INTERFACE_FLOW_IN("5", "端口流入率"),

    INTERFACE_FLOW_OUT("6", "端口流出率"),

    PROCESS_RUN_STATUS("7", "监控进程状态"),

    ALARM("8", "告警信息"),

    RUN_TIME("9", "运行时长"),

    ORACLE("10", "Oracle连接状态");


    private String code;
    private String msg;

    private XunJianTargetEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    private static String getMsgByCode(String code) {
        XunJianTargetEnum[] values = XunJianTargetEnum.values();
        for (XunJianTargetEnum item : values) {
            if (item.getCode().equals(code)) {
                return item.getMsg();
            }
        }
        return code;
    }

    public static String getNameByValues(List<String> values) {
        if (Objects.isNull(values) || values.isEmpty()) {
            return "";
        }
        StringBuilder msg = new StringBuilder();
        for (String code : values) {
            String xunjian = getMsgByCode(code);
            if (StrUtil.isNotEmpty(xunjian)) {
                msg.append(xunjian);
                msg.append(",");
            }

        }
        String mesgStr = msg.toString();
        if (mesgStr.length() == 0) {
            return mesgStr;
        }
        return mesgStr.substring(0, mesgStr.length() - 1);
    }

}
