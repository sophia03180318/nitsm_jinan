package com.jcca.web.collect.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * cpu采集数据
 *
 * @author Lvyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("COLLECT_CPU")
public class CollectCpu extends Model<CollectCpu> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 采集时间
     * 时间的格式化统计分组使用
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
     * cpu使用率
     * 单位%
     */
    @TableField("CPU_USED_RATE")
    private Double cpuUsedRate;
    /**
     * 同一台资产
     * cpu唯一标识
     */
    @TableField("CPU_FLG")
    private String cpuFlg;
    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 分组标识
     */
    @TableField(exist = false)
    private String groupFlg;

}
