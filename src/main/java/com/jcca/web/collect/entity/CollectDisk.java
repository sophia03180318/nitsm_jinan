package com.jcca.web.collect.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 采集的磁盘信息
 *
 * @author Lvyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("COLLECT_DISK")
public class CollectDisk extends Model<CollectDisk> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 采集时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("COLLECT_TIME")
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
     * 挂载点
     */
    @TableField("MOUNT_POINT")
    private String mountPoint;
    /**
     * 总量
     */
    @TableField("TOTAL")
    private Long total;
    /**
     * 已使用
     */
    @TableField("USED")
    private Long used;
    /**
     * 剩余可用
     */
    @TableField("FREE")
    private Long free;
    /**
     * 磁盘使用率
     */
    @TableField("USED_RATE")
    private Double usedRate;
    /**
     * 磁盘可用率
     */
    @TableField("FREE_RATE")
    private Double freeRate;
    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private Date createTime;

}
