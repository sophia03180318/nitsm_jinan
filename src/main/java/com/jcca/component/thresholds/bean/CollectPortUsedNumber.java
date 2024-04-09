package com.jcca.component.thresholds.bean;


import lombok.Data;

import java.io.Serializable;
import java.util.TreeSet;

/**
 * 端口占用数
 */
@Data
public class CollectPortUsedNumber implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 占用总数
     */
    private Integer usedCount;
    /**
     * 总数
     */
    private Integer portCount;
    /**
     * 使用率
     */
    private Double usedRate;
    /**
     * 资产ID
     */
    private String assetId;
    /**
     * 采集时间
     */
    private Long collectTime;
    /**
     * tcp 侦听的端口列表
     */
    private TreeSet<String> tcpPortList;
    /**
     * UDP侦听的端口列表
     */
    private TreeSet<String> udpPortList;

}
