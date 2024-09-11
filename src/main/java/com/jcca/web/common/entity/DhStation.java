package com.jcca.web.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @description: 局、站
 * @author: sophia
 * @create: 2023/11/29 17:18
 **/
@Data
@TableName("DH_STATION")
public class DhStation {

    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @TableField("STATION_ID")
    private String stationId;

    @TableField("ROOM_ID")
    private String roomId; //ITSM对应的机房ID

    @TableField("PARENT_ID")
    private String parentID;

    @TableField("NAME")
    private String name;

    @TableField("DATA_DESC")
    private String descc;

    @TableField("LONGITUDE")
    private Float longitude; //经度

    @TableField("LATITUDE")
    private Float latitude; //纬度

    @TableField("CREATE_TIME")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}