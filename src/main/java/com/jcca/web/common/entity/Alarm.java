package com.jcca.web.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @description:
 * @author: sophia
 * @create: 2023/11/27 11:40
 **/
@Data
@TableName("DH_ALARM")
public class Alarm {

    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @TableField("PROPERTY_ID")
    private String propertyId;

    //告警等级
    @TableField("LEVELL")
    private Integer levell;

    //告警描述
    @TableField("DESCC")
    private String descc;

    //设备ID
    @TableField("DEVICE_ID")
    private String deviceId;

    //告警序号
    @TableField("ALARM_ID")
    private String alarmId;

    //设备?名称
    @TableField("NAME")
    private String name;

    //发生时间
    @TableField("OCCURRENCE_TIME")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date occurrenceTime;

    //ID描述
    @TableField(exist = false)
    private String idDesc;

    //告警级别描述
    @TableField(exist = false)
    private String levelStr;

    //告警号
    @TableField(exist = false)
    private  String alarmNumber;

    //告警标志
    @TableField(exist = false)
    private String alarmFlag;

    //告警文本
    @TableField(exist = false)
    private String alarmInfo;

    @TableField("CREATE_TIME")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

}