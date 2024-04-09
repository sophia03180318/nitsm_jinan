package com.jcca.admin.biz.enums;

import lombok.Getter;

/**
 * @ClassName AlarmTemplateEnum
 * @Description 告警模板类型枚举
 * 说明：
 * 本类中的code对应ReceiveAlarmTypeEnum中的code，
 * category对应alarm_template表中的category字段，
 * categoryName对应告警内容中的组件。
 * @Date 2020/6/18 9:43
 * @Author hanwone
 */
@Getter
public enum AlarmTemplateEnum {

    PING("1", "PING", "PING告警"),
    SYSLOG("2", "SYSLOG", "SYSLOG告警"),
    SNMP("3", "SNMP", "SNMP告警"),
    PROCESS("4", "PROCESS", "进程丢失告警"),
    INTERFACE("5", "INTERFACE", "端口通断告警"),
    NETWORK_CARD("6", "NETWORK_CARD", "网卡通断告警"),

    CPU("7", "THRESHOLD_PERFORMANCE", "CPU"),
    DISK("8", "THRESHOLD_PERFORMANCE", "磁盘"),
    MEM("9", "THRESHOLD_PERFORMANCE", "内存"),

    TH_PORT_IN_RATE("21", "THRESHOLD_INTERFACES", "端口流量流入"),
    TH_PORT_OUT_RATE("22", "THRESHOLD_INTERFACES", "端口流量流出"),
    LOSE_PACKETS_RATE_OUT("10", "THRESHOLD_INTERFACES", "流出丢包率"),
    LOSE_PACKETS_RATE_IN("11", "THRESHOLD_INTERFACES", "流入丢包率"),
    CODE_ERRO_OUT("12", "THRESHOLD_INTERFACES", "流出误码率"),
    CODE_ERRO_IN("13", "THRESHOLD_INTERFACES", "流入误码率"),
    PROCESS_CPU("14", "THRESHOLD_PROCESS", "进程CPU"),
    PROCESS_MEM("15", "THRESHOLD_PROCESS", "进程内存"),

    TIME_DEVIATION("16", "TIME_DEVIATION", "时间偏差"),
    ROUTER_TABLE("17", "ROUTER_TABLE", "路由表变化"),
    IBMMQ("18", "IBMMQ", "IBMMQ告警"),

    ORACLE_DB("19", "ORACLE_DB", "ORACLE数据库表空间"),

    // 卡斯柯相关 syt
    CASCO_LINK("23", "CASCO_LINK", "卡斯柯连接告警"),
    CASCO_MASTER_CHANGE("24", "CASCO_MASTER_CHANGE", "卡斯柯主备切换告警"),
    CASCO_CHANGE_VERSION("25", "CASCO_CHANGE_VERSION", "卡斯柯版本变更通知"),
    CASCO_THRESHOLD("26", "CASCO_THRESHOLD", "卡斯柯阈值告警");

    private String code;
    private String category;
    private String categoryName;

    AlarmTemplateEnum(String code, String category, String categoryName) {
        this.code = code;
        this.category = category;
        this.categoryName = categoryName;
    }

    public static String getCategory(String code) {
        AlarmTemplateEnum[] values = AlarmTemplateEnum.values();
        for (AlarmTemplateEnum value : values) {
            if (value.code.equals(code)) {
                return value.category;
            }
        }
        return code;
    }

    public static String getCategoryName(String code) {
        AlarmTemplateEnum[] values = AlarmTemplateEnum.values();
        for (AlarmTemplateEnum value : values) {
            if (value.code.equals(code)) {
                return value.categoryName;
            }
        }
        return code;
    }
}
