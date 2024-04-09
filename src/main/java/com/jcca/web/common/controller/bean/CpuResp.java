package com.jcca.web.common.controller.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * CPU数据
 */
@Data
public class CpuResp implements Serializable {

    /**
     * HH:mm:ss
     */
    private String collectTime;

    private Double cpuUsedRate;
    /**
     * yyyy-MM-dd HH:mm:ss
     */
    private String collectDate;

}
