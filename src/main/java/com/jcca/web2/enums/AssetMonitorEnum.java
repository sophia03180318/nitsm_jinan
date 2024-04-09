package com.jcca.web2.enums;

/**
 * @author HanHW
 * @description 资产监控状态常量
 * @className AssetMonitorEnum
 * @date 2023/12/6 14:40
 * @since 2.1.0.0
 */
public enum AssetMonitorEnum {

    ABNORMAL(0, "异常"),
    NORMAL(1, "正常"),
    UNKNOWN(2, "未知"),
    UNMONITOR(3, "不监控"),
    TEMP_STATE(4, "临时状态"),

    ;


    public int code;

    public String descr;

    AssetMonitorEnum(Integer code, String descr) {
        this.code = code;
        this.descr = descr;
    }
}
