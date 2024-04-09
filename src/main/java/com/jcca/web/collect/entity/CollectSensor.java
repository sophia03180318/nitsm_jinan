package com.jcca.web.collect.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 环境检测传感检测采集信息
 *
 * @author Lvyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("COLLECT_SENSOR")
public class CollectSensor extends Model<CollectSensor> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 采集时间
     */
    @TableField("COLLECT_TIME")
    private Date collectTime;
    /**
     * 采集编号 同一台设备同一次采集编号相同
     */
    @TableField("COLLECT_CODE")
    private String collectCode;
    /**
     * 资产ID
     */
    @TableField("ASSET_ID")
    private String assetId;
    /**
     * 序列号
     */
    @TableField("SERIAL_NUMBER_NAME")
    private String serialNumberName;
    /**
     * 传感类型
     */
    @TableField("SENSOR_TYPE")
    private String sensorType;
    /**
     * 采集到的值
     */
    @TableField("VALUE")
    private String value;
    /**
     * 状态
     */
    @TableField("STATUS")
    private String status;
    /**
     * 描述信息
     */
    @TableField("DESC_STR")
    private String descStr;
    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private Date createTime;
    /**
     * 状态
     */
    @TableField(exist = false)
    private String statusStr;
}
