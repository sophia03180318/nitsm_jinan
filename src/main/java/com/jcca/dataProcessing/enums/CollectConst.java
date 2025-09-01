package com.jcca.dataProcessing.enums;

/**
 * @ClassName ReceiveCollectConst
 * @Description 接收采集器推送定义的数据类型
 * @Date 2020/6/5 17:25
 * @Author hanwone
 */
public interface CollectConst {
    /**
     * CPU
     */
    String CPU = "1";
    /**
     * 磁盘V
     */
    String DISK = "2";
    /**
     * 内存
     * MEM
     */
    String MEM = "3";
    /**
     * 硬件端口
     */
    String INTERFACE = "4";
    /**
     * 板卡
     */
    String PCB = "5";
    /**
     * 进程
     */
    String PROCESS = "6";
    /**
     * 环境传感监控
     */
    String SENSOR = "7";
    /**
     * VLAN数据
     */
    String VLAN = "8";
    /**
     * 中心系统各类时间采集
     */
    String SYSTEM_TIME = "9";
    /**
     * 数据库采集
     */
    String DB = "10";
    /**
     * 网卡
     */
    String NET_CARD = "11";
    /**
     * 车站时间处理
     */
    String SYSTEM_STATION_TIME = "12";
    /**
     * 管理口
     */
    String SYS_PORT = "13";

    /**
     * 连接数
     */

    String CONNECT = "14";

    /**
     * AIX 系统信息采集
     */
    String AIX_SYSTEM_MSG = "15";

    /**
     * 磁盘阵列信息采集
     */
    String RAID_SYSTEM_MSG = "16";

    /**
     * 光交换机
     */
    String OPTICAL_SWITCH = "17";

    /**
     * DS
     */

    String DS_SYSTEM_MSG = "18";
    /**
     * 端口占用数
     */
    String PORT_NUMBER = "19";

    String CLUSTER = "20";

    /**
     * 宏杉存储
     */
    String HS_RAID_SYSTEM_MSG="21";

    /**
     * 卡斯柯
     */
    String CASCO_THRESHOLD = "25";
    String CASCO_LINK = "23";
    String CASCO_MASTER = "24";
    String CASCO_VERSION = "26";

    String PROCESS_GROUP="27";
    /**
     * 北羊
     */
    String BEIYANG_CHANNEL = "62";
    String BEIYANG_VERSION = "61";
    String BEIYANG_WORKSTATE = "60";
    /**
     * 通号
     */
    String CRSC_CLOCK = "30";
    String CRSC_LINK = "31";
    String CRSC_MASTER = "32";
    String CRSC_PROCESS = "33";
    String CRSC_STATE = "34";
    String CRSC_VERSION = "35";
    /**
     * 铁科
     */

    String TIEKE_SECURE_LINK = "43";
    String TIEKE_VERSION = "41";
    String TIEKE_WORKSTATE = "40";
    String TIEKE_CHANNEL_LINK = "42";
    /**
     * 信达环宇
     */
    String XIN_DA_HUAN_YU = "50";
    /**
     * 通信质量监督
     */
    String CONGXING = "70";


    String SYSLOG = "71";

    String DB_ALARM = "72";

    String ping = "73";

    String CUSTOMEVENT = "74";

    String MQ = "75";

    String SNMP = "76";

    String DongHuan = "77";

    /**
     * 采集CPU负载信息
     */
    String CPU_LINUX_LOAD_AVG = "2025070301";

    String CollectNodeStatus = "78";


    /**
     * 风扇采集
     */
    String BHM_FAN="80";
    /**
     * 电源采集
     */
    String BHM_POWER="81";
    /**
     * PCIE采集
     */
    String BHM_PCIE="82";
    /**
     * CPU信息采集
     */
    String BHM_CPU_MSG="83";
    /**
     * 内存信息采集
     */
    String BHM_MEMORY_MSG="84";
    /**
     * 存储信息采集
     */
    String BHM_STORAGE_MSG="85";
    /**
     * 温度信息采集
     */
    String BHM_TEMP_MSG="86";

}
