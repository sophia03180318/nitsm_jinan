package com.jcca.component.event.constant;


/**
 * 事件匹配码
 *
 * @author lyp
 */
public class EventUniqueCode {

    /**
     * 未知事件
     */
    public static final String UNKONW_EVENT = "未知类型";

    /**
     * CPU阈值事件
     */
    public static final String CPU_UNIQUE_CODE = "THRE_CPU";
    /**
     * CPU阈值事件上下限
     */
    public static final String CPU_SECTION_UNIQUE_CODE = "THRE_CPU_SECTION";
    /**
     * 应用链
     */
    public static final String ASSET_APP_LINK = "ASSET_APP_LINK";
    /**
     * 磁盘
     */
    public static final String DISK_UNIQUE_CODE = "THRE_DISK";
    /**
     * 内存
     */
    public static final String MEMORY_UNIQUE_CODE = "THRE_MEMORY";
    /**
     * 区间
     */
    public static final String MEMORY_SECTION_UNIQUE_CODE = "THRE_MEMORY_SECTION";
    /**
     * 时间偏差阈值
     */
    public static final String THRE_SYSTIME_UNIQUE_CODE = "THRE_SYSTIME";
    /**
     * 运行时长阈值
     */
    public static final String THRE_RUNNINGTIME_UNIQUE_CODE = "THRE_RUNNINGTIME";
    /**
     * 重启事件
     */
    public static final String RESTART_UNIQUE_CODE = "RESTART";
    /**
     * 端口阈值-发送丢包率
     */
    public static final String THRE_INTERFACES_SEND_LOSE_UNIQUE_CODE = "THRE_INTERFACES_SEND_LOSE";
    /**
     * 发送光功率
     */
    public static final String THRE_INTERFACES_SEND_POWER = "THRE_INTERFACES_SEND_POWER";
    /**
     * 接收光功率
     */
    public static final String THRE_INTERFACES_RECEIVE_POWER = "THRE_INTERFACES_RECEIVE_POWER";
    /**
     * 端口阈值-接收丢包率
     */
    public static final String THRE_INTERFACES_RECEIVE_LOSE_UNIQUE_CODE = "THRE_INTERFACES_RECEIVE_LOSE";
    /**
     * 端口阈值-发送误码率
     */
    public static final String THRE_INTERFACES_SEND_ERROR_UNIQUE_CODE = "THRE_INTERFACES_SEND_ERROR";
    /**
     * 端口阈值-接收误码率
     */
    public static final String THRE_INTERFACES_RECEIVE_ERROR_UNIQUE_CODE = "THRE_INTERFACES_RECEIVE_ERROR";
    /**
     * 端口阈值-流入使用率
     */
    public static final String THRE_INTERFACES_PORT_IN_UNIQUE_CODE = "THRE_INTERFACES_PORT_IN";
    /**
     * 端口阈值-流出使用率
     */
    public static final String THRE_INTERFACES_PORT_OUT_UNIQUE_CODE = "THRE_INTERFACES_PORT_OUT";

    /**
     * 端口通断
     */
    public static final String INTERFACES_UP_DOWN_UNIQUE_CODE = "INTERFACES_UP_DOWN";
    /**
     * 网卡通断
     */
    public static final String NETWORK_UP_DOWN_UNIQUE_CODE = "NETWORK_UP_DOWN";

    /**
     * 表空间
     */
    public static final String TABSP_UNIQUE_CODE = "THRE_DB_TAB";

    /**
     * 数据库连接
     */
    public static final String DB_LINK = "DB_LINK";

    /**
     * 双机全断
     */
    public static final String PING_ALL_STOP = "PING_ALL_STOP";
    /**
     * 双机单断
     */
    public static final String PING_OTHER_STOP = "PING_OTHER_STOP";
    /**
     * 单机PING
     */
    public static final String PING_STOP = "PING_STOP";

    /**
     * 卡斯柯版本
     */
    public static final String CASCO_VERSION = "CASCO_VERSION";
    /**
     * 卡斯柯阈值
     */
    public static final String CASCO_THRE = "CASCO_THRE";
    /**
     * 卡斯柯连接
     */
    public static final String CASCO_LINK = "CASCO_LINK";
    /**
     * 卡斯柯主备
     */
    public static final String CASCO_CHANGE = "CASCO_CHANGE";
    /**
     * 卡斯柯主备中断
     */
    public static final String CASCO_MASTER_STOP = "CASCO_MASTER_STOP";

