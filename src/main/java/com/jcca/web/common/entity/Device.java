package com.jcca.web.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @description: 设备信息
 * @author: sophia
 * @create: 2023/11/29 17:27
 **/
@Data
@TableName("DH_DEVICE")
public class Device {

    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @TableField("DEVICE_ID")
    private String deviceId;

    @TableField("PARENT_ID")
    private String parentID;

    @TableField("NAME")
    private String name;

    @TableField("DATA_DESC")
    private String dataDesc;

    @TableField("DEVICE_TYPE")
    private Integer deviceType; //设备类型

    @TableField("PRODUCTOR")
    private String productor;   //生产厂家

    @TableField("VERSION")
    private String version;     //设备版本

    @TableField("BEGIN_RUN_TIME")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date beginRunTime; //上架时间

    @TableField("CREATE_TIME")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

}