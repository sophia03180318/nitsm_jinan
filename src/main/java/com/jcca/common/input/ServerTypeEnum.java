package com.jcca.common.input;

import lombok.Getter;

/**
 * 业务类型
 */
@Getter
public enum ServerTypeEnum {

    SYSTEM_INIT(1,"系统初始化业务"),

    SYSTEM_ORG_SYSLOG(3,"Syslog原始接收"),

    JOB_QUARTZ(4,"定时任务"),

    STATION_AGENT(5,"车站客户端"),

    ASSET(1000,"资产"),

    PROCESS_MANAGER(1061,"进程管理"),

    NO_GROUP_PROCESS_STATUS(1070,"无组进程状态分析"),

    ALARM_INFO(1071,"事件告警管理"),

    WEB_ASSET_MONITOR_STATUS(1001,"资产监控状态判定"),

    WEB_ASSET_COLLECT_TEST(1002,"资产采集测试"),

    WEB_ASSET_TEMP(1003,"资产模板配置"),

    WEB_ASSET_DEL(1004,"资产删除"),

    WEB_ASSET_EXCEL_MODE_DOWNLOAD(1005,"下载资产模板"),

    WEB_ASSET_IMPORT(1006,"资产导入"),

    WEB_CABINET(1011,"机柜业务"),

    WEB_DS_MANAGER(1021,"DS管理业务"),

    WEB_HARDWARE_MANAGER(1031, "硬件更换记录"),

    WEB_OPTICAL_EXPORT(1041, "光功率计算导入、导出"),

    WEB_RAID(1051, "磁盘阵列管理"),

    WEB_XUNJIAN(1061, "智能巡检"),

    QUEUE_PUSH_WEB(2000, "告警推送队列"),

    //    QUEUE_CASCO(2010, "casco业务队列"),
//    QUEUE_CRSC(2020, "通号业务队列"),
//    QUEUE_TIEKE(2021, "铁科业务队列"),
//    QUEUE_BEIYANG(2022, "北羊业务队列"),
//    QUEUE_XDHY(2023, "信达环宇业务队列"),
//    QUEUE_CONGXING(2024, "从兴业务队列"),
    QUEUE_BROKER(2025, "业务队列"),

    QUEUE_EVENT_ADD(2030, "1分析保存事件队列"),

    QUEUE_EVENT_GROUP_ALARM(2040, "2预处理告警队列"),

    QUEUE_EXE_ALARM_EVENT(2050, "3告警处理队列"),

    AIX_SYSLOG(3000, "aix系统日志接收模块"),

    RAID_SYSLOG(3010, "存储系统日志接收模块"),

    STATION_ALARM(3020,"车站告警接收模块"),

    SYSTEM_COLLECTOR(2000,"采集配置"),

    SYSTEM_IMPORT_CABINET(2001,"机柜导入"),

    SYSTEM_MONITOR(2002,"首页"),
    SYSTEM_LOGIN(2003,"登录"),
    SYSTEM_ORG(2004,"组织管理"),
    SYSTEM_STATION(2005,"车站升级"),

    DISPOSE_CLUSTER(5000, "集群处理队列");

    private Integer code;
    private String msg;

    ServerTypeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
