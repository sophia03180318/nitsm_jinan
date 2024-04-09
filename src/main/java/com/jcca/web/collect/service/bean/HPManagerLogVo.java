package com.jcca.web.collect.service.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * 惠普管理口
 */
@Data
public class HPManagerLogVo implements Serializable {

    /**
     * 日志ID
     */
    private String id;
    /**
     * 级别
     * 2 信息通知 不需要处理；
     * 3 信息，但与LCD警报消息
     * 6 已修复通知，已采取纠正措施
     * 9 警告非致命错误条件
     * 15 紧急  部件故障
     */
    private String severity;
    /**
     * 类型
     * 需要字典配置
     */
    private String classStr;
    /**
     * 匹配码
     */
    private String entryCode;
    /**
     * 最后一次时间
     */
    private String lastUpdate;
    /**
     * 初始时间
     */
    private String initialUpdate;
    /**
     * 发生次数
     */
    private String count;
    /**
     * 描述
     */
    private String description;

}
