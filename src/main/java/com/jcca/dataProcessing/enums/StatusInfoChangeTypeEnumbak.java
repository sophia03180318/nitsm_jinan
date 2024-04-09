package com.jcca.dataProcessing.enums;

import lombok.Getter;

/**
 * 枚举
 *
 * @author zhaozheng
 */
@Getter
public enum StatusInfoChangeTypeEnumbak {


    // 设备基础信息变化


    status_db("status:db_state", "数据库状态", ""),
    status_dbVersion("dbVersion", "数据库版本", ""),
    status_dbSysUpTime("dbSysUpTime", "启动时间", ""),
    status_dbCacheLibrary("dbCacheLibrary", "缓存命中率", ""),
    status_dbMemTotal("dbMemTotal", "数据库的运存总量", ""),
    status_dbDiskTotal("dbDiskTotal", " 硬盘总量", ""),
    status_dbCache("dbCache", "缓存大小", ""),
    status_dbBusynessRate("dbBusynessRate", "繁忙率", ""),
    status_dbSessionSize("dbSessionSize", "数据库的session数", ""),
    status_dbSessionUsedRate("dbSessionUsedRate", "数据库使用率", ""),
    status_dbCachePoolSize("dbCachePoolSize", "缓存池大小", ""),
    status_dbCachePoolhit("dbCachePoolhit", "缓存池命中率", ""),
    status_canUseLockSize("canUseLockSize", "可用锁数量", ""),
    status_dbLockUsedRate("dbLockUsedRate", "锁使用率", ""),
    status_dbLockWaitRate("dbLockWaitRate", "据库锁的等待率", ""),
    status_javaPoolSize("javaPoolSize", "java池大小", ""),
    status_redoLogBuffer("redoLogBuffer", "大池大小", ""),
    status_dbConnection("dbConnection", "数据库连接数", ""),
    status_dbActive("dbActive", "数据库活动连接数", ""),
    status_dbLanguage("dbLanguage", "语言环境", ""),
    status_alertPath("alertPath", "告警文件地址", ""),


    status_dbFiles("status:dbFiles", "数据库文件地址", ""),
    status_dbFiles_category("category", "数据库文件种类", ""),
    status_dbFiles_name("name", "文件名", ""),
    status_dbFiles_status("status", "文件状态", ""),
    status_dbFiles_health("health", "健康状态", ""),
    status_tablespace("status:tablespace", "数据库表空间", ""),
    status_tablespace_name("name", "表空间名称", ""),


    status_conn_size("conn_size", "连接数状态", ""),


    status_run_time("run_time", "运行时间", ""),


    status_power("status:power", "电源状态", ""),


    status_tcp_port("status:port_tcp", "tcp端口占用明细", ""),
    status_udp_port("status:port_udp", "udp端口占用明细", ""),
    status_time("status:time", "时间状态", ""),


    status_ConnNumState("connNumState", "连接数阈值状态", ""),
    status_timeChannel("softChannel", "软件时间同步", ""),
    status_clock("softclock", "时钟同步服务器状态", ""),

    status_net("status:net", "网卡状态", ""),


    //自定义事件信息  category分类
    //CPU事件
    event("event", "设备事件", ""),
    event_CPU("event:event_CPU", "cpu状态", ""),
    event_CPU_normal("event:event_CPU:normal", "cpu普通阈值事件", getPubThresholdMsg("", "CPU使用率", "%%")),
    event_CPU_section("event:event_CPU:section", "cpu区间阈值事件", getSectionThresholdMsg("", "CPU使用率", "%%")),
    event_CPU_sectionOne("event:event_CPU:sectionOne", "cpu一阶阈值事件", getLevelThresholdMsg("", "CPU使用率", "一阶", "%%")),
    event_CPU_sectionTwo("event:event_CPU:sectionTwo", "cpu二阶阈值事件", getLevelThresholdMsg("", "CPU使用率", "二阶", "%%")),
    event_CPU_sectionThree("event:event_CPU:sectionThree", "cpu三阶阈值事件", getLevelThresholdMsg("", "CPU使用率", "三阶", "%%")),


    //MQ状态信息事件
    event_mq("event:event_mq", "MQ状态", ""),
    event_mq_connect("event:event_mq:connect", "mq连接状态事件", "MQ连接状态异常"),
    event_mq_channel("event:event_mq:channel", "mq通道状态事件", "MQ通道状态异常"),
    event_mq_queue("event:event_mq:queue", "mq队列状态事件", "MQ队列状态异常"),
    event_mq_queue_threshold("event:event_mq:queue_threshold", "mq队列阈值状态事件", "MQ队列%s超过设定最大阈值！"),

    //网络安全状态事件 network security
    event_networkSecurity("event:event_networkSecurity", "网络安全状态", ""),
    event_networkSecurity_state("event:event_networkSecurity:state", "网络安全运行状态事件", "网络安全运行状态异常。");


    private String code;
    private String name;
    private String descr;

    /**
     * 拼装普通阈值
     *
     * @param title
     * @param msg
     * @return
     */
    private static String getPubThresholdMsg(String title, String msg, String unit) {
        return title + "当前采集到" + msg + "为: %s" + unit + ",设定阈值为:%s" + unit + "。";
    }

    /**
     * 拼装级别阈值
     *
     * @param title
     * @param msg
     * @return
     */
    private static String getLevelThresholdMsg(String title, String msg, String level, String unit) {
        return title + "当前采集到" + msg + "为: %s" + unit + ",设定" + level + "阈值为:%s" + unit + "。";
    }

    /**
     * 拼装区间阈值
     *
     * @param title
     * @param msg
     * @return
     */
    private static String getSectionThresholdMsg(String title, String msg, String unit) {
        return title + "当前采集到" + msg + ": %s" + unit + ",设定允许最大值为：%s%%,允许最小值为：%s" + unit + "。";
    }

    StatusInfoChangeTypeEnumbak(String code, String name, String descr) {
        this.code = code;
        this.name = name;
        this.descr = descr;
    }

    public static String getName(String code) {
        StatusInfoChangeTypeEnumbak[] values = StatusInfoChangeTypeEnumbak.values();
        for (StatusInfoChangeTypeEnumbak value : values) {
            if (value.code.equals(code)) {
                return value.name;
            }
        }
        return "UNKNOWN";
    }

    public static String getDescr(String code) {
        StatusInfoChangeTypeEnumbak[] values = StatusInfoChangeTypeEnumbak.values();
        for (StatusInfoChangeTypeEnumbak value : values) {
            if (value.code.equals(code)) {
                return value.descr;
            }
        }
        return "UNKNOWN";
    }
}
