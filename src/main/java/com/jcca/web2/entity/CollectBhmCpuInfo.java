package com.jcca.web2.entity;


import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 管理口的cpu硬件信息
 * 只存单次数据
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("COLLECT_BHM_CPU_INFO")
public class CollectBhmCpuInfo  extends Model<CollectBhmCpuInfo> {

    @TableId(value = "ID", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 资产ID
     */
    @TableField("ASSET_ID")
    private String assetId;
    /**
     * CPU 在服务器内的本地标识 ID，通常与物理插槽编号对应。
     */
    @TableField("CPU_ID")
    private String cpuId;
    /**
     * CPU 的易读名称，用于可视化管理界面（如服务器 BMC 界面）显示，直观区分多颗 CPU。
     */
    @TableField("NAME")
    private String name;
    /**
     * CPU 制造商
     */
    @TableField("MANUFACTURER")
    private String manufacturer;
    /**
     * CPU 具体型号
     */
    @TableField("MODEL")
    private String model;
    /**
     *支持的指令集架构
     */
    @TableField("INSTRUCTION_SET")
    private String instructionSet;
    /**
     * 处理器架构
     */
    @TableField("PROCESSOR_ARCHITECTURE")
    private String processorArchitecture;
    /**
     * 处理器类型
     */
    @TableField("PROCESSOR_TYPE")
    private String processorType;
    /**
     * CPU 对应的物理插槽编号
     */
    @TableField("SOCKET")
    private String socket;
    /**
     * CPU 物理核心总数
     */
    @TableField("TOTAL_CORES")
    private Integer totalCores;
    /**
     * CPU 逻辑线程总数
     */
    @TableField("TOTAL_THREADS")
    private Integer totalThreads;
    /**
     * CPU 单核最高睿频速度
     */
    @TableField("MAX_SPEED_MHZ")
    private Integer maxSpeedMHz;
    /**
     * 健康状态
     */
    @TableField("HEALTH")
    private String health;
    /**
     * 是否启用
     * Enabled 启用
     */
    @TableField("STATE")
    private String state;

    /**
     * 采集批次码
     */
    @TableField("COLLECT_CODE")
    private String collectCode;
    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private Date createTime;
}