    /**
     * SNMP通知
     */
    public static final String SNMP_NOTIFY_UNIQUE_CODE = "SNMP_NOTIFY";
    /**
     * 路由表通知
     */
    public static final String ROUTE_NOTIFY_UNIQUE_CODE = "ROUTE_NOTIFY";

    /**
     * MQ告警
     */
    public static final String MQ_UNIQUE_CODE = "MQ_UNIQUE_CODE";
    //队列管理器告警
    public static final String QM_UNIQUE_CODE = "QM_UNIQUE_CODE";

    /**
     * x天未确认二级告警
     */
    public static final String LEVEL_2 = "LEVEL_2";

    /**
     * x天未确认三级告警
     */
    public static final String LEVEL_3 = "LEVEL_3";

    /**
     * a天 b次不健康事件
     */
    public static final String Unhealthy_1 = "Unhealthy_1";


    /**
     * c天 d次不健康事件
     */
    public static final String Unhealthy_2 = "Unhealthy_2";


    /**
     * 单进机进程中断
     */
    public static final String PROCESS_STOP = "PROCESS_STOP";
    /**
     * 进程全部丢失
     */
    public static final String PROCESS_ALL_STOP = "PROCESS_ALL_STOP";
    /**
     * 部分进程丢失
     */
    public static final String PROCESS_OTHER_STOP = "PROCESS_OTHER_STOP";

    /**
     * 进程CPU
     */
    public static final String TH_PROCESS_CPU = "TH_PROCESS_CPU";
    /**
     * 进程内存
     */
    public static final String TH_PROCESS_MEMORY = "TH_PROCESS_MEMORY";

    /**
     * 车站采集器状态事件
     */
    public static final String COLLECTOR_STATION_STATUS = "COLLECTOR_STATION_STATUS";
    /**
     * 中心采集器状态事件
     */
    public static final String COLLECTOR_CENTER_STATUS = "COLLECTOR_CENTER_STATUS";
    /**
     * 通号一些事件,相同类型的事件 告警和恢复码应该一致
     * 进程告警
     */
    public static final String CRSC_PROCESS_ALARM = "CRSC_PROCESS_ALARM";
    public static final String CRSC_CLOCK_ALARM = "CRSC_CLOCK_ALARM"; // 时钟告警
    public static final String CRSC_PROCESS_STATE = "CRSC_PROCESS_STATE"; // 进程状态
    public static final String CRSC_VERSION = "CRSC_VERSION"; // 主备
    public static final String CRSC_LINK = "CRSC_LINK"; // 连接
    public static final String CRSC_MASTER_SLAVE = "CRSC_MASTER_SLAVE"; // 主备
    public static final String CRSC_THRESHOLD = "CRSC_THRESHOLD"; // 阈值

    // 铁科告警事件
    public static final String TIEKE_WORK_STATE = "TIEKE_WORK_STATE"; // 软件工作状态
    public static final String TIEKE_SOFT_VERSION = "TIEKE_SOFT_VERSION"; // 软件版本变化
    public static final String TIEKE_CHANNEL_LINK = "TIEKE_CHANNEL_LINK"; // 通道连接
    public static final String TIEKE_SECURE_LINK = "TIEKE_SECURE_LINK"; // 安全连接

    // 信达环宇
    public static final String XDHY_SYSLOG = "XIN_DA_HUAN_YU_SYSLOG";

    // 北羊
    public static final String BEIYANG_WORKSTATE = "BEIYANG_WORKSTATE"; // 工作状态
    public static final String BEIYANG_SOFT_VERSION = "BEIYANG_SOFT_VERSION"; // 软件版本变化
    public static final String BEIYANG_CHANNEL_LINK = "BEIYANG_CHANNEL_LINK"; // 通道连接

    // 从兴
    public static final String CONGXING_ALARM = "CONGXING_ALARM"; // 通道连接

    /**
     * 温度
     */
    public static final String COLLECTOR_SENSOR_GAUGE = "COLLECTOR_SENSOR_GAUGE";

    /**
     * 端口占用
     */
    public static final String SERVER_PORT_USED = "SERVER_PORT_USED";
    /**
     * 管理口通断
     */
    public static final String IPMI_PORT_STATUS = "IPMI_PORT_STATUS";
    /**
     * 自律机集群主备切换
     */
    public static final String MAIN_BACKUP_CHANGE = "MAIN_BACKUP_CHANGE";
    /**
     * 自律机集群节点状态
     */
    public static final String SERVER_STATUS = "SERVER_STATUS";


}
