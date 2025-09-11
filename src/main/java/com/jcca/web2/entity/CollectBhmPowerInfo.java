package com.jcca.web2.entity;


import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.jcca.common.annotation.WebField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 电源信息
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("COLLECT_BHM_POWER_INFO")
public class CollectBhmPowerInfo extends Model<CollectBhmPowerInfo> {


    public static final String NORMAL_HEALTH = "OK";

    @TableId(value = "ID", type = IdType.ID_WORKER_STR)
    private String id;

    @TableField("ASSET_ID")
    private String assetId;

    /**
     * 名称
     */
    @WebField(title = "名称")
    @TableField("NAME")
    private String name;
    /**
     * 电源序列号
     */
    @WebField(title = "电源序列号")
    @TableField("SERIAL_NUMBER")
    private String serialNumber;
    /**
     * 唯一标识
     * 内部的标识
     */
    @WebField(title = "内部标识")
    @TableField("MEMBER_ID")
    private String memberId;
    /**
     * 型号
     */
    @WebField(title = "型号")
    @TableField("MODEL")
    private String model;
    /**
     * 电源制造商
     */
    @WebField(title = "制造商")
    @TableField("MANUFACTURER")
    private String manufacturer;
    /**
     * 电源转换效率（交流输入功率转换为直流输出功率的比例）
     * 百分比
     */
    @WebField(title = "电源转换效率")
    @TableField("EFFICIENCY_PERCENT")
    private Integer efficiencyPercent;

    /**
     * 源模块的固件版本号，用于标识软件版本
     */
    @WebField(title = "固件版本号")
    @TableField("FIRMWARE_VERSION")
    private String firmwareVersion;

    /**
     * 当前输入电压
     * 伏特
     */
    @WebField(title = "输入电压")
    @TableField("LINE_INPUT_VOLTAGE")
    private String lineInputVoltage;

    /**
     * 输入电压类型
     * 交流、直流
     */
    @WebField(title = "输入电压类型")
    @TableField("LINE_INPUT_VOLTAGE_TYPE")
    private String lineInputVoltageType;

    /**
     * 输入电流
     */
    @WebField(title = "输入电流")
    @TableField("INPUT_CURRENT")
    private String inputCurrent;

    /**
     * 输入功率
     */
    @WebField(title = "输入功率")
    @TableField("INPUT_POWER")
    private String inputPower;

    /**
     * 输出电流
     */
    @WebField(title = "输出电流")
    @TableField("OUTPUT_CURRENT")
    private String outputCurrent;

    /**
     * 输出电压
     */
    @WebField(title = "输出电压")
    @TableField("PUT_PUT_VOLT")
    private String outputVolt;

    /**
     * 电源模块的最大容量
     * 额定最大功率 瓦特
     */
    @TableField("POWER_CAPACITY_WATTS")
    private String powerCapacityWatts;
    /**
     * 当前输出功率
     * 实际供给服务器的功率
     */
    @WebField(title = "当前输出功率")
    @TableField("POWER_OUTPUT_WATTS")
    private String powerOutputWatts;

    /**
     * 电源类型（AC 交流，DC 直流）
     *
     */
    @WebField(title = "电源类型")
    @TableField("POWER_SUPPLY_TYPE")
    private String powerSupplyType;
    /**
     * 是否已经安装
     * true 物理上已经安装
     * false 没有安装
     */
    @TableField("PRESENT")
    private String present;



    /**
     * 健康状态
     */
    @WebField(title = "健康状态")
    @TableField("HEALTH")
    private String health;
    /**
     * 是否启用
     * Enabled 启用
     */
    @WebField(title = "是否启用")
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
