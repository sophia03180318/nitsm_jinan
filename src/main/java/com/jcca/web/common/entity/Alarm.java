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
    @TableField("ALARM_LEVEL")
    private Integer level;

    //告警描述
    @TableField("ALARM_DESC")
    private String desc;

    //设备ID
    @TableField("DEVICE_ID")
    private String deviceId;

    //告警序号
    private String alarmId;

    //设备?名称
    private String name;

    //发生时间
    private Date occurrenceTime;

    //ID描述
    private String idDesc;

    //告警级别描述
    private String levelStr;

    //告警号
    private  String alarmNumber;

    //告警标志
    private String alarmFlag;

    //告警文本
    private String alarmInfo;

    @TableField("CREATE_TIME")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

}