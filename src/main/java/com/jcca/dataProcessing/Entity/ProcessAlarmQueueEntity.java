package com.jcca.dataProcessing.Entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 中心采集到的进程处理
 * @author: Lvyp
 * @create: 2023/11/21 18:00
 */
@Data
public class ProcessAlarmQueueEntity extends  CommonEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 进程状态
     * true 正常 false 丢失、异常
     */
    private Boolean processStatus;
    /**
     * 进程名称
     */
    private String processName;
    /**
     * 进程号
     */
    private String processId;

    private String assetIp;

    /**
     * 进程在阈值表的ID
     */
    private String thresholdId;

}
