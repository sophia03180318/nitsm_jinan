package com.jcca.web.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @description: 属性信息
 * @author: sophia
 * @create: 2023/11/29 17:26
 **/
@Data
@TableName("DH_PROPERTY")
public class Property {
    /**
     * AI = 3    模拟输入量
     * MaxVal	float  	有效上限
     * MinVal	float  	有效下限
     * Alarmlevel	EnumAlarmLevel	告警等级
     * AlarmEnable	EnumEnable	告警使能标记
     * ControlEnable	EnumEnable 	可否控制标记
     * AlarmThresbhold	EnumEnable	告警触发阀值
     * HiLimit1	float  	一级告警上限
     * LoLimit1	float  	一级告警下限
     * HiLimit2	float  	二级告警上限
     * LoLimit2	float  	二级告警下限
     * HiLimit3	float  	三级告警上限
     * LoLimit3	float 	三级告警下限
     * Stander	float  	标称值
     * Percision	float  	精度
     * Saved	EnumEnable 	是否保存历史
     * Unit	char [UNIT_LENGTH]	单位
     * Desc0	Char [DES_LENGTH]	数字值为0时的描述
     * Desc1	Char [DES_LENGTH]	数字值为0时的描述
     */
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @TableField("PROPERTY_ID")
    private String propertyId;

    @TableField("PARENT_ID")
    private String parentID;

    @TableField("NAME")
    private String name;

    @TableField("DATA_DESC")
    private String dataDesc;

    @TableField("MAXVAL")
    private Float maxVal;

    @TableField("MINVAL")
    private Float minVal;

    @TableField("ALARM_LEVEL")
    private Integer alarmlevel;

    @TableField("ALARM_ENABLE")
    private Integer alarmEnable;

    @TableField("CONTROL_ENABLE")
    private Integer controlEnable;

    @TableField("ALARM_THRESBHOLD")
    private Integer alarmThresbhold;

    @TableField("HILIMIT1")
    private Float hiLimit1;

    @TableField("LOLIMIT1")
    private Float loLimit1;

    @TableField("HILIMIT2")
    private Float hiLimit2;

    @TableField("LOLIMIT2")
    private Float loLimit2;

    @TableField("HILIMIT3")
    private Float hiLimit3;

    @TableField("LOLIMIT3")
    private Float loLimit3;

    @TableField("STANDER")
    private float stander;

    @TableField("PERCISION")
    private float percision;

    @TableField("SAVED")
    private Integer saved;

    @TableField("UNIT")
    private String unit;

    @TableField("DESC0")
    private String desc0;

    @TableField("DESC1")
    private String desc1;

    @TableField("CREATE_TIME")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 数据值
     * */
    @TableField("PROPERTY_VALUE")
    private String propertyValue;

    /**
     * 数据状态
     * */
    @TableField("STATUS")
    private int status;

}