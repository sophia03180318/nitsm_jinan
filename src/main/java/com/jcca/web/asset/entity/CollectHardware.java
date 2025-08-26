package com.jcca.web.asset.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @author: hhw
 * @description: CollectHardware 主要是用来 存储硬件信息
 * @date: 2025-08-25  13:08
 * @since: 2.1.9.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("COLLECT_HARDWARE")
public class CollectHardware extends Model<CollectCpuLoad> {

    @TableId(value = "ID", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 资产id
     */
    private String assetId;
    /**
     * 主机名
     */
    private String hostName;
    /**
     * 操作系统版本
     */
    private String osVersion;
    /**
     * 操作系统序列号
     */
    private String osSerial;
    /**
     * 主机厂商
     */
    private String hostManufacture;
    /**
     * cpu物理核心数
     */
    private String cpuNum;
    /**
     * cpu核心数
     */
    private String cpuCoreNum;
    /**
     * cpu型号
     */
    private String cpuModel;
    /**
     * cpu频率
     */
    @TableField("CPU_MHZ")
    private String cpuMHz;
    /**
     * cpu最大频率
     */
    @TableField("CPU_MAX_MHZ")
    private String cpuMaxMHz;
    /**
     * cpu最小频率
     */
    @TableField("CPU_MIN_MHZ")
    private String cpuMinMHz;

    private String collectCode;


    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date collectTime;
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
