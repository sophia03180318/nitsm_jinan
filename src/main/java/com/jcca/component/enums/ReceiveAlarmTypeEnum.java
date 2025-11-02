package com.jcca.component.enums;

import lombok.Getter;

/**
 * @ClassName ReceiveAlarmTypeEnum
 * @Description 采集器推送告警类型 告警模板匹配name
 * @Date 2020/6/18 13:40
 * @Author hanwone
 */
@Getter
public enum ReceiveAlarmTypeEnum {
    // 其他
    PING("1", "PING"), SYSLOG("2", "SYSLOG"), SNMP("3", "SNMP"),

    TH_PROCESS("4", "PROCESS"),

    INTERFACE("5", "INTERFACE"), NETWORK_CARD("6", "NETWORK_CARD"),

    // 阈值
    TH_CPU("7", "CPU"), TH_DISK("8", "DISK"), TH_MEM("9", "MEMORY"),
    TH_LOSE_PACKETS_RATE_OUT("10", "LOSE_PACKETS_RATE_OUT"), TH_LOSE_PACKETS_RATE_IN("11", "LOSE_PACKETS_RATE_IN"),
    TH_CODE_ERRO_OUT("12", "CODE_ERRO_OUT"), TH_CODE_ERRO_IN("13", "CODE_ERRO_IN"), TH_PROCESS_CPU("14", "PROCESS_CPU"),
    TH_PROCESS_MEMORY("15", "PROCESS_MEMORY"), TH_PORT_IN_RATE("21", "INTERFACE_PORT_IN"),
    TH_PORT_OUT_RATE("22", "INTERFACE_PORT_OUT"),

    TIME_DEVIATION("16", "TIME_DEVIATION"), ROUTER_TABLE("17", "ROUTER_TABLE"),

    IBMMQ("18", "IBMMQ"),

    ORACLE_DB("19", "ORACLE_DB"),

    COLLECT("20", "COLLECT"),

    // 卡斯柯
    CASCO_LINK("23", "CASCO_LINK"), // 连接告警
    CASCO_MASTER_CHANGE("24", "CASCO_MASTER_CHANGE"), // 主备切换
    CASCO_THRESHOLD("25", "CASCO_THRESHOLD"), // 阈值告警
    CASCO_VERSION_CHANGE("26", "CASCO_VERSION_CHANGE"), // 版本变化
    PROCESS("27", "PROCESS_STATUS"), // 进程

    // 通号
    CRSC_PROCESS_ALARM("30", "CRSC_PROCESS_ALARM"), // 进程告警
    CRSC_CLOCK_ALARM("31", "CRSC_CLOCK_ALARM"), // 时钟告警
    CRSC_PROCESS_STATE("32", "CRSC_PROCESS_STATE"), // 进程状态变化
    CRSC_LINK_CHANGE("33", "CRSC_LINK_CHANGE"), // 连接状态变化
    CRSC_MASTER_CHANGE("34", "CRSC_MASTER_CHANGE"), // 主备切换
    CRSC_VERSION_CHANGE("35", "CRSC_VERSION_CHANGE"), // 版本变化
    CRSC_THRESHOLD("36", "CRSC_THRESHOLD"), // 阈值

    // 铁科
    TIEKE_WORK_STATE("40", "TIEKE_WORK_STATE"), // 软件工作状态
    TIEKE_SOFT_VERSION("41", "TIEKE_SOFT_VERSION"), // 软件版本变化
    TIEKE_CHANNEL_LINK("42", "TIEKE_CHANNEL_LINK"), // 通道连接
    TIEKE_SECURE_LINK("43", "TIEKE_SECURE_LINK"), // 安全连接

    // 信达环宇
    XDHY_LOG("50", "XDHY-Log"),

    // 北羊
    BEIYANG_WORKSTATE("60", "BEIYANG_WORKSTATE"), // 软件工作状态
    BEIYANG_SOFT_VERSION("61", "BEIYANG_SOFT_VERSION"), // 软件版本变化
    BEIYANG_CHANNEL_LINK("62", "BEIYANG_CHANNEL_LINK"), // 通道连接状态

    // 通信质量监督
    CONGXING("70", "CONGXING_ALARM"), // 通信质量
    CONGXING_API("71", "CONGXING_ALARM_API"), // 通信质量
    ;


    private String code;
    private String name;

    ReceiveAlarmTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getName(String code) {
        ReceiveAlarmTypeEnum[] values = ReceiveAlarmTypeEnum.values();
        for (ReceiveAlarmTypeEnum value : values) {
            if (value.code.equals(code)) {
                return value.name;
            }
        }
        return "UNKNOWN";
    }
}
