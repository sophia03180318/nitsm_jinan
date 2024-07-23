package com.jcca.web.common.controller.bean;

import lombok.Data;

/**
 * @description: ITSM进程配置
 * @author: Lvyp
 * @create: 2024/04/19 18:21
 */
@Data
public class ItsmProcessConf {

    /**
     * 进程名字
     */
    private String processName;
    /**
     * 进程ID
     */
    private String processId;
    /**
     * CPU使用率阈值
     */
    private Double cpuRate;
    /**
     * 进程内存使用率阈值
     */
    private Double memoryRate;

}
