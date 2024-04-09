package com.jcca.web.collect.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 系统时间相关采集信息
 *
 * @author Lvyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("COLLECT_SYSTEM_TIME")
public class CollectSystemTime extends Model<CollectSystemTime> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 采集时间
     */
    @TableField("COLLECT_TIME")
    private Date collectTime;
    /**
     * 资产ID
     */
    @TableField("ASSET_ID")
    private String assetId;
    /**
     * 时间偏差时长，毫秒
     */
    @TableField("TIME_SPAN")
    private Long timeSpan;
    /**
     * 系统时间
     */
    @TableField("SYSTEM_DATE")
    private Date systemDate;
    /**
     * 设备运行时长
     */
    @TableField("TIMEDURATION")
    private Long timeduration;
    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private Date createTime;

}
