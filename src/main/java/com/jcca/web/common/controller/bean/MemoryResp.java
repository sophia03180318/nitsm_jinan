package com.jcca.web.common.controller.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * 内存信息
 */
@Data
public class MemoryResp implements Serializable {

    private String collectTime;

    private String collectDate;

    private Long memTotal;

    private Long memUsed;

    private Double memUsedRate;

}
