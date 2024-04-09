package com.jcca.common.utils.constants;

public interface AppLogHead {

    String DISPOSE_DISK_SERVICE = "磁盘采集";
    String DISPOSE_INTERFACE_SERVICE = "硬件端口采集";
    String DISPOSE_PROCESS_SERVICE = "进程采集";
    String DISPOSE_PCB_SERVICE = "板卡采集";
    String DISPOSE_SENSOR_SERVICE = "环境传感数据采集";
    String DISPOSE_VLAN_SERVICE = "环境传感数据采集";
    String DISPOSE_CPU_SERVICE = "CPU数据采集";
    String DISPOSE_MEM_SERVICE = "Memory数据采集";
    String DISPOSE_SYS_TIME_SERVICE = "系统时间数据采集";
    String DISPOSE_DB_SERVICE = "数据库数据采集";
    String DISPOSE_NET_CARD_SERVICE = "网卡数据采集";

    String COLLECT_AGENCY = "采集客户端";

    String STATISTICS_HOUR_CPU = "CPU一小时统计";
    String STATISTICS_HOUR_INTERFACES = "端口一小时统计";
    String STATISTICS_HOUR_INTERFACES_ITEM = "端口一小时明细统计";
    String STATISTICS_HOUR_INTERFACES_ITEM_ALARM = "端口一小时明细流量告警";
    String STATISTICS_HOUR_MEM = "内存一小时统计";

    String REMOVE_TWO_HOUR_CPU = "CPU两小时清理";
    String REMOVE_TWO_HOUR_DB = "DB两小时清理";
    String REMOVE_TWO_HOUR_INTERFACE = "INTERFACE两小时清理";
    String REMOVE_TWO_HOUR_MEMORY = "MEMORY两小时清理";
    String REMOVE_TWO_HOUR_SYSTEM = "系统时间两小时清理";
    String REMOVE_TWO_HOUR_PROCESS = "进程两小时清理";
    String REMOVE_TWO_HOUR_RAID = "磁盘阵列数据两小时清理";
    String REMOVE_TWO_HOUR_PCB = "PCB两小时清理";
    String REMOVE_TWO_HOUR_SENEOR = "传感信息两小时清理";

    String IP_MANAGER = "ip管理";

    String REMOVE_5000 = "清理5000条数据";
}
