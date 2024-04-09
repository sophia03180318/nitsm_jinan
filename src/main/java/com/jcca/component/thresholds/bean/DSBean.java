package com.jcca.component.thresholds.bean;

import lombok.Data;

/**
 * @ Author：sophia
 * @ Date：Created in 19:48 2022/6/5
 * @ Description:
 */
@Data
public class DSBean {

    private String id;

    /**
     * 磁盘类型( 0 Controller /1 ARRAY / 2  Logical driver/ 3 driver )
     */
    private Integer type;

    /**
     * 父级名称
     */
    private String parentOrgName;


    private String parentOrgId;
    /**
     * 组件名称
     */
    private String name;

    /**
     * 组件状态
     */
    private Integer status;

    private String statusInfo;
    /**
     * raid等级
     */
    private String raidLevel;

    /**
     * 容量大小
     */
    private Long capacity;

    private Long freeCapacity;

    private String capacityStr;

    private String freeCapacityStr;


    private Integer xindex;

    private Integer yindex;

}
