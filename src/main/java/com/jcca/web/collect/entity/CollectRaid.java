package com.jcca.web.collect.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 磁盘阵列
 *
 * @author sophia
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("COLLECT_RAID")
public class CollectRaid extends Model<CollectRaid> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.ID_WORKER_STR)
    private String id;

    /**
     * 真实ID
     */
    @TableField("REAL_ID")
    private String realId;

    /**
     * 类型 0:池 1:Mdisk 2:卷组 3capacity 4Drive 5log
     */
    @TableField("DISK_TYPE")
    private Integer diskType;

    /**
     * 资产主键
     */
    @TableField("ASSET_ID")
    private String assetId;
    /**
     * 名称
     */
    @TableField("NAME")
    private String name;
    /**
     * 状态
     */
    @TableField("STATUS")
    private String status;

    /**
     * 池ID
     */
    @TableField("GROUP_ID")
    private String GrpId;

    /**
     * raid等级
     */
    @TableField("RAID_LEVEL")
    private String raidLevel;

    /**
     * 容量
     */
    @TableField("CAPACITY")
    private Long capacity;

    /**
     * 使用容量
     */
    @TableField("USED_CAPACITY")
    private Long usedCapacity;

    /**
     * 采集时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("COLLECT_TIME")
    private Date collectTime;



    /**
     * 采集编号
     */
    @TableField("COLLECT_CODE")
    private String collectCode;

    /**
     * 容量大小
     */
    @TableField("CAPACITY_STR")
    private String capacityStr;

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


    @TableField("X_INDEX")
    private Integer xindex;


    @TableField("Y_INDEX")
    private Integer yindex;

    @TableField("LOG_INFO")
    private String logInfo;


    @TableField("USED_CAPACITY_STR")
    private String usedCapacityStr;

    @TableField(exist=false)
    private String collectTimeStr;

    @TableField(exist = false)
    private Integer health;

    @TableField(exist = false)
    private String usedRate;

    @TableField(exist = false)
    private String freeCapacityStr;


}
