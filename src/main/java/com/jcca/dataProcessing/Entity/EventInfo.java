package com.jcca.dataProcessing.Entity;

import lombok.Data;

/**
 * @author Zhaozheng
 * @description TODO 设备事件告警信息类
 * @className EventInfo
 * @date 2023/11/28 12:57
 * @since 2.1.0.0
 */
@Data
public class EventInfo {
    /**
     * 原始告警级别
     */
    private Integer level;
    /**
     * 事件id
     */
    private String messageId;
    /**
     *事件原始消息
     */
    private String message;
    /**
     *
     */
    private String eventType;
}
