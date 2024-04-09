package com.jcca.component.constants;

/**
 * @ClassName ReceiveCollectConst
 * @Description 接收采集器推送定义的数据类型
 * @Date 2020/6/5 17:25
 * @Author hanwone
 */
public interface ReceiveCollectConst {
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

}
