package com.jcca.component.crsc.constant;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description: 存放所有业务常量
 * @classname: BusinessConst
 * @date: 2022/4/12 18:15
 * @author: hanwone
 * @since: 2.0.0.1
 */
public interface BusinessConst {

    // 存放临时数据
    Map<String, Object> TEMP_MAP = new ConcurrentHashMap<>();

    // 通号心跳间隔5秒
    long CRSC_HEART_PERIOD_5 = 5L;
    // 通号心跳计数
    String HEAR_BEART = "HEAR_BEART";
    // 获取netty通道KEY
    String NETTY_TCP_CHANNEL = "nettyTCPChannel";
    String NETTY_SERVER_CHANNEL = "nettyServerChannel"; // 测试用

    // 帧类型码
    int CRSC_FRAME_TYPE = 0x01;

    // 版本号
    int CRSC_VERSION = 1;

    // 帧功能码
    // 心跳信息
    int CRSC_HEART_BEAT = 0x01;
    // 请求信息
    int CRSC_REQUEST = 0x02;
    // 进程基本信息
    int CRSC_PROCESS_BASE = 0x03;
    // 进程状态信息
    int CRSC_PROCESS_STATE = 0x04;
    // 设备连接信息
    int CRSC_PROCESS_CONN = 0x05;
    // 设备报警信息
    int CRSC_ASSET_ALARM = 0x06;

    // 请求数据类型
    // 基本信息
    int CRSC_BASE = 0x01;
    // 状态信息
    int CRSC_STATE = 0x02;
    // 连接信息
    int CRSC_CONN = 0x03;
    // 报警信息
    int CRSC_ALARM = 0x04;

    // 程序处理代码
    String CRSC_HEART_BEAT_CODE = "CRSC_HEART_BEAT_CODE";
    String CRSC_BASE_CODE = "CRSC_BASE_CODE";
    String CRSC_STATE_CODE = "CRSC_STATE_CODE";
    String CRSC_CONN_CODE = "CRSC_CONN_CODE";
    String CRSC_ALARM_CODE = "CRSC_ALARM_CODE";

    // 设备单位 车站
    int CRSC_STATION = 0x01;
    // 设备单位 中心
    int CRSC_CENTER = 0x02;

    // 设备本机标识A机0
    int CRSC_A = 0x55;
    // 设备本机标识B机1
    int CRSC_B = 0xaa;

    // 设备主备标识 主机0
    int CRSC_MASTER = 204;
    // 设备主备标识 备机1
    int CRSC_SLAVE = 85;
    // 设备主备状态 未知 -2
    int CRSC_UNKNOW = 0xcc;

    // 进程状态, 1：启动 2：停止 3：报警，其他无效
    int CRSC_PROCESS_STATE_1 = 1;
    int CRSC_PROCESS_STATE_2 = 2;
    int CRSC_PROCESS_STATE_3 = 3;
    int CRSC_PROCESS_STATE_4 = -1;


    // 进程报警
    int CRSC_PROCESS_ALARM = 1;
    // 时钟报警
    int CRSC_CLOCK_ALARM = 2;
    // 报警发生
    int CRSC_ALARM_HAPPEN = 0;
    // 报警恢复
    int CRSC_ALARM_RECOVER = 1;

    // 连接状态
    int CRSC_VOID = 0;
    int CRSC_UP = 1;
    int CRSC_DOWN = 2;

    // 通号基础数据文件MD5 key
    String CRSC_REDIS_MD5 = "CRSC_REDIS_MD5";
}
