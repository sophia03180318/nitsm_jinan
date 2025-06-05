package com.jcca.web2.dto.xunjian;

import lombok.Data;

import java.io.Serializable;

/**
 * 发送告警信息
 *
 * @author Lvyp
 */
@Data
public class SendPingAlarmReq implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 发生内容
     */
    private String content;
    /**
     * 发生时间
     * yyyy-MM-dd HH:mm:ss
     */
    private String occurTime;
    /**
     * 告警类型
     * 1：ping告警
     */
    private String category = "1";


    /**
     * 设备ip, 无组ping需要传此参数
     */
    private String assetIp;

    /**
     * 通断标志,无组ping需要传此参数
     */
    private Boolean flag;
    /**
     * 巡检采集任务ID
     */
    private String inspectRecordId;


}
