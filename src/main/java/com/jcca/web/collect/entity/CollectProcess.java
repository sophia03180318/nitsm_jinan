package com.jcca.web.collect.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;


@Data
@EqualsAndHashCode(callSuper = true)
@TableName("COLLECT_PROCESS")
public class CollectProcess extends Model<CollectProcess> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 采集时间
     */
    @TableField("COLLECT_TIME")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date collectTime;
    /**
     * 采集编号
     * 同一台设备同一次采集编号相同
     */
    @TableField("COLLECT_CODE")
    private String collectCode;
    /**
     * 资产ID
     */
    @TableField("ASSET_ID")
    private String assetId;
    /**
     * 进程名称
     */
    @TableField("NAME")
    private String name;
    /**
     * 进程ID
     */
    @TableField("PROCESS_ID")
    private String processId;
    /**
     * cpu使用率
     */
    @TableField("CPU_RATE")
    private Double cpuRate;
    /**
     * 内存使用率
     */
    @TableField("MEMORY_RATE")
    private Double memoryRate;
    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private Date createTime;

}
