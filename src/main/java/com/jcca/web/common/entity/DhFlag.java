package com.jcca.web.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @description:
 * @author: sophia
 * @create: 2023/11/27 11:40
 **/
@Data
@TableName("DH_FLAG")
public class DhFlag {

    @TableField("FLAG")
    private String flagWord;

    @TableField("DEVICE_ID")
    private String deviceId;

    @TableField("EVENT_ID")
    private String eventId;


}