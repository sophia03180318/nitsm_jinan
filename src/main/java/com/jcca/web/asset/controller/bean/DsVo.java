package com.jcca.web.asset.controller.bean;

import lombok.Data;

/**
 * @ Author：sophia
 * @ Date：Created in 16:05 2022/11/4
 * @ Description:
 */
@Data
public class DsVo {

    /**
     * 容量大小
     */
    private Long capacity;

    /**
     * 剩余容量大小
     */
    private Long freeCapacity;

    /**
     * 容量大小
     */
    private String capacityStr;

    /**
     * 已使用容量大小
     */
    private String usedCapacityStr;
    /**
     * 剩余容量大小
     */
    private String freeCapacityStr;

    /**
     * 使用率
     * */
    private String usedRate;

}
