package com.jcca.dataProcessing.Entity;


import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 端口占用数
 */
@Data
public class CollectPortUsedNumberEntity extends  CommonEntity implements Serializable {

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
     * tcp 侦听的端口列表
     */
    private List<String> tcpPortList;
    /**
     * UDP侦听的端口列表
     */
    private List<String> udpPortList;

}
