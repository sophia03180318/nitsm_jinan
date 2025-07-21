package com.jcca.dataProcessing.Entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * @author: hhw
 * @description: CollectCpuLoadBean 主要是用来接收CPU负载信息
 * @date: 2025-07-02  13:08
 * @since: 2.0.13.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CollectCpuLoadBean extends CommonEntity implements Serializable {

    /**
     * CPU一分钟负载
     */
    private String cpuLoadOne;
    /**
     * CPU五分钟负载
     */
    private String cpuLoadFive;
    /**
     * CPU十五分钟负载
     */
    private String cpuLoadFifteen;
    /**
     * CPU逻辑线程数
     */
    private String cpuLogicalNum;
    /**
     * 应用连接信息 <端口,ip>
     */
    private Map<String, Set<String>> connectInfoMap;
}
