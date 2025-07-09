package com.jcca.web.common.controller.bean;

import lombok.Data;

/**
 * syslog接收消息
 */
@Data
public class StationSyslogMessage {
    /**
     * 优先级(PRI) - 由设施(Facility)和严重程度(Severity)组成
     * 计算方式: (Facility * 8) + Severity
     */
    private int priority;
    /**
     * 设施代码 - 表示消息来源的系统类型
     * 例如: 0=内核, 1=用户级, 3=系统守护进程等
     */
    private int facility;
    /**
     * 严重程度代码 - 表示消息的严重级别
     * 例如: 0=紧急, 1=警报, 2=关键, 3=错误, 4=警告, 5=通知, 6=信息, 7=调试
     */
    private int severity;
    /**
     * Syslog协议版本，RFC 5424中定义为1
     */
    private int version;
    /**
     * 消息生成的时间戳，格式为ISO 8601
     */
    private String timestamp;
    /**
     * 生成消息的主机名或IP地址
     */
    private String hostname;
    /**
     * 消息标识符，用于标识特定类型的消息
     */
    private String msgId;
    /**
     * 消息的实际内容
     */
    private String message;
    /**
     * 消息接收的时间戳
     */
    private Long receivedAt;
    /**
     * 原始的完整Syslog消息字符串
     */
    private String rawMessage;
}