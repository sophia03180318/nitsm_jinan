package com.jcca.dataProcessing.Entity;

import lombok.Data;

/**
 * @ Author：sophia
 * @ Date：Created in 19:48 2022/6/5
 * @ Description:
 */
@Data
public class DiskEntity extends CommonEntity {

    private String id;
    private String realId;
    /**
     * 磁盘类型(0池/1M_Disk/2V_Disk/3Capacity/4Drive)
     */
    private Integer diskType;

    /**
     * 名称
     */
    private String name;
    /**
     * 状态(online 联接)
     */
    private String status;

    /**
     * 池ID
     */
    private String GrpId;

    /**
     * raid等级
     */
    private String raidLevel;

    /**
     * 容量大小
     */
    private String capacity;

    /**
     * 使用容量
     */
    private String usedCapacity;

    /**
     * 父级名称
     */
    private String parentOrgName;

    /**
     * 父级ID
     * */
    private String parentOrgId;

    private Integer xindex;

    private Integer yindex;

    private String capacityStr;

    private String usedCapacityStr;


}
