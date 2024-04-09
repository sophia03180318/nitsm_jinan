package com.jcca.web.collect.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @ Author：sophia
 * @ Date：Created in 16:36 2022/11/3
 * @ Description:
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("COLLECT_DS")
public class CollectDS extends Model<CollectRaid> {
    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.ID_WORKER_STR)
    private String id;

    /**
     * 组件名称
     */
    @TableField("ASSET_ID")
    private String assetId;

    /**
     * 组件名称
     */
    @TableField("NAME")
    private String name;

    /**
     * 磁盘类型( 0 Controller /1 ARRAY / 2  Logical driver/ 3 driver )
     */
    @TableField("TYPE")
    private Integer type;

    /**
     * 父级ID
     */
    @TableField("PARENT_ORG_ID")
    private String parentOrgId;

    /**
     * 父级名称
     */
    @TableField("PARENT_ORG_NAME")
    private String parentOrgName;

    /**
     * 组件状态
     */
    @TableField("STATUS_INFO")
    private String statusInfo;

    /**
     * 组件状态
     */
    @TableField("STATUS")
    private Integer status;

    /**
     * raid等级
     */
    @TableField("RAID_LEVEL")
    private String raidLevel;

    /**
     * 容量大小
     */
    @TableField("CAPACITY")
    private Long capacity;


    /**
     * 容量大小
     */
    @TableField("FREE_CAPACITY")
    private Long freeCapacity;


    /**
     * 容量大小
     */
    @TableField("CAPACITY_STR")
    private String capacityStr;


    /**
     * 容量大小
     */
    @TableField("FREE_CAPACITY_STR")
    private String freeCapacityStr;

    @TableField("LOG_INFO")
    private String logInfo;


    @TableField("X_INDEX")
    private Integer xindex;


    @TableField("Y_INDEX")
    private Integer yindex;


    /**
     * 采集时间
     */
    @TableField("COLLECT_TIME")
    private Date collectTime;

    /**
     * 采集编号
     */
    @TableField("COLLECT_CODE")
    private String collectCode;

    /**
     * 使用率
     * */
    @TableField(exist = false)
    private String usedRate;


    /**
     * 所属主机
     * */
    @TableField(exist = false)
    private String hostGroup;


}
