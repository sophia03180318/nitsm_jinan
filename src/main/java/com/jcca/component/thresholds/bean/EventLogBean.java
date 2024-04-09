package com.jcca.component.thresholds.bean;

import lombok.Data;

/**
 * @ Author：sophia
 * @ Date：Created in 19:48 2022/6/5
 * @ Description:
 */
@Data
public class EventLogBean {
    private static final long serialVersionUID = 1L;
    /**
     *
     */
    private String assetId;

    /**
     * 事件序号
     */
    private String seqNum;

    /**
     * 事件ID
     */
    private String eventId;

    /**
     * 错误码
     */
    private String ErrorCode;

    /**
     * 事件=1  告警=2
     */
    private Integer logType;

    /**
     * objectID
     */
    private String objectId;

    /**
     * no=1:异常  /yes=2:恢复
     */
    private Integer status;

    /**
     * 内容
     */
    private String description;

    /**
     * 事件的最近更新时间
     */
    private String lastTime;

}
